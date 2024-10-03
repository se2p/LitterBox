/*
 * Copyright (C) 2019-2024 LitterBox contributors
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

public class PostDominatorTreeTest implements JsonTest {

    @Test
    public void testGreenflag() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/greenflag.json");
        PostDominatorTree pdt = new PostDominatorTree(cfg);

        assertThat(pdt.getNodes()).isEqualTo(cfg.getNodes());

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = cfg.getSuccessors(entry).iterator().next();
        CFGNode move = cfg.getSuccessors(greenFlag).stream().filter(t -> t.getASTNode() instanceof MoveSteps).findFirst().get();
        CFGNode exit = cfg.getExitNode();

        assertThat(pdt.getNumEdges()).isEqualTo(3);
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, move));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, greenFlag));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(greenFlag, entry));
    }


    @Test
    public void testRepeatUntil() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/repeatuntil.json");
        PostDominatorTree pdt = new PostDominatorTree(cfg);

        Set<CFGNode> nodes = pdt.getNodes();
        assertThat(nodes).isEqualTo(cfg.getNodes());
        assertThat(pdt.getNumEdges()).isEqualTo(4);

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = cfg.getSuccessors(entry).iterator().next();
        CFGNode until = cfg.getSuccessors(greenFlag).stream().filter(t -> t.getASTNode() instanceof UntilStmt).findFirst().get();
        CFGNode move = cfg.getSuccessors(until).stream().filter(t -> t.getASTNode() instanceof MoveSteps).findFirst().get();
        CFGNode exit = cfg.getExitNode();

        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, greenFlag));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, until));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(greenFlag, entry));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(until, move));
    }


    @Test
    public void testIfThen() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/ifthen.json");
        PostDominatorTree pdt = new PostDominatorTree(cfg);

        assertThat(pdt.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(pdt.getNumEdges()).isEqualTo(4);

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = cfg.getSuccessors(entry).iterator().next();
        CFGNode ifthen = cfg.getSuccessors(greenFlag).stream().filter(t -> t.getASTNode() instanceof IfThenStmt).findFirst().get();
        CFGNode move = cfg.getSuccessors(ifthen).stream().filter(t -> t.getASTNode() instanceof MoveSteps).findFirst().get();
        CFGNode exit = cfg.getExitNode();

        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, greenFlag));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, ifthen));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, move));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(greenFlag, entry));
    }

    @Test
    public void testIfElse() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/ifelse.json");
        PostDominatorTree pdt = new PostDominatorTree(cfg);

        assertThat(pdt.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(pdt.getNumEdges()).isEqualTo(5);

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = cfg.getSuccessors(entry).iterator().next();
        CFGNode ifelse = cfg.getSuccessors(greenFlag).stream().filter(t -> t.getASTNode() instanceof IfElseStmt).findFirst().get();
        CFGNode move1 = cfg.getSuccessors(ifelse).stream().filter(t -> t.getASTNode() instanceof MoveSteps).toList().get(0);
        CFGNode move2 = cfg.getSuccessors(ifelse).stream().filter(t -> t.getASTNode() instanceof MoveSteps).toList().get(1);
        CFGNode exit = cfg.getExitNode();

        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, greenFlag));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, ifelse));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, move1));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, move2));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(greenFlag, entry));
    }

    @Test
    public void testTwoEvents() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/twoevents.json");
        PostDominatorTree pdt = new PostDominatorTree(cfg);

        assertThat(pdt.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(pdt.getNumEdges()).isEqualTo(5);

        CFGNode entry = cfg.getEntryNode();
        CFGNode greenFlag = cfg.getSuccessors(entry).stream().filter(t -> t.getASTNode() instanceof GreenFlag).findFirst().get();
        CFGNode move1 = cfg.getSuccessors(greenFlag).stream().filter(t -> t.getASTNode() instanceof MoveSteps).findFirst().get();
        CFGNode keyPressed = cfg.getSuccessors(entry).stream().filter(t -> t.getASTNode() instanceof KeyPressed).toList().get(0);
        CFGNode move2 = cfg.getSuccessors(keyPressed).stream().filter(t -> t.getASTNode() instanceof MoveSteps).findFirst().get();
        CFGNode exit = cfg.getExitNode();

        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, greenFlag));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, keyPressed));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, entry));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, move1));
        assertThat(pdt.getEdges()).contains(EndpointPair.ordered(exit, move2));
    }
}
