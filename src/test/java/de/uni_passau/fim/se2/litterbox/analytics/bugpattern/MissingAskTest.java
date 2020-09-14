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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class MissingAskTest {

    private Program loadProgram(String name) throws IOException, ParsingException {
        ObjectMapper mapper = new ObjectMapper();
        File f = new File(name);
        return ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = loadProgram("./src/test/fixtures/emptyProject.json");
        MissingAsk parameterName = new MissingAsk();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testAskBeforeAnswer() throws IOException, ParsingException {
        Program correctAsk = loadProgram("./src/test/fixtures/bugpattern/askBeforeAnswer.json");
        MissingAsk parameterName = new MissingAsk();
        Set<Issue> reports = parameterName.check(correctAsk);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMissingAsk() throws IOException, ParsingException {
        Program correctAsk = loadProgram("./src/test/fixtures/bugpattern/missingAsk.json");
        MissingAsk parameterName = new MissingAsk();
        Set<Issue> reports = parameterName.check(correctAsk);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testMissingAsks() throws IOException, ParsingException {
        Program correctAsk = loadProgram("./src/test/fixtures/bugpattern/missingAsks.json");
        MissingAsk parameterName = new MissingAsk();
        Set<Issue> reports = parameterName.check(correctAsk);
        Assertions.assertEquals(2, reports.size());
    }

}
