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
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import de.uni_passau.fim.se2.litterbox.utils.Either;

import java.util.logging.Logger;

public class LLMProgramQueryAnalyzer implements ProgramAnalyzer<String> {

    private static final Logger log = Logger.getLogger(LLMProgramQueryAnalyzer.class.getName());

    private String query;

    private QueryTarget target;

    // TODO: Handle this option
    private boolean ignoreLooseBlocks;

    public LLMProgramQueryAnalyzer(
            Either<String, CommonQuery> query,
            QueryTarget target,
            boolean ignoreLooseBlocks
    ) {
        this.query = query.asLeft(); // todo: handle CommonQuery case
        this.target = target;
        this.ignoreLooseBlocks = ignoreLooseBlocks;
    }

    @Override
    public String analyze(Program program) {
        // TODO: bubble up options for LlmApi and prompts
        ScratchLLM<OpenAiApi, DefaultPrompts> scratchLLM = new ScratchLLM<>(new OpenAiApi(), new DefaultPrompts());
        return scratchLLM.askAbout(program, target, query);
    }
}
