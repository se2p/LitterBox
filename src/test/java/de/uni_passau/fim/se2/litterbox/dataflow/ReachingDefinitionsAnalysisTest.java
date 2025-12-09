/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.dataflow;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.Parameter;
import de.uni_passau.fim.se2.litterbox.cfg.Definition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class ReachingDefinitionsAnalysisTest implements JsonTest {

    @Test
    public void testReachingDefinition() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/defsayuse.json");

        DataflowAnalysisBuilder<Definition> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Definition> analysis = builder.withForward().withMay().withTransferFunction(new ReachingDefinitionsTransferFunction()).build();
        analysis.applyAnalysis();

        CFGNode entryNode = cfg.getEntryNode();
        CFGNode exitNode = cfg.getExitNode();
        CFGNode sayNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();
        CFGNode setNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetVariableTo).findFirst().get();

        Definition theDefinition = setNode.getDefinitions().iterator().next(); // Exactly one definition

        assertThat(analysis.getDataflowFacts(entryNode)).isEmpty();
        assertThat(analysis.getDataflowFacts(setNode)).isEmpty();
        assertThat(analysis.getDataflowFacts(sayNode)).containsExactly(theDefinition);
        assertThat(analysis.getDataflowFacts(exitNode)).containsExactly(theDefinition);
    }

    @Test
    public void testReachingDefinitions() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/setifchangesay.json");

        DataflowAnalysisBuilder<Definition> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Definition> analysis = builder.withForward().withMay().withTransferFunction(new ReachingDefinitionsTransferFunction()).build();
        analysis.applyAnalysis();

        CFGNode entryNode = cfg.getEntryNode();
        CFGNode exitNode = cfg.getExitNode();
        CFGNode setNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetVariableTo).findFirst().get();
        CFGNode changeNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof ChangeVariableBy).findFirst().get();
        CFGNode ifNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof IfThenStmt).findFirst().get();
        CFGNode sayNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();

        Definition firstDefinition = setNode.getDefinitions().iterator().next();
        Definition secondDefinition = changeNode.getDefinitions().iterator().next();

        assertThat(analysis.getDataflowFacts(entryNode)).isEmpty();
        assertThat(analysis.getDataflowFacts(setNode)).containsExactly();
        assertThat(analysis.getDataflowFacts(ifNode)).containsExactly(firstDefinition);
        assertThat(analysis.getDataflowFacts(changeNode)).containsExactly(firstDefinition);
        assertThat(analysis.getDataflowFacts(sayNode)).containsExactly(firstDefinition, secondDefinition);
        assertThat(analysis.getDataflowFacts(exitNode)).containsExactly(firstDefinition, secondDefinition);
    }

    @Test
    public void testReachingDefinitionsInClone() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/variables.json");

        DataflowAnalysisBuilder<Definition> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Definition> analysis = builder.withForward().withMay().withTransferFunction(new ReachingDefinitionsTransferFunction()).build();
        analysis.applyAnalysis();

        CFGNode entryNode = cfg.getEntryNode();
        CFGNode exitNode = cfg.getExitNode();
        CFGNode setNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetVariableTo).findFirst().get();
        CFGNode changeNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof ChangeVariableBy).findFirst().get();
        CFGNode ifNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof IfThenStmt).findFirst().get();
        CFGNode cloneNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof CreateCloneOf).findFirst().get();
        CFGNode sayNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();

        Definition firstDefinition = setNode.getDefinitions().iterator().next();
        Definition secondDefinition = changeNode.getDefinitions().iterator().next();

        assertThat(analysis.getDataflowFacts(entryNode)).isEmpty();
        assertThat(analysis.getDataflowFacts(setNode)).containsExactly();
        assertThat(analysis.getDataflowFacts(ifNode)).containsExactly(firstDefinition);
        assertThat(analysis.getDataflowFacts(changeNode)).containsExactly(firstDefinition);
        assertThat(analysis.getDataflowFacts(cloneNode)).containsExactly(secondDefinition);
        assertThat(analysis.getDataflowFacts(sayNode)).containsExactly(secondDefinition);
        assertThat(analysis.getDataflowFacts(exitNode)).containsExactly(firstDefinition, secondDefinition);
    }

    @Test
    public void testReachingDefinitionsInCustomBlock() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String scratchBlocks = """
                define myBlock (param)
                move (param) steps
                set [param v] to (0)
                move (param) steps
                
                when green flag clicked
                myBlock (10)
                """;
        program = parser.extendProject(program, "Sprite1", scratchBlocks);

        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        visitor.visit(program);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();

        DataflowAnalysisBuilder<Definition> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Definition> analysis = builder.withForward().withMay().withTransferFunction(new ReachingDefinitionsTransferFunction()).build();
        analysis.applyAnalysis();

                
        List<CFGNode> moveNodes = cfg.getNodes().stream()
                .filter(n -> n.getASTNode() instanceof MoveSteps)
                .collect(Collectors.toList());
        
        CFGNode setNode = cfg.getNodes().stream()
                .filter(n -> n.getASTNode() instanceof SetVariableTo)
                .findFirst().orElseThrow();
                
        CFGNode procDefNode = cfg.getNodes().stream()
                .filter(n -> n.getASTNode() instanceof ProcedureDefinition)
                .findFirst().orElseThrow();
        

        
        Definition paramDef = procDefNode.getDefinitions().stream()
                .filter(d -> d.getDefinable() instanceof Parameter p && p.getIdentifier().getName().equals("param"))
                .findFirst().orElseThrow(() -> new AssertionError("Parameter definition not found"));
        
        Definition setDef = setNode.getDefinitions().iterator().next();
        
        boolean foundFirst = false;
        boolean foundSecond = false;
        
        for (CFGNode moveNode : moveNodes) {
            Set<Definition> reaching = analysis.getDataflowFacts(moveNode);
            
            if (reaching.contains(paramDef) && !reaching.contains(setDef)) {
                foundFirst = true;
            } else if (reaching.contains(setDef) && reaching.contains(paramDef)) {
                foundSecond = true;
            }
        }
        
        assertThat(foundFirst).isTrue();
        assertThat(foundSecond).isTrue();
    }

    @Test
    public void testReachingDefinitionsInCustomBlockWithTwoParameters() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        String scratchBlocks = """
                define myBlock (param1) (param2)
                move (param1) steps
                move (param2) steps
                
                when green flag clicked
                myBlock (10) (20)
                """;
        program = parser.extendProject(program, "Sprite1", scratchBlocks);

        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        visitor.visit(program);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();

        DataflowAnalysisBuilder<Definition> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Definition> analysis = builder.withForward().withMay().withTransferFunction(new ReachingDefinitionsTransferFunction()).build();
        analysis.applyAnalysis();

        List<CFGNode> moveNodes = cfg.getNodes().stream()
                .filter(n -> n.getASTNode() instanceof MoveSteps)
                .collect(Collectors.toList());
        
        CFGNode procDefNode = cfg.getNodes().stream()
                .filter(n -> n.getASTNode() instanceof ProcedureDefinition)
                .findFirst().orElseThrow();
        
        Definition param1Def = procDefNode.getDefinitions().stream()
                .filter(d -> d.getDefinable() instanceof Parameter p && p.getIdentifier().getName().equals("param1"))
                .findFirst().orElseThrow(() -> new AssertionError("Parameter param1 definition not found"));
        
        Definition param2Def = procDefNode.getDefinitions().stream()
                .filter(d -> d.getDefinable() instanceof Parameter p && p.getIdentifier().getName().equals("param2"))
                .findFirst().orElseThrow(() -> new AssertionError("Parameter param2 definition not found"));

        // First move steps uses param1
        CFGNode firstMove = moveNodes.get(0);
        Set<Definition> reachingFirst = analysis.getDataflowFacts(firstMove);
        assertThat(reachingFirst).contains(param1Def);
        assertThat(reachingFirst).contains(param2Def); // param2 is also defined at the start

        // Second move steps uses param2
        CFGNode secondMove = moveNodes.get(1);
        Set<Definition> reachingSecond = analysis.getDataflowFacts(secondMove);
        assertThat(reachingSecond).contains(param1Def);
        assertThat(reachingSecond).contains(param2Def);
    }
}
