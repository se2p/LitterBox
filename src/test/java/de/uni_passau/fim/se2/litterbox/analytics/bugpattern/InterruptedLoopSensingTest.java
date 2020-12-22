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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InterruptedLoopSensingTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        InterruptedLoopSensing parameterName = new InterruptedLoopSensing();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testInterruptedLoopSensing() throws IOException, ParsingException {
        Program illegalParameter = JsonTest.parseProgram("./src/test/fixtures/bugpattern/interruptedLoopSensing.json");
        InterruptedLoopSensing parameterName = new InterruptedLoopSensing();
        Set<Issue> reports = parameterName.check(illegalParameter);
        Assertions.assertEquals(2, reports.size());
        List<Issue> issues= new ArrayList<>(reports);
        Hint hint1 = new Hint(parameterName.getName());
        hint1.setParameter(Hint.THEN_ELSE, IssueTranslator.getInstance().getInfo("if") + " " + IssueTranslator.getInstance().getInfo("then"));
        hint1.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("glide_secs_to_xy"));
        Hint hint2 = new Hint(parameterName.getName());
        hint2.setParameter(Hint.THEN_ELSE, IssueTranslator.getInstance().getInfo("until"));
        hint2.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("glide_secs_to"));
        Assertions.assertEquals(hint1.getHintText(),issues.get(0).getHint());
        Assertions.assertEquals(hint2.getHintText(),issues.get(1).getHint());
    }

    @Test
    public void testInterruptedLoopSensingWithVariableChanging() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/bugpattern/interruptedLoopSensingChangingVariable.json");
        InterruptedLoopSensing parameterName = new InterruptedLoopSensing();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }
}
