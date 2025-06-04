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
package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.truth.Truth.assertThat;

class StatementsInIfStatementTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new StatementsInIfStatement(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testIfStmtAndOtherStmt() throws IOException, ParsingException {
        assertThatFinderReports(1, new StatementsInIfStatement(), "./src/test/fixtures/questions/ifStmtAndOtherStmt.json");
    }

     @Test
    public void testTwoIfStmtsAndOtherStmt() throws IOException, ParsingException {
         Program prog = getAST("src/test/fixtures/questions/twoIfStmtsAndOtherStmt.json");
         Set<Issue> issues = runFinder(prog, new StatementsInIfStatement(), false);
         assertThat(issues.size()).isEqualTo(1);

         for (Issue issue : issues) {
             String choices = "\\[choices]\\[sbi](?:(?!\\[sbi]).)*\\[/sbi]\\[/choices]";
             String answer = "\\[solutions](\\[sbi](?:(?!\\[sbi]).)*\\[/sbi]\\|){2}\\[sbi](?:(?!\\[sbi]).)*\\[/sbi]\\[/solutions]";
             assertThat(issue.getHint().getHintText()).containsMatch(Pattern.compile(choices, Pattern.DOTALL));
             assertThat(issue.getHint().getHintText()).containsMatch(Pattern.compile(answer, Pattern.DOTALL));
         }
    }

    @Test
    public void testNestedIfStmts() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/questions/nestedControlBlock.json");
        Set<Issue> issues = runFinder(prog, new StatementsInIfStatement(), false);
        assertThat(issues.size()).isEqualTo(1);

        for (Issue issue : issues) {
            String choices = "\\[choices]\\[sbi](?:(?!\\[sbi]).)*\\[/sbi]\\|\\[sbi](?:(?!\\[sbi]).)*\\[/sbi]\\[/choices]";
            String answer = "\\[solutions](\\[sbi](?:(?!\\[sbi]).)*\\[/sbi]\\|){2}\\[sbi](?:(?!\\[sbi]).)*\\[/sbi]\\[/solutions]";
            assertThat(issue.getHint().getHintText()).containsMatch(Pattern.compile(choices, Pattern.DOTALL));
            assertThat(issue.getHint().getHintText()).containsMatch(Pattern.compile(answer, Pattern.DOTALL));
        }
    }

    @Test
    public void testOnlyIfStmt() throws IOException, ParsingException {
        assertThatFinderReports(0, new StatementsInIfStatement(), "./src/test/fixtures/questions/twoVariablesInIfElseStatement.json");
    }

    @Test
    public void testIfStmtAndLoop() throws IOException, ParsingException {
        assertThatFinderReports(1, new StatementsInIfStatement(), "./src/test/fixtures/questions/scriptWithLoopAndOtherElements.json");
    }

    @Test
    public void testIfStmtInsideLoop() throws IOException, ParsingException {
        assertThatFinderReports(1, new StatementsInIfStatement(), "./src/test/fixtures/questions/repeatTimesWithStopInIfThen.json");
        assertThatFinderReports(1, new StatementsInIfStatement(), "./src/test/fixtures/questions/ifStmtInsideLoop.json");
    }

    @Test
    public void testLoopInsideIfStmt() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/questions/loopInsideIfStmt.json");
        Set<Issue> issues = runFinder(prog, new StatementsInIfStatement(), false);
        assertThat(issues.size()).isEqualTo(1);

        for (Issue issue : issues) {
            String choices = "\\[choices]\\[sbi](?:(?!\\[sbi]).)*\\[/sbi]\\[/choices]";
            String answer = "\\[solutions]\\[sbi](?:(?!\\[sbi]).)*\\[/sbi]\\[/solutions]";
            assertThat(issue.getHint().getHintText()).containsMatch(Pattern.compile(choices, Pattern.DOTALL));
            assertThat(issue.getHint().getHintText()).containsMatch(Pattern.compile(answer, Pattern.DOTALL));
        }
    }

}
