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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.FinderTest;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class MessageAsLiteralTest implements FinderTest, JsonTest {

    @Test
    public void testMessageAsLiteral() throws IOException, ParsingException {
        // //Script: receive_msg1
        // when I receive [Msg1 v]
        // say [Msg1]
        // broadcast (Msg2 v)
        // say [Msg2]
        // Expect 1 issue: "Msg1" is defined (received) but never sent, and used as literal in say block.
        // "Msg2" is sent, so usage as literal is allowed.
        assertThatFinderReports(1, new MessageAsLiteral(), "src/test/fixtures/bugpattern/messageAsLiteral.json");
    }

    @Test
    public void testMessageAsLiteralHint() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/messageAsLiteral.json");
        MessageAsLiteral finder = new MessageAsLiteral();
        Set<Issue> reports = finder.check(program);
        Truth.assertThat(reports).hasSize(1);
        Hint hint = Hint.fromKey(finder.getName());
        hint.setParameter(Hint.HINT_MESSAGE, "Msg1");
        Truth.assertThat(hint.getHintText(translator)).isEqualTo("[b]Problem:[/b] [newLine] You are using the " +
                "name of a message as text. [newLine] [newLine] [b]Suggestion for code improvement:[/b] " +
                "[newLine] Probably, you meant to use a [sbi]broadcast (Msg1 v)[/sbi] or [sbi]broadcast (Msg1 v) " +
                "and wait[/sbi] block.");
        for (Issue issue : reports) {
            Truth.assertThat(issue.getHintText(translator)).isEqualTo(hint.getHintText(translator));
        }
    }
}
