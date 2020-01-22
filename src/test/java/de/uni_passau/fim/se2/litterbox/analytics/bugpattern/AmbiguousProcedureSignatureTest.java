/*
 * Copyright (C) 2019 LitterBox contributors
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
import java.io.File;
import java.io.IOException;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;

public class AmbiguousProcedureSignatureTest {
    private static Program empty;
    private static Program ambiguousProcedure;
    private static Program ambiguousProcedureDiffArg;
    private static Program emptySign;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/ambiguousProcedureSignature.json");
        ambiguousProcedure = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/ambiguousSignatureDiffArg.json");
        ambiguousProcedureDiffArg = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/emptyAmbiguousSign.json");
       emptySign = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        AmbiguousProcedureSignature parameterName = new AmbiguousProcedureSignature();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testAmbiguousSignatures() {
        AmbiguousProcedureSignature parameterName = new AmbiguousProcedureSignature();
        IssueReport report = parameterName.check(ambiguousProcedure);
        Assertions.assertEquals(2, report.getCount());
    }

    @Test
    public void testAmbiguousSigDifferentParameters() {
        AmbiguousProcedureSignature parameterName = new AmbiguousProcedureSignature();
        IssueReport report = parameterName.check(ambiguousProcedureDiffArg);
        Assertions.assertEquals(2, report.getCount());
    }

    @Test
    public void testAmbiguousEmpty() {
        AmbiguousProcedureSignature parameterName = new AmbiguousProcedureSignature();
        IssueReport report = parameterName.check(emptySign);
        Assertions.assertEquals(0, report.getCount());
    }
}
