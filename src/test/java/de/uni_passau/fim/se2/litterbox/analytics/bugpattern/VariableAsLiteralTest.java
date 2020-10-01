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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VariableAsLiteralTest implements JsonTest {

    @Test
    public void testEmpty() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        Set<Issue> reports = (new VariableAsLiteral()).check(program);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testLiteralsInSayAndIf() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/variableAsLiteral.json");
        Set<Issue> reports = (new VariableAsLiteral()).check(program);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testListAsLiteral() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/listAsLiteral.json");
        Set<Issue> reports = (new VariableAsLiteral()).check(program);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testParameterAsLiteral() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/parameterAsLiteral.json");
        // 2 usages inside custom block, and 2 outside custom block
        Set<Issue> reports = (new VariableAsLiteral()).check(program);
        Assertions.assertEquals(4, reports.size());
    }

    @Test
    public void testActorsAsLiterals() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/actorsAsLiterals.json");
        Set<Issue> reports = (new VariableAsLiteral()).check(program);
        Assertions.assertEquals(0, reports.size());
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
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "if <[thelist] > (50)> then:: #ff0000 // Variable Used as Literal\n" +
                "say [thelist]\n" +
                "end\n" +
                "[/scratchblocks]\n", output);

        issue = issues.get(1);

        visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        visitor.setCurrentActor(issue.getActor());
        issue.getScriptOrProcedureDefinition().accept(visitor);
        visitor.end();
        output = visitor.getScratchBlocks();
        assertEquals("[scratchblocks]\n" +
                "when green flag clicked\n" +
                "if <[thelist] > (50)> then\n" +
                "say [thelist]:: #ff0000 // Variable Used as Literal\n" +
                "end\n" +
                "[/scratchblocks]\n", output);

    }

    @Test
    public void testBlocksThatShouldNotBeRecognisedAsStrings() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/smells/attributesThatAreNotStrings.json");
        Set<Issue> reports = (new VariableAsLiteral()).check(program);
        Assertions.assertEquals(0, reports.size());
    }
}
