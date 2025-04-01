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
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class LLMResponseParser {

    private static final String SPRITE_HEADER = "//Sprite: ";

    private static final String SCRIPT_HEADER = "//Script: ";

    /*
     * Try to fix common obvious errors in ScratchBlocks syntax produced by LLMs.
     */
    public static String fixCommonScratchBlocksIssues(String scratchBlocks) {
        return scratchBlocks.replace("set rotation to", "point in direction");
    }

    public Program parseResultAndUpdateProgram(Program program, String response) {
        ParsedLlmResponseCode spriteScripts = parseLLMResponse(response);
        ActorDefinitionList newActors = getActorDefinitionList(program.getActorDefinitionList(), spriteScripts);
        NodeReplacementVisitor replacementVisitor = new NodeReplacementVisitor(program.getActorDefinitionList(), newActors);

        return (Program) program.accept(replacementVisitor);
    }

    // todo: updateScript? which script gets updated? should this be getUpdatedScriptFromResponse?
    public Script parseResultAndUpdateScript(Program program, Script script, String response) {
        ParsedLlmResponseCode spriteScripts = parseLLMResponse(response);
        Optional<ActorDefinition> actor = AstNodeUtil.findActor(script);

        if (actor.isEmpty()) {
            throw new IllegalArgumentException("Script is not part of an actor");
        }

        return (Script) spriteScripts.script(actor.get().getIdent().getName(), AstNodeUtil.getBlockId(script.getEvent()));
    }

    /**
     * Merges the original sprites with the scripts received from the LLM.
     *
     * @param originalActorDefinitionList The sprites of the original program.
     * @param llmCode The new code as received by the LLM.
     * @return The updated sprite list.
     */
    private ActorDefinitionList getActorDefinitionList(ActorDefinitionList originalActorDefinitionList,
                                                       ParsedLlmResponseCode llmCode) {
        List<ActorDefinition> actors = new ArrayList<>();
        // TODO: This just replaces existing actors, might need to merge scriptlists in the future
        for (ActorDefinition actor : originalActorDefinitionList.getDefinitions()) {
            if (!llmCode.scripts().containsKey(actor.getIdent().getName())) {
                actors.add(actor);
            }
        }

        for (final var entry : llmCode.scripts().entrySet()) {
            final String actorName = entry.getKey();
            final Map<String, ScriptEntity> scripts = entry.getValue();

            ActorDefinition actor = getBlankActorDefinition(actorName);
            NodeReplacementVisitor replacementVisitor = new NodeReplacementVisitor(actor.getScripts(), getScriptList(scripts));
            actors.add((ActorDefinition) actor.accept(replacementVisitor));
        }

        return new ActorDefinitionList(Collections.unmodifiableList(actors));
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

    private ScriptList getScriptList(Map<String, ScriptEntity> scriptMap) {
        List<Script> scripts = new ArrayList<>();
        for (ScriptEntity scriptEntry : scriptMap.values()) {
            // TODO: Need to handle custom blocks as well, this assumes it can only be a script
            scripts.add((Script) scriptEntry);
        }
        return new ScriptList(Collections.unmodifiableList(scripts));
    }

    /**
     * Parses an LLM response in scratchblocks format.
     *
     * @param response The LLM response.
     * @return A map from sprite name to its scripts, represented as a map from script ID to the scratchblocks code as a string.
     */
    public ParsedLlmResponseCode parseLLMResponse(String response) {
        ScratchBlocksParser parser = new ScratchBlocksParser();

        Map<String, Map<String, ScriptEntity>> spriteScripts = new HashMap<>();
        String[] lines = response.split("\n");
        String currentSprite = null;
        String currentScriptId = null;
        StringBuilder currentScriptCode = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("scratch")) {
                // skip -- GPT likes to start markdown blocks with language tags
                // Matches "scratch" and "scratchblocks"
            } else if (line.startsWith(SPRITE_HEADER)) {
                if (currentSprite != null && currentScriptId != null) {
                    spriteScripts.computeIfAbsent(currentSprite, k -> new HashMap<>())
                            .put(currentScriptId, parser.parseScript(currentScriptCode.toString()));
                }
                currentSprite = line.substring(SPRITE_HEADER.length()).trim();
                currentScriptId = null;
                currentScriptCode.setLength(0);
            } else if (line.startsWith(SCRIPT_HEADER)) {
                if (currentScriptId != null) {
                    spriteScripts.computeIfAbsent(currentSprite, k -> new HashMap<>())
                            .put(currentScriptId, parser.parseScript(currentScriptCode.toString()));
                }
                currentScriptId = line.substring(SCRIPT_HEADER.length()).trim();
                currentScriptCode.setLength(0);
            } else {
                currentScriptCode.append(line.trim()).append("\n");
            }
        }

        if (currentSprite != null && currentScriptId != null) {
            spriteScripts.computeIfAbsent(currentSprite, k -> new HashMap<>())
                    .put(currentScriptId, parser.parseScript(currentScriptCode.toString()));
        }

        return new ParsedLlmResponseCode(spriteScripts);
    }

    public record ParsedLlmResponseCode(Map<String, Map<String, ScriptEntity>> scripts) {
        /**
         * Returns all scripts for the given actor.
         *
         * @param actor The name of the sprite/actor.
         * @return All scripts of this actor.
         */
        @Nonnull
        public Map<String, ScriptEntity> actor(final String actor) {
            return Objects.requireNonNullElse(scripts.get(actor), Collections.emptyMap());
        }

        /**
         * Finds a script in the set of parsed scripts.
         *
         * @param actor The name of the sprite/actor the script is in.
         * @param scriptId The block ID of the head block of the script.
         * @return The script if it could be found, {@code null} otherwise.
         */
        @Nullable
        public ScriptEntity script(final String actor, final String scriptId) {
            return actor(actor).get(scriptId);
        }
    }
}
