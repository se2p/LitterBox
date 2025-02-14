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

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.llm.ScratchLLM;
import de.uni_passau.fim.se2.litterbox.llm.api.OpenAiApi;
import de.uni_passau.fim.se2.litterbox.llm.prompts.CommonQuery;
import de.uni_passau.fim.se2.litterbox.llm.prompts.DefaultPrompts;
import de.uni_passau.fim.se2.litterbox.utils.Either;

import java.util.logging.Logger;

public class ProgramLLMAnalyzer implements ProgramAnalyzer<String> {

    private static final Logger log = Logger.getLogger(ProgramLLMAnalyzer.class.getName());

    private String query;

    private String targetSprite;

    private String detectors;

    private boolean ignoreLooseBlocks;

    // TODO: Probably should use two different analyzers rather than a flag
    private boolean fix;

    public ProgramLLMAnalyzer(
            Either<String, CommonQuery> query,
            String targetSprite,
            String detectors,
            boolean ignoreLooseBlocks,
            boolean fix
    ) {
        this.query = query.asLeft(); // todo: handle CommonQuery case
        this.targetSprite = targetSprite;
        this.detectors = detectors;
        this.ignoreLooseBlocks = ignoreLooseBlocks;
        this.fix = fix;
    }

    @Override
    public String analyze(Program program) {
        // TODO: bubble up options for LlmApi and prompts
        ScratchLLM<OpenAiApi, DefaultPrompts> scratchLLM = new ScratchLLM<>(new OpenAiApi(), new DefaultPrompts());
        log.fine("Target sprite: " + targetSprite);
        String response;
        // TODO: Handle this properly once we know what APIs we actually want to offer
        if (fix) {
            response = scratchLLM.improve(program, detectors, ignoreLooseBlocks);
        } else {
            if (targetSprite != null) {
                response = scratchLLM.askAbout(program, targetSprite, query);
            } else {
                response = scratchLLM.askAbout(program, query);
            }
        }
        return response;
    }
}
