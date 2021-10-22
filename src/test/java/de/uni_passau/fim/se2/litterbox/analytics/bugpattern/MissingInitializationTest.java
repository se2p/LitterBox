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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MissingInitializationTest implements JsonTest {

    @Test
    public void testMissingInitialization() throws IOException, ParsingException {
        assertThatFinderReports(1, new MissingInitialization(), "src/test/fixtures/bugpattern/missingVariableInitialization.json");
    }

    @Test
    public void testMissingInitializationInClone() throws IOException, ParsingException {
        assertThatFinderReports(1, new MissingInitialization(), "src/test/fixtures/bugpattern/missingVariableInitializationInClone.json");
    }

    @Test
    public void testMissingInitializationWrongVarUsed() throws IOException, ParsingException {
        assertThatFinderReports(1, new MissingInitialization(), "src/test/fixtures/bugpattern/missingVariableInitializationWrongVarUsed.json");
    }

    @Test
    public void testMissingInitializationInBranch() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/missingVariableInitializationInBranch.json");
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        visitor.visit(program);

        Set<Issue> reports = (new MissingInitialization()).check(program);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testMissingInitializationInBroadcast() throws IOException, ParsingException {
        assertThatFinderReports(1, new MissingInitialization(), "src/test/fixtures/bugpattern/missingVariableInitializationInBroadcast.json");
    }

    @Test
    public void testMissingInitializationInTwoBroadcasts() throws IOException, ParsingException {
        assertThatFinderReports(1, new MissingInitialization(), "src/test/fixtures/bugpattern/missingVariableInitializationInTwoBroadcasts.json");
    }

    @Test
    public void testMissingInitializationInTwoBroadcastsWithDefinition() throws IOException, ParsingException {
        // Not an anomaly: The definition happens in the message receiver, and we don't know
        // if the execution of the receiver will be scheduled before the use
        assertThatFinderReports(0, new MissingInitialization(), "src/test/fixtures/bugpattern/missingVariableInitializationInTwoBroadcastsWithDefinition.json");
    }

    @Test
    public void testMissingInitializationTwoVarReadChange() throws IOException, ParsingException {
        // 2 vars, each is first used in a say, then in a change
        assertThatFinderReports(4, new MissingInitialization(), "src/test/fixtures/bugpattern/missingVariableInitializationTwoVarReadChange.json");
    }

    @Test
    public void testMissingInitializationVariableOfAndVar() throws IOException, ParsingException {
        // One direct use, one us with AttributeOf
        assertThatFinderReports(2, new MissingInitialization(), "src/test/fixtures/bugpattern/missingVariableInitializationVariableOf.json");
    }

    @Test
    public void testMissingInitializationInParallel() throws IOException, ParsingException {
        // This is not an anomaly: The initialization may happen before the use, depending on the scheduler
        assertThatFinderReports(0, new MissingInitialization(), "src/test/fixtures/bugpattern/missingVariableInitializationInParallel.json");
    }

    @Test
    public void testMissingInitializationVarAndAttribute() throws IOException, ParsingException {
        assertThatFinderReports(2, new MissingInitialization(), "src/test/fixtures/bugpattern/missingVariableAndAttributeInitialization.json");
    }

    @Test
    public void testMissingPositionInitialization() throws IOException, ParsingException {
        assertThatFinderReports(1, new MissingInitialization(), "src/test/fixtures/bugpattern/missingAttributeInitializationPosition.json");
    }

    @Test
    public void testMissingListInitialization() throws IOException, ParsingException {
        assertThatFinderReports(1, new MissingInitialization(), "src/test/fixtures/cfg/listoperations.json");
    }

    @Test
    public void testMissingInitializationInCustomBlockWithCall() throws IOException, ParsingException {
        assertThatFinderReports(1, new MissingInitialization(), "src/test/fixtures/bugpattern/missingInitializationInCustomBlock.json");
    }

    @Test
    public void testMissingInitializationInCustomBlockWithoutCall() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/missingInitializationInCustomBlockNotCalled.json");
        MissingInitialization initialization = new MissingInitialization();
        Assertions.assertEquals(MissingInitialization.NAME, initialization.getName());
        Set<Issue> reports = (initialization).check(program);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMissingInitializationOfList() throws IOException, ParsingException {
        assertThatFinderReports(1, new MissingInitialization(), "src/test/fixtures/bugpattern/missingInitializationUseList.json");
    }

    @Test
    public void testMissingInitializationOfListNotReportingHide() throws IOException, ParsingException {
        assertThatFinderReports(0, new MissingInitialization(), "src/test/fixtures/bugpattern/missingInitializationHideList.json");
    }

    @Test
    public void testMissingInitializationCostumeInCloneScript() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/missingInitClone.json");
        MissingInitialization initialization = new MissingInitialization();
        List<Issue> reports = new ArrayList<>(initialization.check(program));
        Assertions.assertEquals(1, reports.size());
        Hint hint = new Hint(MissingInitialization.NAME_CLONE);
        hint.setParameter(Hint.HINT_VARIABLE, "attribute \"costume\"");
        Assertions.assertEquals(hint.getHintText(), reports.get(0).getHint());
    }
}
