/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

import static com.google.common.truth.Truth.assertThat;

public class DominatorTreeTest implements JsonTest {

    @Test
    public void testGreenflag() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/greenflag.json");
        DominatorTree dt = new DominatorTree(cfg);

        assertThat(dt.getNodes()).isEqualTo(cfg.getNodes());

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = cfg.getSuccessors(entry).iterator().next();
        CFGNode move = dt.getSuccessors(greenFlag).stream().filter(t -> t.getASTNode() instanceof MoveSteps).findFirst().get();
        CFGNode exit = cfg.getExitNode();

        assertThat(dt.getNumEdges()).isEqualTo(3); // Greenflag is conditional
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(entry, greenFlag));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(greenFlag, move));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(greenFlag, exit));
    }


    @Test
    public void testRepeatUntil() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/repeatuntil.json");
        DominatorTree dt = new DominatorTree(cfg);

        Set<CFGNode> nodes = dt.getNodes();
        assertThat(nodes).isEqualTo(cfg.getNodes());
        assertThat(dt.getNumEdges()).isEqualTo(4);

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = cfg.getSuccessors(entry).iterator().next();
        CFGNode until = dt.getSuccessors(greenFlag).stream().filter(t -> t.getASTNode() instanceof UntilStmt).findFirst().get();
        CFGNode move = dt.getSuccessors(until).iterator().next();
        CFGNode exit = cfg.getExitNode();

        assertThat(dt.getEdges()).contains(EndpointPair.ordered(entry, greenFlag));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(greenFlag, exit));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(greenFlag, until));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(until, move));
    }


    @Test
    public void testIfThen() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/ifthen.json");
        DominatorTree dt = new DominatorTree(cfg);

        assertThat(dt.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(dt.getNumEdges()).isEqualTo(4);

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = cfg.getSuccessors(entry).iterator().next();
        CFGNode ifthen = dt.getSuccessors(greenFlag).stream().filter(t -> t.getASTNode() instanceof IfThenStmt).findFirst().get();
        CFGNode move = dt.getSuccessors(ifthen).iterator().next();
        CFGNode exit = cfg.getExitNode();

        assertThat(dt.getEdges()).contains(EndpointPair.ordered(entry, greenFlag));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(greenFlag, exit));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(greenFlag, ifthen));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(ifthen, move));
    }

    @Test
    public void testIfElse() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/ifelse.json");
        DominatorTree dt = new DominatorTree(cfg);

        assertThat(dt.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(dt.getNumEdges()).isEqualTo(5);

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = cfg.getSuccessors(entry).iterator().next();
        CFGNode ifelse = dt.getSuccessors(greenFlag).stream().filter(t -> t.getASTNode() instanceof IfElseStmt).findFirst().get();
        CFGNode move1 = dt.getSuccessors(ifelse).stream().filter(t -> t.getASTNode() instanceof MoveSteps).toList().get(0);
        CFGNode move2 = dt.getSuccessors(ifelse).stream().filter(t -> t.getASTNode() instanceof MoveSteps).toList().get(1);
        CFGNode exit = cfg.getExitNode();

        assertThat(dt.getEdges()).contains(EndpointPair.ordered(entry, greenFlag));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(greenFlag, exit));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(greenFlag, ifelse));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(ifelse, move1));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(ifelse, move2));
    }

    @Test
    public void testTwoEvents() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/twoevents.json");
        DominatorTree dt = new DominatorTree(cfg);

        assertThat(dt.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(dt.getNumEdges()).isEqualTo(5);

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = dt.getSuccessors(entry).stream().filter(t -> t.getASTNode() instanceof GreenFlag).findFirst().get();
        CFGNode move1 = dt.getSuccessors(greenFlag).iterator().next();
        CFGNode keyPressed = dt.getSuccessors(entry).stream().filter(t -> t.getASTNode() instanceof KeyPressed).toList().get(0);
        CFGNode move2 = dt.getSuccessors(keyPressed).iterator().next();
        CFGNode exit = cfg.getExitNode();

        assertThat(dt.getEdges()).contains(EndpointPair.ordered(entry, greenFlag));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(entry, keyPressed));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(entry, exit));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(greenFlag, move1));
        assertThat(dt.getEdges()).contains(EndpointPair.ordered(keyPressed, move2));
    }
}
