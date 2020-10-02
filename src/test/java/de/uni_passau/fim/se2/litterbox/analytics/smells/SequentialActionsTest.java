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

package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

// All these tests make the assumption that a sequence has to consist of at least 2 statements, and must occur at least 3 times

public class SequentialActionsTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/emptyProject.json");

        SequentialActions parameterName = new SequentialActions();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testThreeTimesTwoStatements() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/sequenceOfThreePairs.json");
        SequentialActions parameterName = new SequentialActions();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testThreeTimesThreeStatements() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/sequenceOfThreeTriples.json");

        SequentialActions parameterName = new SequentialActions();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testFourTimesTwoStatements() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/sequenceOfFourPairs.json");

        SequentialActions parameterName = new SequentialActions();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testSequenceInLoop() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/sequenceInLoop.json");

        SequentialActions parameterName = new SequentialActions();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testSequenceWithNestedIf() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/sequenceWithNestedIf.json");

        SequentialActions parameterName = new SequentialActions();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testSequenceWithIfs() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/sequenceWithIfs.json");

        SequentialActions parameterName = new SequentialActions();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testSequenceWithInterruption() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/sequenceWithInterruption.json");

        SequentialActions parameterName = new SequentialActions();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testSequenceWithPrefix() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/sequenceWithPrefix.json");

        SequentialActions parameterName = new SequentialActions();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testSequenceWithPostfix() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/sequenceWithPostfix.json");

        SequentialActions parameterName = new SequentialActions();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(1, reports.size());
    }
}
