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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImmediateStopAfterSayTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new ImmediateStopAfterSay(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testImmediateStop() throws IOException, ParsingException {
        assertThatFinderReports(2, new ImmediateStopAfterSay(), "./src/test/fixtures/bugpattern/immediateStop.json");
    }

    @Test
    public void testImmediateStopMultiple() throws IOException, ParsingException {
        Program illegalParameter = JsonTest.parseProgram("./src/test/fixtures/bugpattern/immediateStopAfterSayMultiple.json");
        ImmediateStopAfterSay parameterName = new ImmediateStopAfterSay();
        List<Issue> reports = new ArrayList<>(parameterName.check(illegalParameter));
        Assertions.assertEquals(1, reports.size());
        Hint hint = new Hint(ImmediateStopAfterSay.HINT_MULTIPLE);
        hint.setParameter(Hint.HINT_SAY_THINK, "say");
        Assertions.assertEquals(hint.getHintText(), reports.get(0).getHint());
    }
}
