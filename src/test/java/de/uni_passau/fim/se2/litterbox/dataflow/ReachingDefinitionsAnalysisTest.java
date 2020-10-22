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
import de.uni_passau.fim.se2.litterbox.cfg.Definition;
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
        assertThat(analysis.getDataflowFacts(cloneNode)).containsExactly(firstDefinition, secondDefinition);
        assertThat(analysis.getDataflowFacts(sayNode)).containsExactly(firstDefinition, secondDefinition);
        assertThat(analysis.getDataflowFacts(exitNode)).containsExactly(firstDefinition, secondDefinition);
    }
}
