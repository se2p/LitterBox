/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.FinderTest;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class IfElseStatementExecutionTest implements FinderTest, JsonTest {

    private final IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfElseStatementExecution(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testNoIfStmts() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfElseStatementExecution(), "src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testScriptWithOnlyIfThenStmt() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfElseStatementExecution(), "src/test/fixtures/questions/oneVariableInIfStatement.json");
    }

    @Test
    public void testScriptWithOnlyIfElseStmt() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfElseStatementExecution(), "src/test/fixtures/questions/twoVariablesInIfElseStatement.json");
    }

    @Test
    public void testScriptWithTwoIfElseStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new IfElseStatementExecution(), "src/test/fixtures/questions/twoIfElseStmtsInScript.json");
    }

    @Test
    public void testScriptWithNestedIfElseStmts() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfElseStatementExecution(), "src/test/fixtures/questions/nestedIfElseStmts.json");
    }

    @Test
    public void testTwoScriptsWithIfElseStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new IfElseStatementExecution(), "src/test/fixtures/questions/twoScriptsWithBothIfStmts.json");
    }

    @Test
    public void testEmptyControlBody() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfElseStatementExecution(), "src/test/fixtures/questions/emptyIfElseBody.json");
    }

    @Test
    public void testHint() throws ParsingException, IOException {
        Program prog = getAST("src/test/fixtures/questions/twoVariablesInIfElseStatement.json");
        Set<Question> questions = runFinder(prog, new IfElseStatementExecution(), false);

        for (Question question : questions) {
            String questionText = question.getQuestionText(translator);
            assertThat(question.hasCorrectAnswers()).isTrue();
            // Check that the question text contains the localized condition status
            assertThat(questionText).containsMatch("TRUE|FALSE");
            // Check that the correct answer contains scratchblocks code
            String correct = question.getCorrectAnswers().get(0).resolveText(translator, translator::getInfo);
            assertThat(correct).isNotEmpty();
        }
    }
}
