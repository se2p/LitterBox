/*
 * Copyright (C) 2020 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

class ComparingLiteralsTest implements JsonTest {

    @Test
    public void testComparingLiterals() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/comparingLiterals.json");
        ComparingLiterals finder = new ComparingLiterals();
        Set<Issue> reports = finder.check(program);
        Truth.assertThat(reports).hasSize(3);
        int i = 0;
        for (Issue issue : reports) {
            if (i == 1) {
                Truth.assertThat(issue.getHint()).isEqualTo("Reporter blocks are used to evaluate the truth value of certain expressions. If you compare two literals, the result will always be the same, TRUE. The blocks in the control body will be executed always or never. Therefore comparison is not necessary.");
            } else {
                Truth.assertThat(issue.getHint()).isEqualTo("Reporter blocks are used to evaluate the truth value of certain expressions. If you compare two literals, the result will always be the same, FALSE. The blocks in the control body will be executed always or never. Therefore comparison is not necessary.");
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

        for (Issue issue : reports) {
            Truth.assertThat(issue.getHint()).isEqualTo("Reporter blocks are used to evaluate the truth value of certain expressions. If you compare two literals, the result will always be the same, TRUE. The blocks in the control body will be executed always or never. Therefore comparison is not necessary.");
        }
    }
}
