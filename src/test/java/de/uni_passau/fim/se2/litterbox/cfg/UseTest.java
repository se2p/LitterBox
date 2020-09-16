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
package de.uni_passau.fim.se2.litterbox.cfg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.AttributeAboveValue;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        Set<Variable> uses = visitor.getDefineables();
        assertThat(uses).hasSize(1);
    }

    @Test
    public void testDefinitionIsNotAUse() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/onedef.json");
        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetVariableTo).findFirst().get();
        VariableUseVisitor visitor = new VariableUseVisitor();
        node.getASTNode().accept(visitor);

        Set<Variable> uses = visitor.getDefineables();
        assertThat(uses).hasSize(0);
    }

    @Test
    public void testUseAndDefinition() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/defuse.json");
        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof ChangeVariableBy).findFirst().get();
        VariableUseVisitor visitor = new VariableUseVisitor();
        node.getASTNode().accept(visitor);

        Set<Variable> uses = visitor.getDefineables();
        assertThat(uses).hasSize(1);
    }

    @Test
    public void testVariableReferenceIsAUse() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/variableref.json");
        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();
        VariableUseVisitor visitor = new VariableUseVisitor();
        node.getASTNode().accept(visitor);

        Set<Variable> uses = visitor.getDefineables();
        assertThat(uses).hasSize(1);
    }

    @Test
    public void testVariableUsedInAttributeOf() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/bugpattern/missingVariableInitializationVariableOf.json");
        List<CFGNode> nodes = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).collect(Collectors.toList());
        VariableUseVisitor visitor = new VariableUseVisitor();
        nodes.get(0).getASTNode().accept(visitor);

        Set<Variable> uses = visitor.getDefineables();
        assertThat(uses).hasSize(1);

        visitor = new VariableUseVisitor();
        nodes.get(1).getASTNode().accept(visitor);

        uses = visitor.getDefineables();
        assertThat(uses).hasSize(1);
    }

    @Test
    public void testVariablesDefSayUse() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/defsayuse.json");

        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();
        VariableUseVisitor visitor = new VariableUseVisitor();
        node.getASTNode().accept(visitor);
        Set<Variable> uses = visitor.getDefineables();
        assertThat(uses).hasSize(1);

        node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetVariableTo).findFirst().get();
        visitor = new VariableUseVisitor();
        node.getASTNode().accept(visitor);
        uses = visitor.getDefineables();
        assertThat(uses).hasSize(0);
    }

    @Test
    public void testUseInIf() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/defuseinif.json");

        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetVariableTo).findFirst().get();
        Defineable var = node.getDefinitions().iterator().next().getDefinable();

        node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof IfThenStmt).findFirst().get();
        assertThat(getUses(node)).containsExactly(var);

        node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof ChangeVariableBy).findFirst().get();
        assertThat(getUses(node)).containsExactly(var);
        node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();
        assertThat(getUses(node)).containsExactly(var);

        node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof MoveSteps).findFirst().get();
        assertThat(getUses(node)).isEmpty();
    }

    @Test
    public void testUseOfOtherSprite() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/uselocalvarfromothersprite.json");

        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetVariableTo).findFirst().get();
        Defineable var = node.getDefinitions().iterator().next().getDefinable();

        node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();
        assertThat(getUses(node)).containsExactly(var);
    }

    @Test
    public void testAttributeOfVariable() throws IOException, ParsingException {
        // If the dropdown contains a variable or parameter we don't statically know what sprite
        // we're referring to, so for now we skip these definitions/uses...
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/nouseattributewithvariable.json");
        List<CFGNode> nodes = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).collect(Collectors.toList());
        for (CFGNode node : nodes) {
            assertThat(node.getDefinitions()).isEmpty();
            assertThat(node.getUses()).isEmpty();
        }
    }

    @Test
    public void testTimerUses() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/timerBlock.json");
        List<CFGNode> nodes = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof AttributeAboveValue).collect(Collectors.toList());
        assertThat(nodes).hasSize(1);
        CFGNode node = nodes.iterator().next();

        assertThat(node.getUses()).hasSize(1);
        assertThat(node.getDefinitions()).isEmpty();
    }

    @Test
    public void testUseOfVariablesInDifferentScopes() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/variablescopes.json");

        // In this test there are two sprites
        // Sprite 1:
        //   Says a global variable
        //   Thinks a variable local to Sprite 1
        //   Waits for a variable local to Sprite 2
        // Sprite 2:
        //   Says a global variable
        //   Thinks a variable local to Sprite 1
        //   Waits for a variable local to Sprite 2
        //
        // The resulting variables extracted need to be equal

        // Sprite 1
        CFGNode sayGlobalVariable1 = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();
        assertThat(getUses(sayGlobalVariable1)).hasSize(1);
        Defineable globalVar1 = sayGlobalVariable1.getUses().iterator().next().getDefinable();
        assertThat(globalVar1).isInstanceOf(Variable.class);

        CFGNode thinkLocalVariable1 = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof ThinkForSecs).findFirst().get();
        assertThat(getUses(thinkLocalVariable1)).hasSize(1);
        Defineable localVar1 = thinkLocalVariable1.getUses().iterator().next().getDefinable();
        assertThat(localVar1).isInstanceOf(Variable.class);

        CFGNode waitOtherVariable1 = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof WaitUntil).findFirst().get();
        assertThat(getUses(waitOtherVariable1)).hasSize(1);
        Defineable waitOtherVar1 = waitOtherVariable1.getUses().iterator().next().getDefinable();
        assertThat(waitOtherVar1).isInstanceOf(Variable.class);

        assertThat(globalVar1).isNotEqualTo(localVar1);
        assertThat(globalVar1).isNotEqualTo(waitOtherVar1);

        // Sprite 2
        CFGNode sayGlobalVariable2 = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof Say).findFirst().get();
        assertThat(getUses(sayGlobalVariable2)).hasSize(1);
        Defineable globalVar2 = sayGlobalVariable1.getUses().iterator().next().getDefinable();
        assertThat(globalVar2).isInstanceOf(Variable.class);

        CFGNode thinkLocalVariable2 = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof Think).findFirst().get();
        assertThat(getUses(thinkLocalVariable2)).hasSize(1);
        Defineable localVar2 = thinkLocalVariable2.getUses().iterator().next().getDefinable();
        assertThat(localVar2).isInstanceOf(Variable.class);

        CFGNode waitOtherVariable2 = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof WaitSeconds).findFirst().get();
        assertThat(getUses(waitOtherVariable2)).hasSize(1);
        Defineable waitOtherVar2 = waitOtherVariable2.getUses().iterator().next().getDefinable();
        assertThat(waitOtherVar2).isInstanceOf(Variable.class);

        assertThat(globalVar1).isEqualTo(globalVar2);
        assertThat(localVar1).isEqualTo(localVar2);
        assertThat(waitOtherVar1).isEqualTo(waitOtherVar2);
    }

    private Set<Variable> getUses(CFGNode node) {
        VariableUseVisitor visitor = new VariableUseVisitor();
        node.getASTNode().accept(visitor);
        return visitor.getDefineables();
    }
}
