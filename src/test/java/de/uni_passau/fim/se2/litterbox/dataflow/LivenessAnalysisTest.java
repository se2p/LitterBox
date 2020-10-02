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
import de.uni_passau.fim.se2.litterbox.cfg.Use;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class LivenessAnalysisTest implements JsonTest {

    @Test
    public void testLivenessNotOverwritten() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/defsayuse.json");
        DataflowAnalysisBuilder<Use> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Use> analysis = builder.withBackward().withMay().withTransferFunction(new LivenessTransferFunction()).build();
        analysis.applyAnalysis();

        CFGNode entryNode = cfg.getEntryNode();
        CFGNode exitNode = cfg.getExitNode();
        CFGNode sayNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();
        CFGNode setNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetVariableTo).findFirst().get();

        Use theUse = sayNode.getUses().iterator().next(); // Exactly one use

        assertThat(analysis.getDataflowFacts(entryNode)).isEmpty();
        assertThat(analysis.getDataflowFacts(setNode)).containsExactly(theUse);
        assertThat(analysis.getDataflowFacts(sayNode)).containsExactly();
        assertThat(analysis.getDataflowFacts(exitNode)).containsExactly();
    }

    @Test
    public void testMultipleUses() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/variabletwouses.json");
        DataflowAnalysisBuilder<Use> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Use> analysis = builder.withBackward().withMay().withTransferFunction(new LivenessTransferFunction()).build();
        analysis.applyAnalysis();

        CFGNode entryNode = cfg.getEntryNode();
        CFGNode exitNode = cfg.getExitNode();
        CFGNode setNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetVariableTo).findFirst().get();
        CFGNode sayNode1 = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();
        CFGNode sayNode2 = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).filter(n -> n != sayNode1).findFirst().get();

        Use firstUse = sayNode1.getUses().iterator().next();
        Use secondUse = sayNode2.getUses().iterator().next();

        assertThat(analysis.getDataflowFacts(entryNode)).isEmpty();
        assertThat(analysis.getDataflowFacts(setNode)).containsExactly(firstUse, secondUse);
        assertThat(analysis.getDataflowFacts(sayNode1)).containsExactly(secondUse);
        assertThat(analysis.getDataflowFacts(sayNode2)).isEmpty();
        assertThat(analysis.getDataflowFacts(exitNode)).isEmpty();
    }

    @Test
    public void testMultipleUsesInBranch() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dataflow/setifchangesay.json");
        DataflowAnalysisBuilder<Use> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Use> analysis = builder.withBackward().withMay().withTransferFunction(new LivenessTransferFunction()).build();
        analysis.applyAnalysis();

        CFGNode entryNode = cfg.getEntryNode();
        CFGNode exitNode = cfg.getExitNode();
        CFGNode setNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetVariableTo).findFirst().get();
        CFGNode changeNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof ChangeVariableBy).findFirst().get();
        CFGNode ifNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof IfThenStmt).findFirst().get();
        CFGNode sayNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();

        Use firstUse = ifNode.getUses().iterator().next();
        Use secondUse = changeNode.getUses().iterator().next();
        Use thirdUse = sayNode.getUses().iterator().next();

        assertThat(analysis.getDataflowFacts(entryNode)).isEmpty();
        assertThat(analysis.getDataflowFacts(setNode)).containsExactly(firstUse, secondUse, thirdUse);
        assertThat(analysis.getDataflowFacts(ifNode)).containsExactly(secondUse, thirdUse);
        assertThat(analysis.getDataflowFacts(changeNode)).containsExactly(thirdUse);
        assertThat(analysis.getDataflowFacts(sayNode)).containsExactly();
        assertThat(analysis.getDataflowFacts(exitNode)).containsExactly();
    }

    @Test
    public void testMultipleUsesInClone() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/variables.json");
        DataflowAnalysisBuilder<Use> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Use> analysis = builder.withBackward().withMay().withTransferFunction(new LivenessTransferFunction()).build();
        analysis.applyAnalysis();

        CFGNode entryNode = cfg.getEntryNode();
        CFGNode exitNode = cfg.getExitNode();
        CFGNode setNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetVariableTo).findFirst().get();
        CFGNode changeNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof ChangeVariableBy).findFirst().get();
        CFGNode ifNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof IfThenStmt).findFirst().get();
        CFGNode cloneNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof CreateCloneOf).findFirst().get();
        CFGNode sayNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();

        Use firstUse = ifNode.getUses().iterator().next(); // var1
        Use secondUse = changeNode.getUses().iterator().next(); // var1
        Use thirdUse = sayNode.getUses().iterator().next(); // var2

        assertThat(analysis.getDataflowFacts(entryNode)).containsExactly(thirdUse);
        assertThat(analysis.getDataflowFacts(setNode)).containsExactly(firstUse, secondUse, thirdUse);
        assertThat(analysis.getDataflowFacts(ifNode)).containsExactly(secondUse, thirdUse);
        assertThat(analysis.getDataflowFacts(changeNode)).containsExactly(thirdUse);
        assertThat(analysis.getDataflowFacts(cloneNode)).containsExactly(thirdUse);
        assertThat(analysis.getDataflowFacts(sayNode)).containsExactly();
        assertThat(analysis.getDataflowFacts(exitNode)).containsExactly();
    }
}
