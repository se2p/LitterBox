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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class AmbiguousParameterNameUnusedTest {
    private static Program empty;
    private static Program ambiguousParams;
    private static Program clans;
    private static Program realAmbiguousParam;
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/ambiguousParameters.json");
        ambiguousParams = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/clans.json");
        clans = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/realAmbiguousParameter.json");
        realAmbiguousParam = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        AmbiguousParameterNameUnused parameterName = new AmbiguousParameterNameUnused();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testAmbiguousParameters() {
        AmbiguousParameterNameUnused parameterName = new AmbiguousParameterNameUnused();
        Set<Issue> reports = parameterName.check(ambiguousParams);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testClans() {
        AmbiguousParameterNameUnused parameterName = new AmbiguousParameterNameUnused();
        Set<Issue> reports = parameterName.check(clans);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testReal() {
        AmbiguousParameterNameUnused parameterName = new AmbiguousParameterNameUnused();
        Set<Issue> reports = parameterName.check(realAmbiguousParam);
        Assertions.assertEquals(0, reports.size());
    }
}
