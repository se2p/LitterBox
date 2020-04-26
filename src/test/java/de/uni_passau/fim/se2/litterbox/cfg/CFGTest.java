/*
 * Copyright (C) 2019 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class CFGTest {

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
    public void testVariable2() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/bugpattern/missingVariableInitializationInParallel2.json");
        System.out.println(cfg.toDotString());
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
}
