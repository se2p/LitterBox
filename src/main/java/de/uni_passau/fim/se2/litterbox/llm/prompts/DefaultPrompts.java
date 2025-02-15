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
package de.uni_passau.fim.se2.litterbox.llm.prompts;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.Collection;
import java.util.stream.Collectors;

public class DefaultPrompts extends PromptBuilder {

    @Override
    public String askQuestion(final Program program, final QueryTarget target, final String question) {
        return describeTarget(program, target) + """
                Answer the following question:
                %s
                """.formatted(question);
    }

    @Override
    public String improveCode(final Program program, final QueryTarget target, final Collection<Issue> issues) {
        final String issueDescription = issues.stream().map(Issue::getHint).collect(Collectors.joining("\n\n"));

        return describeTarget(program, target) + """
               The code contains the following bugs and code smells:
               %s

               Create a version of the program where these issues are fixed.
               Only output the ScratchBlocks code and nothing else.
               """.formatted(issueDescription);
    }

    @Override
    public String completeCode(final Program program, final QueryTarget target) {
        return describeTarget(program, target) + """
                Auto-complete the code.
                """;
    }

    @Override
    protected String describeTarget(final Program program, final QueryTarget target) {
        final ASTNode targetNode = target.getTargetNode(program);
        final String label = target.getTargetDescription();
        final String scratchBlocks = ScratchBlocksVisitor.of(targetNode);

        return """
               You are given the following %s:
               %s
                """.formatted(label, scratchBlocks);
    }
}
