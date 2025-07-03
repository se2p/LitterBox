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
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultPrompts extends PromptBuilder {

    @Override
    public String systemPrompt() {
        return """
                You are a teacher whose aim it is to help a student learning.
                The student has created a Scratch program and now needs help on this program.

                All textual responses and identifiers in code examples should be using the language: %s
                """.formatted(IssueTranslator.getInstance().getLanguage());
    }

    @Override
    public String askQuestion(
            final Program program, final QueryTarget target, final LlmQuery question, final boolean ignoreLooseBlocks
    ) {
        String questionText;
        if (question instanceof LlmQuery.PredefinedQuery predefinedQuery) {
            questionText = createPromptForCommonQuery(predefinedQuery.query());
        } else {
            questionText = ((LlmQuery.CustomQuery) question).query();
        }
        return describeTarget(program, target, ignoreLooseBlocks) + """
                Answer the following question:
                %s
                """.formatted(questionText);
    }

    @Override
    public String improveCode(final Program program, final QueryTarget target, final Collection<Issue> issues) {
        final String issueDescription = issues.stream().map(Issue::getHintText).collect(Collectors.joining("\n\n"));

        return describeTarget(program, target) + """
                The code contains the following bugs and code smells:
                %s

                Create a version of the program where these issues are fixed.
                Only output the ScratchBlocks code and nothing else.
                Include sprite and script ids in the ScratchBlocks code.
                """.formatted(issueDescription);
    }

    @Override
    public String completeCode(final Program program, final QueryTarget target) {
        String result = describeTarget(program, target)
                + """
                Auto-complete the code.
                """;
        if (target.getTargetDescription().equals("sprite")) {
            result += """
                    The improved code should either extend an existing script with new blocks,
                    or add a new script to the sprite. The new code should enhance existing
                    functionality, or provide new functionality. It should not duplicate or clone
                    existing code.
                    """;
        } else if (target.getTargetDescription().equals("script")) {
            result += """
                    The improved code should extend the existing script with new blocks.
                    """;
        } else {
            result += """
                    The improved code should either extend an existing script with new blocks,
                    or add a new script to an existing sprite. The new code should enhance existing
                    functionality, or provide new functionality. It should not duplicate or clone
                    existing code.
                    """;
        }

        result += """
                Only output the ScratchBlocks code and nothing else.
                Include sprite and script ids in the ScratchBlocks code.
                Do not delete existing blocks.
                """;
        return result;
    }

    @Override
    public String createPromptForCommonQuery(CommonQuery query) {
        return switch (query) {
            case SUMMARISE:
                yield """
                        Summarise what this code does.
                        """;
            case EXPLAIN:
                yield """
                        Explain how this code works.
                        """;
            case SUGGEST_EXTENSION:
                yield """
                        Suggest how to extend this code with new functionality.
                        """;
            case PROVIDE_FEEDBACK:
                yield """
                        This code was written by a student.
                        Provide feedback to the student about the code as well as the creativity.
                        """;
            case PROVIDE_PRAISE:
                yield """
                        This code was written by a student. Provide praise to the student who wrote it.
                        """;
            case FIND_BUGS:
                yield """
                        Find and describe any bugs in this code.
                        """;
        };
    }

    @Override
    public String findNewBugs(String existingBugsDescription) {
        return """
                A static code analysis tool identified the following list of issues in the given code:
                %s

                List any further bugs in the code not already included in this list.
                Do not suggest new program features.
                Do not list generic issues such as "lack of comments in the code".
                New issues must describe problems related to specific existing statements in the code.
                Do not list bugs that are already contained in the list of issues above.
                Report each issue using the following structure:

                New Finding <number>:
                - Finding Description: <textual issue description>
                - Finding Location: <ID of the script containing the issue>
                """.formatted(existingBugsDescription).stripIndent();
    }

    @Override
    public String explainIssue(Issue issue) {
        return """
                A static code analysis tool identified an issue and provides the following explanation:
                %s

                Describe an example user interaction with the program and
                how it might be affected by the issue.

                Only provide a list of steps to reproduce the issue, but no other text,
                except for a title "Here is how you might observe effects of this issue:".
                """.formatted(issue.getHintText()).stripIndent();
    }

    @Override
    public String isIssueFalsePositive(final Issue issue) {
        return """
                A static code analysis tool identified an issue in sprite %s, script %s, and provides the following
                explanation:
                %s

                Does the given program actually contain the issue described by the static code analysis tool?
                Respond only with "yes" or "no".
                """.formatted(
                        issue.getActorName(), AstNodeUtil.getBlockId(issue.getScript()), issue.getHintText()
                ).stripIndent();
    }

    @Override
    public String fixSyntax(final String scratchBlocksScripts) {
        return """
                You know the ScratchBlocks syntax from the Scratch community forums. Numbers, strings, and variables are
                surrounded by parentheses. Boolean conditions are surrounded by angled brackets < and > instead. Only
                dropdown menus surround the selected option with brackets like `[option v]`.
                The following Scratch scripts are in the ScratchBlocks syntax. They contain invalid syntax.
                Fix the syntax.
                Only return the fixed ScratchBlocks code. Do not change the comments starting with `//`.

                ```
                %s
                ```
                """.formatted(scratchBlocksScripts).stripIndent();
    }

    @Override
    protected String describeTarget(final Program program, final QueryTarget target, final boolean ignoreLooseBlocks) {
        final ASTNode targetNode = target.getTargetNode(program);

        final String scratchBlocks;
        if (ignoreLooseBlocks) {
            scratchBlocks = ScratchBlocksVisitor.ofIgnoringLooseBlocks(targetNode);
        } else {
            scratchBlocks = ScratchBlocksVisitor.of(targetNode);
        }

        // Parsing expects sprite names and script ids
        if (target.getTargetDescription().equals("script")) {
            Optional<ActorDefinition> actor = AstNodeUtil.findActor(targetNode);
            String scriptId = AstNodeUtil.getBlockId(targetNode);
            if (actor.isPresent()) {
                String spriteName = actor.get().getIdent().getName();
                return """
                        You are given the script with id %s in sprite %s:
                        //Sprite: %s
                        //Script: %s
                        %s
                        """.formatted(scriptId, spriteName, spriteName, scriptId, scratchBlocks);
            } else {
                throw new IllegalArgumentException("No sprite found for script " + scriptId);
            }
        } else if (target.getTargetDescription().equals("sprite")) {
            Optional<ActorDefinition> actor = AstNodeUtil.findActor(targetNode);
            if (actor.isPresent()) {
                String spriteName = actor.get().getIdent().getName();
                return """
                        You are given the sprite with name %s:
                        %s
                        """.formatted(spriteName, scratchBlocks);
            } else {
                throw new IllegalArgumentException("Sprite not found for");
            }
        } else {
            return """
                    You are given the following program:
                    %s
                    """.formatted(scratchBlocks);
        }
    }
}
