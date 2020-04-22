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

package de.uni_passau.fim.se2.litterbox.cfg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class UseTest {

    private Program getAST(String fileName) throws IOException, ParsingException {
        File file = new File(fileName);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode project = objectMapper.readTree(file);
        Program program = ProgramParser.parseProgram("TestProgram", project);
        return program;
    }

    private ControlFlowGraph getCFG(String fileName) throws IOException, ParsingException {
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        visitor.visit(getAST(fileName));
        return visitor.getControlFlowGraph();
    }

    @Test
    public void testSingleUse() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/oneuse.json");
        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof ShowVariable).findFirst().get();
        VariableUseVisitor visitor = new VariableUseVisitor();
        node.getASTNode().accept(visitor);
        Set<Qualified> uses = visitor.getUses();
        assertThat(uses).hasSize(1);
    }

    @Test
    public void testDefinitionIsNotAUse() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/onedef.json");
        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetVariableTo).findFirst().get();
        VariableUseVisitor visitor = new VariableUseVisitor();
        node.getASTNode().accept(visitor);

        Set<Qualified> uses = visitor.getUses();
        assertThat(uses).hasSize(0);
    }

    @Test
    public void testUseAndDefinition() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/defuse.json");
        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof ChangeVariableBy).findFirst().get();
        VariableUseVisitor visitor = new VariableUseVisitor();
        node.getASTNode().accept(visitor);

        Set<Qualified> uses = visitor.getUses();
        assertThat(uses).hasSize(1);
    }


    @Test
    public void testVariableReferenceIsAUse() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/variableref.json");
        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();
        VariableUseVisitor visitor = new VariableUseVisitor();
        node.getASTNode().accept(visitor);

        Set<Qualified> uses = visitor.getUses();
        assertThat(uses).hasSize(1);
    }

    @Test
    public void testVariablesDefSayUse() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/defsayuse.json");

        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();
        VariableUseVisitor visitor = new VariableUseVisitor();
        node.getASTNode().accept(visitor);
        Set<Qualified> uses = visitor.getUses();
        assertThat(uses).hasSize(1);

        node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetVariableTo).findFirst().get();
        visitor = new VariableUseVisitor();
        node.getASTNode().accept(visitor);
        uses = visitor.getUses();
        assertThat(uses).hasSize(0);
    }
}
