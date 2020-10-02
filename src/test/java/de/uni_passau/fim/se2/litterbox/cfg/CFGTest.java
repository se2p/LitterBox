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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class CFGTest implements JsonTest {

    @Test
    public void testGreenflag() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/greenflag.json");
        assertThat(cfg.getNumNodes()).isEqualTo(4); // Entry, Exit, Greenflag, Move
        assertThat(cfg.getNumEdges()).isEqualTo(4); // Greenflag is conditional
    }

    @Test
    public void testTwoGreenflags() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/twogreenflags.json");
        assertThat(cfg.getNumNodes()).isEqualTo(5); // Entry, Exit, Greenflag, Move, TurnRight
        assertThat(cfg.getNumEdges()).isEqualTo(6); // Greenflag is conditional
    }

    @Test
    public void testOnClick() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/onclick.json");
        assertThat(cfg.getNumNodes()).isEqualTo(4); // Entry, Exit, Click, Move
        assertThat(cfg.getNumEdges()).isEqualTo(4); // Click is conditional
    }

    @Test
    public void testOnKeyPress() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/onkeypress.json");
        assertThat(cfg.getNumNodes()).isEqualTo(4); // Entry, Exit, Press, Move
        assertThat(cfg.getNumEdges()).isEqualTo(4); // Press is conditional
    }

    @Test
    public void testOnStage() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/onstage.json");
        assertThat(cfg.getNumNodes()).isEqualTo(4); // Entry, Exit, Stage, Move
        assertThat(cfg.getNumEdges()).isEqualTo(4); // OnStage is not connected!
        // TODO: Is this a bug pattern?
    }

    @Test
    public void testOnVolume() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/onvolume.json");
        // TODO: Also test for multiple event handlers
        assertThat(cfg.getNumNodes()).isEqualTo(4); // Entry, Exit, Volume, Move
        assertThat(cfg.getNumEdges()).isEqualTo(4); // Volume is conditional
    }

    @Test
    public void testIfThen() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/ifthen.json");
        assertThat(cfg.getNumNodes()).isEqualTo(5); // Entry, Exit, Greenflag, If, Move
        assertThat(cfg.getNumEdges()).isEqualTo(6); // Two conditionals
    }

    @Test
    public void testIfElse() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/ifelse.json");
        assertThat(cfg.getNumNodes()).isEqualTo(6); // Entry, Exit, Greenflag, If, Movex2
        assertThat(cfg.getNumEdges()).isEqualTo(7); // Event and then+else
    }

    @Test
    public void testRepeatTimes() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/repeattimes.json");
        assertThat(cfg.getNumNodes()).isEqualTo(5); // Entry, Exit, Greenflag, Repeat, Move
        assertThat(cfg.getNumEdges()).isEqualTo(6); // Two conditionals
    }

    @Test
    public void testRepeatUntil() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/repeatuntil.json");
        assertThat(cfg.getNumNodes()).isEqualTo(5); // Entry, Exit, Greenflag, Repeat, Move
        assertThat(cfg.getNumEdges()).isEqualTo(6); // Two conditionals
    }

    @Test
    public void testRepeatForever() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/repeatforever.json");
        assertThat(cfg.getNumNodes()).isEqualTo(5); // Entry, Exit, Greenflag, Repeat, Move
        assertThat(cfg.getNumEdges()).isEqualTo(6); // Two conditionals
    }

    @Test
    public void testIfElseWithRepeat() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/ifelse_repeattimes.json");
        assertThat(cfg.getNumNodes()).isEqualTo(8); // Entry, Exit, Greenflag, IfElse, Repeatx2, Movex2
        assertThat(cfg.getNumEdges()).isEqualTo(11);
    }

    @Test
    public void testTwoEvents() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/twoevents.json");
        assertThat(cfg.getNumNodes()).isEqualTo(6); // Entry, Exit, Greenflag, KeyPressed, 2xMove
        assertThat(cfg.getNumEdges()).isEqualTo(8);
    }

    @Test
    public void testBroadcastWithoutReceiver() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/broadcastnoreceiver.json");
        assertThat(cfg.getNumNodes()).isEqualTo(5); // Entry, Exit, Greenflag, Broadcast, Message
        assertThat(cfg.getNumEdges()).isEqualTo(6);
    }

    // TODO: This is a bug pattern?
    @Test
    public void testReceiveWithoutBroadcast() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/receivewithoutbroadcast.json");
        assertThat(cfg.getNumNodes()).isEqualTo(4); // Entry, Exit, BroadcastReceive, Move
        assertThat(cfg.getNumEdges()).isEqualTo(4);
    }

    @Test
    public void testReceiveBroadcast() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/broadcastreceive.json");
        assertThat(cfg.getNumNodes()).isEqualTo(6); // Entry, Exit, Greenflag, Broadcast, Receive, Move
        assertThat(cfg.getNumEdges()).isEqualTo(8);
    }

    @Test
    public void testReceiveTwoMessages() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/receivetwomessages.json");
        assertThat(cfg.getNumNodes()).isEqualTo(9); // Entry, Exit, Greenflag, Broadcastx2, Messagex2, Move, Turn
        assertThat(cfg.getNumEdges()).isEqualTo(13);
    }

    @Test
    public void testCloneInit() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/cloneinit.json");
        // On clone, and move
        assertThat(cfg.getNumNodes()).isEqualTo(4); // Entry, Exit, Greenflag, Broadcastx2, Messagex2, Move, Turn
        assertThat(cfg.getNumEdges()).isEqualTo(4);
    }

    @Test
    public void testCreateClone() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/createclone.json");
        assertThat(cfg.getNumNodes()).isEqualTo(5); // Entry, Exit, Greenflag, CreateClone, Clone
        assertThat(cfg.getNumEdges()).isEqualTo(6);
    }

    @Test
    public void testCreateCloneFromOther() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/createclonefromother.json");
        assertThat(cfg.getNumNodes()).isEqualTo(5); // Entry, Exit, Greenflag, CreateClone, Clone
        assertThat(cfg.getNumEdges()).isEqualTo(6);
    }

    @Test
    public void testCreateTwoClones() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/createtwoclones.json");
        assertThat(cfg.getNumNodes()).isEqualTo(7); // Entry, Exit, Greenflag, CreateClonex2, Clonex2
        assertThat(cfg.getNumEdges()).isEqualTo(9);
    }

    @Test
    public void testCreateAndInitTwoClones() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/createandinittwoclones.json");
        assertThat(cfg.getNumNodes()).isEqualTo(9); // Entry, Exit, Greenflag, CreateClonex2, Clonex2, Movex2
        assertThat(cfg.getNumEdges()).isEqualTo(13);
    }

    @Test
    public void testVariable() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/variable.json");
        assertThat(cfg.getNumNodes()).isEqualTo(6); // Entry, Exit, Set, Change, Read
        assertThat(cfg.getNumEdges()).isEqualTo(6);
    }

    @Test
    public void testTwoSprites() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/twosprites.json");
        assertThat(cfg.getNumNodes()).isEqualTo(5); // Entry, Exit, Greenflag, movex2
        assertThat(cfg.getNumEdges()).isEqualTo(6);
    }

    @Test
    public void testCustomBlock() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/customblock.json");
        assertThat(cfg.getNumNodes()).isEqualTo(4); // Entry, Exit, Block, Move
        assertThat(cfg.getNumEdges()).isEqualTo(3); // Block is connected to exit. TODO: Should it?
    }

    @Test
    public void testCallCustomBlock() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/callcustomblock.json");
        assertThat(cfg.getNumNodes()).isEqualTo(7); // Entry, Exit, Greenflag, move, call, return, customblock
        assertThat(cfg.getNumEdges()).isEqualTo(7);
    }

    @Test
    public void testTwoCallsCustomBlock() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/customblocktwocalls.json");
        assertThat(cfg.getNumNodes()).isEqualTo(9); // Entry, Exit, Greenflag, move, callx2
        // Returnx2, custom block
        assertThat(cfg.getNumEdges()).isEqualTo(10);
    }

    @Test
    public void testCallCustomBlockWithCode() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/calledcustomblock.json");
        assertThat(cfg.getNumNodes()).isEqualTo(9); // testCallCustomBlock + 2
        assertThat(cfg.getNumEdges()).isEqualTo(9); // testCallCustomBlock + 2
    }

    @Test
    public void testNextBackdrop() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/nextbackdroponstage.json");
        assertThat(cfg.getNumNodes()).isEqualTo(4);
        assertThat(cfg.getNumEdges()).isEqualTo(4);
    }

    @Test
    public void testNextBackdropOnSprite() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/nextbackdroponsprite.json");
        assertThat(cfg.getNumNodes()).isEqualTo(4);
        assertThat(cfg.getNumEdges()).isEqualTo(4);
    }

    // Empty loops may lead to self-loops. Which are ok.
    @Test
    public void testEmptyLoop() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/emptyloop.json");
        assertThat(cfg.getNumNodes()).isEqualTo(4);
        assertThat(cfg.getNumEdges()).isEqualTo(5); // Including self-loop
    }

    @Test
    public void testListStatements() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/listoperations.json");
        assertThat(cfg.getNumNodes()).isEqualTo(14);
        assertThat(cfg.getNumEdges()).isEqualTo(14);
    }

    @Test
    public void testCloneMyself() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/clonemyself.json");
        Optional<CFGNode> createCloneNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof CreateCloneOf).findFirst();
        assertThat(createCloneNode.isPresent());
        Set<CFGNode> successors = cfg.getSuccessors(createCloneNode.get());
        assertThat(successors).hasSize(2);
        assertThat(successors).contains(cfg.getExitNode());
    }

    @Test
    public void testCloneOther() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/cloneother.json");
        Optional<CFGNode> createCloneNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof CreateCloneOf).findFirst();
        assertThat(createCloneNode.isPresent());
        Set<CFGNode> successors = cfg.getSuccessors(createCloneNode.get());
        assertThat(successors).hasSize(2);
        assertThat(successors).contains(cfg.getExitNode());
    }

    @Test
    public void testCloneVariable() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/clonevariable.json");
        Optional<CFGNode> createCloneNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof CreateCloneOf).findFirst();
        assertThat(createCloneNode.isPresent());
        Set<CFGNode> successors = cfg.getSuccessors(createCloneNode.get());
        // Overapproximate by adding edges to all sprites except stage
        assertThat(successors).hasSize(3);
        assertThat(successors).contains(cfg.getExitNode());
    }

    @Test
    public void testCloneExpression() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/cloneexpression.json");
        Optional<CFGNode> createCloneNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof CreateCloneOf).findFirst();
        assertThat(createCloneNode.isPresent());
        Set<CFGNode> successors = cfg.getSuccessors(createCloneNode.get());
        // Overapproximate by adding edges to all sprites except stage
        assertThat(successors).hasSize(3);
        assertThat(successors).contains(cfg.getExitNode());
    }

    @Test
    public void testBroadcastMessage() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/broadcastmessage.json");
        Optional<CFGNode> broadcastNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof Broadcast).findFirst();
        assertThat(broadcastNode.isPresent());
        Set<CFGNode> successors = cfg.getSuccessors(broadcastNode.get());
        assertThat(successors).hasSize(2);
        assertThat(successors).contains(cfg.getExitNode());
    }

    @Test
    public void testBroadcastAndWaitMessage() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/broadcastmessageandwait.json");
        Optional<CFGNode> broadcastNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof BroadcastAndWait).findFirst();
        assertThat(broadcastNode.isPresent());
        Set<CFGNode> successors = cfg.getSuccessors(broadcastNode.get());
        assertThat(successors).hasSize(2);
        assertThat(successors).contains(cfg.getExitNode());
    }

    @Test
    public void testBroadcastVariable() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/broadcastvariable.json");
        Optional<CFGNode> broadcastNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof Broadcast).findFirst();
        assertThat(broadcastNode.isPresent());
        Set<CFGNode> successors = cfg.getSuccessors(broadcastNode.get());
        // If the message is an expression (e.g. a variable) we need to overapproximate
        assertThat(successors).hasSize(3);
        assertThat(successors).contains(cfg.getExitNode());
    }

    @Test
    public void testBroadcastAndWaitVariable() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/broadcastvariableandwait.json");
        Optional<CFGNode> broadcastNode = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof BroadcastAndWait).findFirst();
        assertThat(broadcastNode.isPresent());
        Set<CFGNode> successors = cfg.getSuccessors(broadcastNode.get());
        // If the message is an expression (e.g. a variable) we need to overapproximate
        assertThat(successors).hasSize(3);
        assertThat(successors).contains(cfg.getExitNode());
    }
}
