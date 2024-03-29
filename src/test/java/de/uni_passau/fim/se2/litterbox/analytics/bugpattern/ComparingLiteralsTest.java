/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.hint.ComparingLiteralsHintFactory;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class ComparingLiteralsTest implements JsonTest {

    @Test
    public void testComparingLiterals() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/comparingLiterals.json");
        ComparingLiterals finder = new ComparingLiterals();
        Set<Issue> reports = finder.check(program);
        Truth.assertThat(reports).hasSize(3);
        Hint trueHint = new Hint(ComparingLiteralsHintFactory.DEFAULT_VARIABLE_WITHOUT_INFORMATION);
        trueHint.setParameter(ComparingLiteralsHintFactory.HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo("true"));
        trueHint.setParameter(Hint.HINT_VARIABLE, "");
        trueHint.setParameter(ComparingLiteralsHintFactory.ADD_INFO_DICT, IssueTranslator.getInstance().getInfo(ComparingLiteralsHintFactory.ADD_INFO_DICT_RESOURCE));
        Hint falseHint = new Hint(ComparingLiteralsHintFactory.DEFAULT_VARIABLE_WITHOUT_INFORMATION);
        falseHint.setParameter(ComparingLiteralsHintFactory.HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo("false"));
        falseHint.setParameter(Hint.HINT_VARIABLE, "");
        falseHint.setParameter(ComparingLiteralsHintFactory.ADD_INFO_DICT, "");
        int i = 0;
        for (Issue issue : reports) {
            if (i == 1) {
                Truth.assertThat(issue.getHint()).isEqualTo(trueHint.getHintText());
            } else {
                Truth.assertThat(issue.getHint()).isEqualTo(falseHint.getHintText());
            }
            i++;
        }
    }

    @Test
    public void testComparingLiteralsNumbersGreater() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/compareNumbersGreater.json");
        ComparingLiterals finder = new ComparingLiterals();
        Set<Issue> reports = finder.check(program);
        Truth.assertThat(reports).hasSize(1);
        Hint trueHint = new Hint(ComparingLiteralsHintFactory.DEFAULT_TRUE);
        trueHint.setParameter(ComparingLiteralsHintFactory.ALWAYS_NEVER, IssueTranslator.getInstance().getInfo("always"));
        trueHint.setParameter(Hint.THEN_ELSE, IssueTranslator.getInstance().getInfo("then"));
        for (Issue issue : reports) {
            Truth.assertThat(issue.getHint()).isEqualTo(trueHint.getHintText());
        }
    }

    @Test
    public void testComparingLiteralsWait() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/comparingLiteralsWait.json");
        ComparingLiterals finder = new ComparingLiterals();
        Set<Issue> reports = finder.check(program);
        Truth.assertThat(reports).hasSize(1);
        Hint trueHint = new Hint(ComparingLiteralsHintFactory.DEFAULT_WITHOUT_INFORMATION);
        trueHint.setParameter(ComparingLiteralsHintFactory.HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo("true"));
        trueHint.setParameter(ComparingLiteralsHintFactory.ADD_INFO_DICT, IssueTranslator.getInstance().getInfo(ComparingLiteralsHintFactory.ADD_INFO_DICT_RESOURCE));
        for (Issue issue : reports) {
            Truth.assertThat(issue.getHint()).isEqualTo(trueHint.getHintText());
        }
    }

    @Test
    public void testComparingLiteralsVariableExists() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/comparingLiteralsVariableExists.json");
        ComparingLiterals finder = new ComparingLiterals();
        Set<Issue> reports = finder.check(program);
        Truth.assertThat(reports).hasSize(1);
        Hint falseHint = new Hint(ComparingLiteralsHintFactory.DEFAULT_VARIABLE_EXISTS);
        falseHint.setParameter(ComparingLiteralsHintFactory.ALWAYS_NEVER, IssueTranslator.getInstance().getInfo(ComparingLiteralsHintFactory.NEVER));
        falseHint.setParameter(Hint.HINT_VARIABLE, "test");
        falseHint.setParameter(Hint.THEN_ELSE, IssueTranslator.getInstance().getInfo("then"));
        falseHint.setParameter(ComparingLiteralsHintFactory.HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo("false"));
        falseHint.setParameter(ComparingLiteralsHintFactory.ADD_INFO_DICT, "");
        for (Issue issue : reports) {
            Truth.assertThat(issue.getHint()).isEqualTo(falseHint.getHintText());
        }
    }

    @Test
    public void testComparingLiteralsTwoStrings() throws IOException, ParsingException {
        assertThatFinderReports(1, new ComparingLiterals(), "src/test/fixtures/bugpattern/comparingLiteralsStrings.json");
    }

    @Test
    public void testSubsumptionByVariableAsLiteral() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/comparingLiteralsSubsumption.json");
        List<Issue> reportsVariableAsLiteral = new ArrayList<>((new VariableAsLiteral()).check(program));
        Assertions.assertEquals(2, reportsVariableAsLiteral.size());
        List<Issue> reportsComparingLiterals = new ArrayList<>((new ComparingLiterals()).check(program));
        Assertions.assertEquals(1, reportsComparingLiterals.size());
        Assertions.assertFalse(reportsComparingLiterals.get(0).isSubsumedBy(reportsVariableAsLiteral.get(0)));
        Assertions.assertTrue(reportsComparingLiterals.get(0).isSubsumedBy(reportsVariableAsLiteral.get(1)));
    }
}
