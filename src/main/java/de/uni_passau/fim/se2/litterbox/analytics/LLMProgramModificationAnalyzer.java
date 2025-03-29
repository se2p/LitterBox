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

package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.llm.ScratchLLM;
import de.uni_passau.fim.se2.litterbox.llm.api.OpenAiApi;
import de.uni_passau.fim.se2.litterbox.llm.prompts.DefaultPrompts;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;

import java.util.*;

public abstract class LLMProgramModificationAnalyzer implements ProgramAnalyzer<Program> {

    private final static String SPRITE_HEADER = "//Sprite: ";

    private final static String SCRIPT_HEADER = "//Script: ";

    protected ScratchLLM<OpenAiApi, DefaultPrompts> scratchLLM;

    protected QueryTarget target;

    protected boolean ignoreLooseBlocks;

    protected LLMProgramModificationAnalyzer(
            QueryTarget target,
            boolean ignoreLooseBlocks,
            ScratchLLM<OpenAiApi, DefaultPrompts> scratchLLM
    ) {
        this.target = target;
        this.ignoreLooseBlocks = ignoreLooseBlocks;
        this.scratchLLM = scratchLLM;
    }

    protected LLMProgramModificationAnalyzer(
            QueryTarget target,
            boolean ignoreLooseBlocks
    ) {
        this(target, ignoreLooseBlocks, ScratchLLM.buildScratchLLM());
    }

    public abstract String callLLM(Program program);

    @Override
    public Program analyze(Program program) {
        String response = callLLM(program);

        Map<String, Map<String, ScriptEntity>> spriteScripts = parseLLMResponse(response);
        ActorDefinitionList newActors = getActorDefinitionList(program.getActorDefinitionList(), spriteScripts);
        NodeReplacementVisitor replacementVisitor = new NodeReplacementVisitor(program.getActorDefinitionList(), newActors);

        return (Program) program.accept(replacementVisitor);
    }

    private ActorDefinitionList getActorDefinitionList(ActorDefinitionList originalActorDefinitionList,
                                                       Map<String, Map<String, ScriptEntity>> actorMap) {
        List<ActorDefinition> actors = new ArrayList<>();
        // TODO: This just replaces existing actors, might need to merge scriptlists in the future
        for (ActorDefinition actor : originalActorDefinitionList.getDefinitions()) {
            if (!actorMap.containsKey(actor.getIdent().getName())) {
                actors.add(actor);
            }
        }

        for (String actorName : actorMap.keySet()) {
            ActorDefinition actor = getBlankActorDefinition(actorName);
            NodeReplacementVisitor replacementVisitor = new NodeReplacementVisitor(actor.getScripts(), getScriptList(actorMap.get(actorName)));
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
    public Map<String, Map<String, ScriptEntity>> parseLLMResponse(String response) {
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

        return spriteScripts;
    }
}
