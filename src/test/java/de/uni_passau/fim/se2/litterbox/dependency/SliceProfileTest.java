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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class SliceProfileTest implements JsonTest {

    @Test
    public void testNoDefinition() throws ParsingException, IOException {
        // Program only uses a variable in a set block
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dependency/noDefinition.json");
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);
        SliceProfile sliceProfile = new SliceProfile(pdg);
        assertThat(sliceProfile.getScriptLength()).isEqualTo(1);
        assertThat(sliceProfile.getIntersectionLength()).isEqualTo(1);
        assertThat(sliceProfile.getCoverage()).isEqualTo(1);
        assertThat(sliceProfile.getOverlap()).isEqualTo(1);
        assertThat(sliceProfile.getTightness()).isEqualTo(1);

    }

    @Test
    public void testSayNoDefinition() throws ParsingException, IOException {
        // Program only uses a variable in a say block
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dependency/sayVariableNoDefinition.json");
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);
        SliceProfile sliceProfile = new SliceProfile(pdg);
        assertThat(sliceProfile.getScriptLength()).isEqualTo(1);
        assertThat(sliceProfile.getIntersectionLength()).isEqualTo(1);
        assertThat(sliceProfile.getCoverage()).isEqualTo(1);
        assertThat(sliceProfile.getOverlap()).isEqualTo(1);
        assertThat(sliceProfile.getTightness()).isEqualTo(1);
    }

    @Test
    public void testTwoVariables() throws ParsingException, IOException {
        // Set X
        // Set Y
        // Change X
        // Change Y
        // Say X
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dependency/simpleSliceProfile.json");
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);
        SliceProfile sliceProfile = new SliceProfile(pdg);

        assertThat(sliceProfile.getScriptLength()).isEqualTo(5);
        assertThat(sliceProfile.getIntersectionLength()).isEqualTo(0);
        assertThat(sliceProfile.getCoverage()).isWithin(0.01).of(0.53);
        assertThat(sliceProfile.getOverlap()).isEqualTo(0);
        assertThat(sliceProfile.getTightness()).isEqualTo(0);
    }

    @Test
    public void testThreeVariables() throws ParsingException, IOException {
        // when green flag clicked
        // set [my variable v] to (0)
        // set [var1 v] to (0)
        // set [var2 v] to (0)
        // repeat (10)
        // move (10) steps
        // if <(my variable) = (10)> then
        // change [var1 v] by (1)
        // else
        // change [var2 v] by (1)
        // end
        // change [my variable v] by (1)
        // end
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dependency/threeVariableSliceProfile.json");
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);
        SliceProfile sliceProfile = new SliceProfile(pdg);

        assertThat(sliceProfile.getScriptLength()).isEqualTo(9);
        assertThat(sliceProfile.getIntersectionLength()).isEqualTo(1);
        assertThat(sliceProfile.getCoverage()).isWithin(0.01).of(0.56);
        assertThat(sliceProfile.getOverlap()).isWithin(0.01).of(0.25);
        assertThat(sliceProfile.getTightness()).isWithin(0.01).of(0.11);
    }

    @Test
    public void testLooksMotion() throws ParsingException, IOException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/dependency/looksMotion.json");
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);
        SliceProfile sliceProfile = new SliceProfile(pdg);

        assertThat(sliceProfile.getScriptLength()).isEqualTo(6);
        assertThat(sliceProfile.getIntersectionLength()).isEqualTo(2);
        assertThat(sliceProfile.getCoverage()).isWithin(0.01).of(0.66);
        assertThat(sliceProfile.getOverlap()).isWithin(0.01).of(0.52);
        assertThat(sliceProfile.getTightness()).isWithin(0.01).of(0.33);
    }
}
