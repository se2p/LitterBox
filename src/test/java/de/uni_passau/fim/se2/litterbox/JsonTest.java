/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox;

import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.smells.EmptyCustomBlock;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.Set;

public interface JsonTest {

    default Program getAST(String fileName) throws IOException, ParsingException {
        Scratch3Parser parser = new Scratch3Parser();
        return parser.parseFile(fileName);
    }

    default ControlFlowGraph getCFG(String fileName) throws IOException, ParsingException {
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        visitor.visit(getAST(fileName));
        return visitor.getControlFlowGraph();
    }

    // TODO: This is a bit redundant wrt getAST (it is added for the tests that have a static test fixture)
    static Program parseProgram(String fileName) throws IOException, ParsingException {
        Scratch3Parser parser = new Scratch3Parser();
        return parser.parseFile(fileName);
    }

    default void assertThatFinderReports(int expectedIssues, IssueFinder finder, String filePath) throws IOException, ParsingException {
        Program prog = getAST(filePath);
        Set<Issue> reports = finder.check(prog);
        Assertions.assertEquals(expectedIssues, reports.size());
    }

    default void assertNumberActorDefinitions(int expectedActors, String filePath) throws IOException, ParsingException {
        Program prog = getAST(filePath);
        ActorDefinitionList list = prog.getActorDefinitionList();
        Truth.assertThat(list.getDefinitions().size()).isEqualTo(expectedActors);
    }
}
