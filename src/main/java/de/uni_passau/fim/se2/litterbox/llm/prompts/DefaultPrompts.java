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
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.Collection;
import java.util.stream.Collectors;

public class DefaultPrompts extends PromptBuilder {

    @Override
    public String askQuestion(final Program program, final String question) {
        return """
                You are given the following Scratch program:
                %s

                Please answer the following question:
                %s
                """.formatted(ScratchBlocksVisitor.of(program), question);
    }

    @Override
    public String askQuestion(final Program program, final String sprite, final String question) {
        final ActorDefinition spriteNode = AstNodeUtil.getActors(program, false)
                .filter(actor -> actor.getIdent().getName().equals(sprite))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Could not find sprite '" + sprite + "' in the program.")
                );
        final String scratchBlocks = ScratchBlocksVisitor.of(spriteNode);

        return """
                You are given the following Scratch sprite:
                %s

                Answer the following question:
                %s
                """.formatted(scratchBlocks, question);
    }

    @Override
    public String improveCode(final ASTNode code, final Collection<Issue> issues) {
        final String scratchBlocks = ScratchBlocksVisitor.of(code);
        final String issueDescription = issues.stream().map(Issue::getHint).collect(Collectors.joining("\n\n"));

        return """
                You are given the following Scratch program:
                %s

                The program contains the following bugs and code smells:
                %s

                Create a version of the program where this bug is fixed.
                Only output the ScratchBlocks code and nothing else.
                """.formatted(scratchBlocks, issueDescription);
    }
}
