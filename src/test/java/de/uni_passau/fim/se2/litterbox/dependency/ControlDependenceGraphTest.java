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
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

public class ControlDependenceGraphTest implements JsonTest {

    @Test
    public void testGreenflag() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/greenflag.json");
        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);

        assertThat(cdg.getNodes()).isEqualTo(cfg.getNodes());

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = cfg.getSuccessors(entry).iterator().next();
        CFGNode move = cfg.getSuccessors(greenFlag).stream().filter(t -> t.getASTNode() instanceof MoveSteps).findFirst().get();
        CFGNode exit = cfg.getExitNode();

        assertThat(cdg.getNumEdges()).isEqualTo(3);
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(entry, exit));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(entry, greenFlag));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(greenFlag, move));
    }


    @Test
    public void testRepeatUntil() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/repeatuntil.json");
        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);

        Set<CFGNode> nodes = cdg.getNodes();
        assertThat(nodes).isEqualTo(cfg.getNodes());
        assertThat(cdg.getNumEdges()).isEqualTo(5);

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = cfg.getSuccessors(entry).iterator().next();
        CFGNode until = cfg.getSuccessors(greenFlag).stream().filter(t -> t.getASTNode() instanceof UntilStmt).findFirst().get();
        CFGNode move = cfg.getSuccessors(until).stream().filter(t -> t.getASTNode() instanceof MoveSteps).findFirst().get();
        CFGNode exit = cfg.getExitNode();

        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(entry, exit));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(entry, greenFlag));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(greenFlag, until));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(until, move));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(until, until));
    }


    @Test
    public void testIfThen() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/ifthen.json");
        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);

        assertThat(cdg.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(cdg.getNumEdges()).isEqualTo(4);

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = cfg.getSuccessors(entry).iterator().next();
        CFGNode ifthen = cfg.getSuccessors(greenFlag).stream().filter(t -> t.getASTNode() instanceof IfThenStmt).findFirst().get();
        CFGNode move = cfg.getSuccessors(ifthen).stream().filter(t -> t.getASTNode() instanceof MoveSteps).findFirst().get();
        CFGNode exit = cfg.getExitNode();

        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(entry, exit));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(entry, greenFlag));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(greenFlag, ifthen));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(ifthen, move));
    }

    @Test
    public void testIfElse() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/ifelse.json");
        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);

        assertThat(cdg.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(cdg.getNumEdges()).isEqualTo(5);

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = cfg.getSuccessors(entry).iterator().next();
        CFGNode ifelse = cfg.getSuccessors(greenFlag).stream().filter(t -> t.getASTNode() instanceof IfElseStmt).findFirst().get();
        CFGNode move1 = cfg.getSuccessors(ifelse).stream().filter(t -> t.getASTNode() instanceof MoveSteps).collect(Collectors.toList()).get(0);
        CFGNode move2 = cfg.getSuccessors(ifelse).stream().filter(t -> t.getASTNode() instanceof MoveSteps).collect(Collectors.toList()).get(1);
        CFGNode exit = cfg.getExitNode();

        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(entry, exit));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(entry, greenFlag));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(greenFlag, ifelse));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(ifelse, move1));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(ifelse, move2));
    }

    @Test
    public void testTwoEvents() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/twoevents.json");
        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);

        assertThat(cdg.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(cdg.getNumEdges()).isEqualTo(5);

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = cfg.getSuccessors(entry).stream().filter(t -> t.getASTNode() instanceof GreenFlag).findFirst().get();
        CFGNode move1 = cfg.getSuccessors(greenFlag).stream().filter(t -> t.getASTNode() instanceof MoveSteps).findFirst().get();
        CFGNode keyPressed = cfg.getSuccessors(entry).stream().filter(t -> t.getASTNode() instanceof KeyPressed).collect(Collectors.toList()).get(0);
        CFGNode move2 = cfg.getSuccessors(keyPressed).stream().filter(t -> t.getASTNode() instanceof MoveSteps).findFirst().get();
        CFGNode exit = cfg.getExitNode();

        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(entry, exit));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(entry, greenFlag));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(entry, keyPressed));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(greenFlag, move1));
        assertThat(cdg.getEdges()).contains(EndpointPair.ordered(keyPressed, move2));
    }
}
