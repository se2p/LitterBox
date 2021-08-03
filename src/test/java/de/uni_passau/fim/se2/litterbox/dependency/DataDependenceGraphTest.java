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
package de.uni_passau.fim.se2.litterbox.dependency;

import com.google.common.graph.EndpointPair;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class DataDependenceGraphTest  implements JsonTest {

    @Test
    public void testGreenflag() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/greenflag.json");
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);

        assertThat(ddg.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(ddg.getNumEdges()).isEqualTo(0);
    }

    @Test
    public void testRepeatUntil() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/repeatuntil.json");
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);

        assertThat(ddg.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(ddg.getNumEdges()).isEqualTo(1);

        CFGNode move = ddg.getNodes().stream().filter(t -> t.getASTNode() instanceof MoveSteps).findFirst().get();
        assertThat(ddg.getEdges()).containsExactly(EndpointPair.ordered(move, move));
    }

    @Test
    public void testIfThen() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/ifthen.json");
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);

        assertThat(ddg.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(ddg.getNumEdges()).isEqualTo(0);
    }

    @Test
    public void testIfElse() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/ifelse.json");
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);

        assertThat(ddg.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(ddg.getNumEdges()).isEqualTo(0);
    }

    @Test
    public void testTwoEvents() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/twoevents.json");
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);

        System.out.println(cfg.toDotString());

        assertThat(ddg.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(ddg.getNumEdges()).isEqualTo(0);
    }

    @Test
    public void testReachingDefinitionsInClone() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/variables.json");
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);

        assertThat(ddg.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(ddg.getNumEdges()).isEqualTo(2);

        CFGNode setVariable = ddg.getNodes().stream().filter(t -> t.getASTNode() instanceof SetVariableTo).findFirst().get();
        CFGNode changeVariable = ddg.getNodes().stream().filter(t -> t.getASTNode() instanceof ChangeVariableBy).findFirst().get();
        CFGNode ifThen = ddg.getNodes().stream().filter(t -> t.getASTNode() instanceof IfThenStmt).findFirst().get();
        assertThat(ddg.getEdges()).contains(EndpointPair.ordered(setVariable, changeVariable));
        assertThat(ddg.getEdges()).contains(EndpointPair.ordered(setVariable, ifThen));
    }

    @Test
    public void testNoDefInIf() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/defuseinif.json");
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);

        assertThat(ddg.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(ddg.getNumEdges()).isEqualTo(3);

        CFGNode setVariable = ddg.getNodes().stream().filter(t -> t.getASTNode() instanceof SetVariableTo).findFirst().get();
        CFGNode changeVariable = ddg.getNodes().stream().filter(t -> t.getASTNode() instanceof ChangeVariableBy).findFirst().get();
        CFGNode ifThen = ddg.getNodes().stream().filter(t -> t.getASTNode() instanceof IfThenStmt).findFirst().get();
        CFGNode say = ddg.getNodes().stream().filter(t -> t.getASTNode() instanceof SayForSecs).findFirst().get();
        assertThat(ddg.getEdges()).contains(EndpointPair.ordered(setVariable, changeVariable));
        assertThat(ddg.getEdges()).contains(EndpointPair.ordered(setVariable, ifThen));
        assertThat(ddg.getEdges()).contains(EndpointPair.ordered(setVariable, say));
    }
}
