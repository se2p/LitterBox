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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScriptReplacementVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VariableAsLiteralTest implements JsonTest {

    @Test
    public void testEmpty() throws IOException, ParsingException {
        assertThatFinderReports(0, new VariableAsLiteral(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testLiteralsInSayAndIf() throws IOException, ParsingException {
        Program program = JsonTest.parseProgram("src/test/fixtures/bugpattern/variableAsLiteral.json");
        VariableAsLiteral issueFinder = new VariableAsLiteral();
        Set<Issue> reports = issueFinder.check(program);
        assertThat(reports).hasSize(2);
        for (Issue anIssue : reports) {
            ScriptReplacementVisitor visitor = new ScriptReplacementVisitor(anIssue.getScript(), (Script) anIssue.getRefactoredScriptOrProcedureDefinition());
            Program refactoredProgram = (Program) program.accept(visitor);
            Set<Issue> refactoredIssues = issueFinder.check(refactoredProgram);
            assertThat(refactoredIssues).hasSize(1);
        }
    }

    @Test
    public void testListAsLiteral() throws IOException, ParsingException {
        assertThatFinderReports(2, new VariableAsLiteral(), "src/test/fixtures/bugpattern/listAsLiteral.json");
    }

    @Test
    public void testActorsAsLiterals() throws IOException, ParsingException {
        assertThatFinderReports(0, new VariableAsLiteral(), "src/test/fixtures/bugpattern/actorsAsLiterals.json");
    }

    @Test
    public void testRandomListEntry() throws IOException, ParsingException {
        assertThatFinderReports(0, new VariableAsLiteral(), "src/test/fixtures/bugpattern/randomListentry.json");
    }

    @Test
    public void testScratchBlocksOutput() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/listAsLiteral.json");
        VariableAsLiteral finder = new VariableAsLiteral();
        List<Issue> issues = new ArrayList<>(finder.check(program));
        Issue issue = issues.get(0);

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        String output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "if <[thelist] > (50):: #ff0000> then // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "say [thelist]" + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(), output);

        issue = issues.get(1);

        visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]" + System.lineSeparator() +
                "when green flag clicked" + System.lineSeparator() +
                "if <[thelist] > (50)> then" + System.lineSeparator() +
                "say [thelist]:: #ff0000 // " + ScratchBlocksVisitor.BUG_NOTE + System.lineSeparator() +
                "end" + System.lineSeparator() +
                "[/scratchblocks]" + System.lineSeparator(), output);
    }

    @Test
    public void testBlocksThatShouldNotBeRecognisedAsStrings() throws IOException, ParsingException {
        assertThatFinderReports(0, new VariableAsLiteral(), "src/test/fixtures/smells/attributesThatAreNotStrings.json");
    }

    @Test
    public void testHideListWithNoBug() throws IOException, ParsingException {
        assertThatFinderReports(0, new VariableAsLiteral(), "src/test/fixtures/bugpattern/hideList.json");
    }

    @Test
    public void testHint() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/happyNewYear.json");
        VariableAsLiteral lit = new VariableAsLiteral();
        Set<Issue> reports = lit.check(program);
        Assertions.assertEquals(1, reports.size());
        Hint hint = new Hint(lit.getName());
        hint.setParameter(Hint.HINT_VARIABLE, "aktuelles Jahr");
        for (Issue issue : reports) {
            Truth.assertThat(issue.getHint()).isEqualTo(hint.getHintText());
        }
    }

    @Test
    public void testSoundNameNoBug() throws IOException, ParsingException {
        assertThatFinderReports(0, new VariableAsLiteral(), "src/test/fixtures/bugpattern/variableNameInDropDowns.json");
    }

    @Test
    public void testParameterNameOutsideProcedure() throws IOException, ParsingException {
        assertThatFinderReports(0, new VariableAsLiteral(), "src/test/fixtures/bugpattern/parameterNameAsALiteralOutsideProcedure.json");
    }

    @Test
    public void testCallParameter() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/variableInLiteralCallParameter.json");
        VariableAsLiteral lit = new VariableAsLiteral();
        Set<Issue> reports = lit.check(program);
        Assertions.assertEquals(2, reports.size());
        for (Issue issue : reports) {
            Truth.assertThat(issue.getCodeLocation()).isNotNull();
        }
    }
}
