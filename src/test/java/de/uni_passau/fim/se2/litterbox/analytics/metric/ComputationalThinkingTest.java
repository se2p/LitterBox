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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class ComputationalThinkingTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/emptyProject.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(empty)).isEqualTo(0.0);
        assertThat(new ComputationalThinkingAverageScore().calculateMetric(empty)).isEqualTo(0.0);
    }

    @Test
    public void testGreenflag() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/greenflag.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(2.0);
        // Greenflag
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // Move
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(1.0);
    }

    @Test
    public void testTwoGreenflags() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/twogreenflags.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(3.0);
        // Greenflag
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // Second Greenflag
        assertThat(new ComputationalThinkingScoreParallelization().calculateMetric(program)).isEqualTo(1.0);
        // Move
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(1.0);
    }


    @Test
    public void testOnClick() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/onclick.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(3.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // OnClick
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(2.0);
    }

    @Test
    public void testOnKeyPress() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/onkeypress.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(3.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // OnPress
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(2.0);
    }

    @Test
    public void testOnBackdropSwitch() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/onstage.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(4.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // OnBackdropSwitch
        assertThat(new ComputationalThinkingScoreSynchronization().calculateMetric(program)).isEqualTo(3.0);
    }

    @Test
    public void testOnVolume() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/onvolume.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(4.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // GreaterThan
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(3.0);
    }

    @Test
    public void testIfThen() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/ifthen.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(4.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // If
        assertThat(new ComputationalThinkingScoreLogic().calculateMetric(program)).isEqualTo(1.0);
        // KeyPressed
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(2.0);
    }

    @Test
    public void testIfElse() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/ifelse.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(5.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // If
        assertThat(new ComputationalThinkingScoreLogic().calculateMetric(program)).isEqualTo(2.0);
        // KeyPressed
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(2.0);
    }

    @Test
    public void testRepeatTimes() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/repeattimes.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(4.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // Flowcontrol
        assertThat(new ComputationalThinkingScoreFlowControl().calculateMetric(program)).isEqualTo(2.0);
        // Greenflag
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(1.0);
    }

    @Test
    public void testRepeatUntil() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/repeatuntil.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(6.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // Repeat-until
        assertThat(new ComputationalThinkingScoreFlowControl().calculateMetric(program)).isEqualTo(3.0);
        // Keypressed
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(2.0);
    }

    @Test
    public void testRepeatForever() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/repeatforever.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(4.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // Forever
        assertThat(new ComputationalThinkingScoreFlowControl().calculateMetric(program)).isEqualTo(2.0);
        // Greenflag
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(1.0);
    }

    @Test
    public void testIfElseWithRepeat() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/ifelse_repeattimes.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(7.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // If-Else
        assertThat(new ComputationalThinkingScoreLogic().calculateMetric(program)).isEqualTo(2.0);
        // Times
        assertThat(new ComputationalThinkingScoreFlowControl().calculateMetric(program)).isEqualTo(2.0);
        // Keypressed
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(2.0);
    }

    @Test
    public void testTwoEvents() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/twoevents.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(3.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // Keypressed
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(2.0);
    }

    @Test
    public void testBroadcastWithoutReceiver() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/broadcastnoreceiver.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(3.0);
        // Broadcast
        assertThat(new ComputationalThinkingScoreSynchronization().calculateMetric(program)).isEqualTo(2.0);
        // Greenflag
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(1.0);
    }

    @Test
    public void testReceiveBroadcast() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/broadcastreceive.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(4.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // Receive message
        assertThat(new ComputationalThinkingScoreSynchronization().calculateMetric(program)).isEqualTo(2.0);
        // Greenflag
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(1.0);
    }

    @Test
    public void testReceiveTwoMessages() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/receivetwomessages.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(5.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // Sequence
        assertThat(new ComputationalThinkingScoreFlowControl().calculateMetric(program)).isEqualTo(1.0);
        // Receive message
        assertThat(new ComputationalThinkingScoreSynchronization().calculateMetric(program)).isEqualTo(2.0);
        // Greenflag
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(1.0);
    }

    @Test
    public void testCloneInit() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/cloneinit.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(4.0);
        // Clones
        assertThat(new ComputationalThinkingScoreAbstraction().calculateMetric(program)).isEqualTo(3.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
    }

    @Test
    public void testVariable() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/variable.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(4.0);
        // Variable
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(2.0);
        // Sequence
        assertThat(new ComputationalThinkingScoreFlowControl().calculateMetric(program)).isEqualTo(1.0);
        // Greenflag
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(1.0);
    }

    @Test
    public void testTwoSprites() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/twosprites.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(3.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // Two sprites with scripts
        assertThat(new ComputationalThinkingScoreParallelization().calculateMetric(program)).isEqualTo(1.0);
        // Greenflag
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(1.0);
    }

    @Test
    public void testCustomBlock() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/customblock.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(3.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // Custom block
        assertThat(new ComputationalThinkingScoreAbstraction().calculateMetric(program)).isEqualTo(2.0);
    }

    @Test
    public void testCallCustomBlock() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/callcustomblock.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(4.0);
        // Move
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(1.0);
        // Custom block
        assertThat(new ComputationalThinkingScoreAbstraction().calculateMetric(program)).isEqualTo(2.0);
        // Greenflag
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(1.0);
    }

    @Test
    public void testListStatements() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/listoperations.json");
        assertThat(new ComputationalThinkingScore().calculateMetric(program)).isEqualTo(5.0);
        // List
        assertThat(new ComputationalThinkingScoreDataRepresentation().calculateMetric(program)).isEqualTo(3.0);
        // Sequence
        assertThat(new ComputationalThinkingScoreFlowControl().calculateMetric(program)).isEqualTo(1.0);
        // Greenflag
        assertThat(new ComputationalThinkingScoreUserInteractivity().calculateMetric(program)).isEqualTo(1.0);
    }
}
