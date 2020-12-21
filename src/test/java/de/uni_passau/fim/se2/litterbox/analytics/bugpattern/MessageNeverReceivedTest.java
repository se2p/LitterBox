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

import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class MessageNeverReceivedTest implements JsonTest {

    @Test
    public void testMessageNeverReceived() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/broadcastSync.json");
        MessageNeverReceived finder = new MessageNeverReceived();
        Set<Issue> reports = finder.check(program);
        Truth.assertThat(reports).hasSize(4);
        // TODO: Restore this check
//        Truth.assertThat(check.getPosition().get(2)).isEqualTo("Apple");
//        Truth.assertThat(check.getPosition().get(0)).isEqualTo("Sprite1");
//        Truth.assertThat(check.getPosition().get(1)).isEqualTo("Abby");
    }

    @Test
    public void testMessageNeverReceivedDouble() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/messageNeverReceivedDouble.json");
        MessageNeverReceived finder = new MessageNeverReceived();
        List<Issue> reports = new ArrayList<>(finder.check(program));
        Truth.assertThat(reports).hasSize(3);
        Assertions.assertTrue(reports.get(0).isDuplicateOf(reports.get(1)));
        Assertions.assertFalse(reports.get(0).isDuplicateOf(reports.get(2)));
    }
}
