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
package de.uni_passau.fim.se2.litterbox.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.utils.BlockCount;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BlockCountTest {
    private static Program empty;
    private static Program nestedLoops;
    private static Program withproc;
    private static Program fixedStatements;
    private static Program fixedExpressions;
    private static Program halfFixedExpr;
    private static Program onlyVariable;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/nestedLoops.json");
        nestedLoops = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/blockCountWithProc.json");
        withproc = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/fixedExpressions.json");
        fixedExpressions = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/stmtParser/allFixedStatements.json");
        fixedStatements = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/halfFixedExpressions.json");
        halfFixedExpr = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/onlyVariable.json");
        onlyVariable = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        BlockCount parameterName = new BlockCount();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testBlockCountNested() {
        BlockCount parameterName = new BlockCount();
        IssueReport report = parameterName.check(nestedLoops);
        Assertions.assertEquals(14, report.getCount());
    }

    @Test
    public void testBlockproc() {
        BlockCount parameterName = new BlockCount();
        IssueReport report = parameterName.check(withproc);
        Assertions.assertEquals(18, report.getCount());
    }

    @Test
    public void testFixedStatements() {
        BlockCount parameterName = new BlockCount();
        IssueReport report = parameterName.check(fixedStatements);
        Assertions.assertEquals(26, report.getCount());
    }

    @Test
    public void testFixedExpr() {
        BlockCount parameterName = new BlockCount();
        IssueReport report = parameterName.check(fixedExpressions);
        Assertions.assertEquals(4, report.getCount());
    }

    @Test
    public void testOnlyVariable() {
        BlockCount parameterName = new BlockCount();
        IssueReport report = parameterName.check(onlyVariable);
        Assertions.assertEquals(1, report.getCount());
    }

    @Test
    public void testHalfFixedExpr() {
        BlockCount parameterName = new BlockCount();
        IssueReport report = parameterName.check(halfFixedExpr);
        Assertions.assertEquals(5, report.getCount()); //TODO does an empty string have to be an UnspecifiedExpr?
    }
}
