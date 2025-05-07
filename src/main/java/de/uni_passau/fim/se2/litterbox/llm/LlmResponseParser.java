/*
 * Copyright (C) 2019-2024 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.llm;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchProjectMerger;

import java.util.*;

public class LlmResponseParser {

    private static final String SPRITE_HEADER = "//Sprite: ";

    private static final String SCRIPT_HEADER = "//Script: ";

    private final ScratchBlocksParser parser = new ScratchBlocksParser();

    /*
     * Try to fix common obvious errors in ScratchBlocks syntax produced by LLMs.
     */
    public static String fixCommonScratchBlocksIssues(String scratchBlocks) {
        return scratchBlocks.replace("set rotation to", "point in direction");
    }

    /**
     * Integrates the parsed response into the given program.
     *
     * @param program     The program the LLM was originally queried with.
     * @param llmResponse The response snippets from the LLM.
     * @return The updated program.
     */
    public Program updateProgram(final Program program, final ParsedLlmResponseCode llmResponse) {
        ActorDefinitionList newActors = mergeActors(program, llmResponse);
        NodeReplacementVisitor replacementVisitor = new NodeReplacementVisitor(
                program.getActorDefinitionList(), newActors
        );
        Program newProgram = (Program) program.accept(replacementVisitor);

        return updateProgramInfo(newProgram, llmResponse);
    }

    private Program updateProgramInfo(final Program program, final ParsedLlmResponseCode llmCode) {
        ScratchProjectMerger merger = new ScratchProjectMerger();
        Program newProgram = program;
        for (final var entry : llmCode.scripts().entrySet()) {
            final String actorName = entry.getKey();
            final Map<String, ScriptEntity> scripts = entry.getValue();
            for (ScriptEntity scriptEntity : scripts.values()) {
                newProgram = merger.updateProjectInfo(program, actorName, scriptEntity);
            }
        }
        return newProgram;
    }

    // todo: updateScript? which script gets updated? should this be getUpdatedScriptFromResponse?
    public Script parseResultAndUpdateScript(Program program, Script script, String response) {
        ParsedLlmResponseCode spriteScripts = parseLLMResponse(response);
        Optional<ActorDefinition> actor = AstNodeUtil.findActor(script);

        if (actor.isEmpty()) {
            throw new IllegalArgumentException("Script is not part of an actor");
        }

        return (Script) spriteScripts.script(
                actor.get().getIdent().getName(), AstNodeUtil.getBlockId(script.getEvent())
        );
    }

    /**
     * Merges the original sprites with the scripts received from the LLM.
     *
     * @param originalProgram The original program.
     * @param llmCode         The new code as received by the LLM.
     * @return The updated sprite list.
     */
    private ActorDefinitionList mergeActors(final Program originalProgram, final ParsedLlmResponseCode llmCode) {
        List<ActorDefinition> actors = new ArrayList<>();

        // copy over unchanged actors
        for (ActorDefinition actor : originalProgram.getActorDefinitionList().getDefinitions()) {
            if (!llmCode.scripts().containsKey(actor.getIdent().getName())) {
                if (actor.isStage()) {
                    actors.add(0, actor);
                } else {
                    actors.add(actor);
                }
            }
        }

        // merge changed actors from original program + new/changed scripts from LLM response
        for (final var entry : llmCode.scripts().entrySet()) {
            final String actorName = entry.getKey();
            final Map<String, ScriptEntity> scripts = entry.getValue();

            final ActorDefinition actor = originalProgram.getActorDefinitionList().getActorDefinition(actorName)
                    .orElseGet(() -> getBlankActorDefinition(actorName));
            final ActorDefinition updatedActor = mergeActor(actor, scripts);

            if (updatedActor.isStage()) {
                actors.add(0, updatedActor);
            } else {
                actors.add(updatedActor);
            }
        }

        return new ActorDefinitionList(Collections.unmodifiableList(actors));
    }

    /**
     * Adds new scripts and procedures from an LLM response into the original actor.
     *
     * <p>Overrides existing scripts/procedures based on the ID.
     *
     * @param originalActor      The original actor from the project the LLM was queried for.
     * @param llmResponseScripts The scripts and procedures contained in the LLM response.
     * @return The updated actor.
     */
    private ActorDefinition mergeActor(final ActorDefinition originalActor, final Map<String, ScriptEntity> llmResponseScripts
    ) {
        final Map<String, Script> scripts = new HashMap<>();
        final Map<String, ProcedureDefinition> procedures = new HashMap<>();

        // copy over original scripts and procedures
        originalActor.getScripts().getScriptList()
                .forEach(script -> scripts.put(AstNodeUtil.getBlockId(script.getEvent()), script));
        originalActor.getProcedureDefinitionList().getList()
                .forEach(procedure -> procedures.put(AstNodeUtil.getBlockId(procedure), procedure));

        // override scripts and procedures also contained in LLM response
        for (final var entry : llmResponseScripts.entrySet()) {
            if (entry.getValue() instanceof Script script) {
                scripts.put(entry.getKey(), script);
            } else if (entry.getValue() instanceof ProcedureDefinition procedureDefinition) {
                procedures.put(entry.getKey(), procedureDefinition);
            } else {
                throw new IllegalStateException("Unknown script type");
            }
        }

        final NodeReplacementVisitor scriptsReplacementVisitor = new NodeReplacementVisitor(
                originalActor.getScripts(), new ScriptList(List.copyOf(scripts.values()))
        );
        ActorDefinition updatedActor = (ActorDefinition) originalActor.accept(scriptsReplacementVisitor);

        final NodeReplacementVisitor procedureReplacementVisitor = new NodeReplacementVisitor(
                updatedActor.getProcedureDefinitionList(),
                new ProcedureDefinitionList(List.copyOf(procedures.values()))
        );


        return (ActorDefinition) updatedActor.accept(procedureReplacementVisitor);
    }

    private ActorDefinition getBlankActorDefinition(String actorName) {
        return new ActorDefinition(ActorType.getSprite(),
                new StrId(actorName),
                new DeclarationStmtList(Collections.emptyList()),
                new SetStmtList(Collections.emptyList()),
                new ProcedureDefinitionList(Collections.emptyList()),
                new ScriptList(Collections.emptyList()),
                new ActorMetadata(
                        new CommentMetadataList(Collections.emptyList()),
                        0,
                        new ImageMetadataList(Collections.emptyList()),
                        new SoundMetadataList(Collections.emptyList())
                )
        );
    }

    /**
     * Parses an LLM response in ScratchBlocks format.
     *
     * @param response The LLM response.
     * @return The parseable and not parseable scripts as found in the LLM message.
     */
    public ParsedLlmResponseCode parseLLMResponse(String response) {
        response = fixCommonScratchBlocksIssues(response);

        Map<String, Map<String, ScriptEntity>> spriteScripts = new HashMap<>();
        Map<String, Map<String, String>> unparseableScripts = new HashMap<>();
        String currentSprite = null;
        String currentScriptId = null;
        StringBuilder currentScriptCode = new StringBuilder();

        for (String line : response.lines().toList()) {
            if (line.startsWith("scratch")) {
                // skip -- GPT likes to start markdown blocks with language tags
                // Matches "scratch" and "scratchblocks"
            } else if (line.startsWith(SPRITE_HEADER)) {
                if (currentSprite != null && currentScriptId != null) {
                    parseScript(
                            spriteScripts, unparseableScripts, currentSprite, currentScriptId,
                            currentScriptCode.toString()
                    );
                }
                currentSprite = line.substring(SPRITE_HEADER.length()).trim();
                currentScriptId = null;
                currentScriptCode.setLength(0);
            } else if (line.startsWith(SCRIPT_HEADER)) {
                if (currentScriptId != null) {
                    parseScript(
                            spriteScripts, unparseableScripts, currentSprite, currentScriptId,
                            currentScriptCode.toString()
                    );
                }
                currentScriptId = line.substring(SCRIPT_HEADER.length()).trim();
                currentScriptCode.setLength(0);
            } else {
                currentScriptCode.append(line.trim()).append("\n");
            }
        }

        if (currentSprite != null && currentScriptId != null) {
            parseScript(
                    spriteScripts, unparseableScripts, currentSprite, currentScriptId, currentScriptCode.toString()
            );
        }

        return new ParsedLlmResponseCode(spriteScripts, unparseableScripts);
    }

    private void parseScript(
            final Map<String, Map<String, ScriptEntity>> scripts,
            final Map<String, Map<String, String>> parseFailedScripts,
            final String currentSprite,
            final String currentScriptId,
            final String scratchBlocksScript
    ) {
        final Optional<ScriptEntity> parsedScript = tryParseScript(currentSprite, scratchBlocksScript);

        if (parsedScript.isEmpty()) {
            parseFailedScripts.computeIfAbsent(currentSprite, k -> new HashMap<>())
                    .put(currentScriptId, scratchBlocksScript);
        } else {
            scripts.computeIfAbsent(currentSprite, k -> new HashMap<>())
                    .put(currentScriptId, parsedScript.get());
        }
    }

    private Optional<ScriptEntity> tryParseScript(final String currentSprite, final String scriptScratchBlocksCode) {
        try {
            final ScriptEntity script = parser.parseScriptEntity(currentSprite, scriptScratchBlocksCode);
            return Optional.ofNullable(script);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
