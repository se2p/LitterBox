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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;

public class TimeDependenceGraphTest implements JsonTest {

    @Test
    public void testGreenflag() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/greenflag.json");
        TimeDependenceGraph tdg = new TimeDependenceGraph(cfg);

        assertThat(tdg.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(tdg.getNumEdges()).isEqualTo(0);
    }

    @Test
    public void testReachingDefinitionsInClone() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/variables.json");
        TimeDependenceGraph tdg = new TimeDependenceGraph(cfg);

        assertThat(tdg.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(tdg.getNumEdges()).isEqualTo(1);

        CFGNode say = tdg.getNodes().stream().filter(t -> t.getASTNode() instanceof SayForSecs).findFirst().get();
        assertThat(tdg.getEdges()).containsExactly(EndpointPair.ordered(say, cfg.getExitNode()));
    }


    @Test
    public void testAllDelayStatements() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dependency/delays.json");
        TimeDependenceGraph tdg = new TimeDependenceGraph(cfg);

        assertThat(tdg.getNodes()).isEqualTo(cfg.getNodes());
        assertThat(tdg.getNumEdges()).isEqualTo(30);

        CFGNode sayForSecs = tdg.getNodes().stream().filter(t -> t.getASTNode() instanceof SayForSecs).findFirst().get();
        CFGNode glideTo = tdg.getNodes().stream().filter(t -> t.getASTNode() instanceof GlideSecsTo).findFirst().get();
        CFGNode glideToXY = tdg.getNodes().stream().filter(t -> t.getASTNode() instanceof GlideSecsToXY).findFirst().get();
        CFGNode playSound = tdg.getNodes().stream().filter(t -> t.getASTNode() instanceof PlaySoundUntilDone).findFirst().get();
        CFGNode thinkForSecs = tdg.getNodes().stream().filter(t -> t.getASTNode() instanceof ThinkForSecs).findFirst().get();
        CFGNode wait = tdg.getNodes().stream().filter(t -> t.getASTNode() instanceof WaitSeconds).findFirst().get();
        CFGNode say = tdg.getNodes().stream().filter(t -> t.getASTNode() instanceof Say).findFirst().get();
        CFGNode if1 = tdg.getNodes().stream().filter(t -> t.getASTNode() instanceof IfThenStmt).findFirst().get();
        CFGNode if2 = cfg.getSuccessors(if1).stream().filter(t -> t.getASTNode() instanceof IfThenStmt).findFirst().get();

        for (CFGNode node : Arrays.asList(sayForSecs, glideTo, glideToXY, playSound, thinkForSecs, wait)) {
            assertThat(tdg.getEdges()).contains(EndpointPair.ordered(node, cfg.getExitNode()));
            assertThat(tdg.getEdges()).contains(EndpointPair.ordered(node, say));
        }
        for (CFGNode node : Arrays.asList(sayForSecs, glideTo, glideToXY, playSound, thinkForSecs)) {
            assertThat(tdg.getEdges()).contains(EndpointPair.ordered(node, wait));
        }
        for (CFGNode node : Arrays.asList(glideTo, glideToXY, playSound, thinkForSecs)) {
            assertThat(tdg.getEdges()).contains(EndpointPair.ordered(node, sayForSecs));
        }
        for (CFGNode node : Arrays.asList(glideTo, glideToXY, playSound)) {
            assertThat(tdg.getEdges()).contains(EndpointPair.ordered(node, thinkForSecs));
        }
        for (CFGNode node : Arrays.asList(glideTo, glideToXY)) {
            assertThat(tdg.getEdges()).contains(EndpointPair.ordered(node, playSound));
            assertThat(tdg.getEdges()).contains(EndpointPair.ordered(node, if1));
            assertThat(tdg.getEdges()).contains(EndpointPair.ordered(node, if2));
        }
    }
}
