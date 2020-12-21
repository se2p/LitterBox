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
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.hint.MessageNeverSentHintFactory;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class MessageNeverSentTest implements JsonTest {

    @Test
    public void testMessageNeverSent() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/broadcastSync.json");
        MessageNeverSent finder = new MessageNeverSent();
        Set<Issue> reports = finder.check(program);
        Truth.assertThat(reports).isEmpty();
    }

    @Test
    public void testMessageRec() throws IOException, ParsingException {
        Program messageRec = getAST("src/test/fixtures/bugpattern/messageRec.json");
        MessageNeverSent finder = new MessageNeverSent();
        Set<Issue> reports = finder.check(messageRec);
        Truth.assertThat(reports).hasSize(1);
    }

    @Test
    public void testMessageNeverSentSay() throws IOException, ParsingException {
        Program messageRec = getAST("src/test/fixtures/bugpattern/messageNeverSentSay.json");
        MessageNeverSent finder = new MessageNeverSent();
        Set<Issue> reports = finder.check(messageRec);
        Truth.assertThat(reports).hasSize(1);
        for (Issue issue : reports) {
            Hint hint = new Hint(MessageNeverSentHintFactory.MESSAGE_IN_SAY_OR_THINK);
            hint.setParameter(Hint.HINT_SPRITES, "Sprite1");
            hint.setParameter(Hint.HINT_MESSAGE,"test");
            hint.setParameter(Hint.HINT_SAY_THINK, IssueTranslator.getInstance().getInfo("say"));
            Assertions.assertEquals(hint.getHintText(), issue.getHint());
        }
    }

    @Test
    public void testMessageNeverSentTouching() throws IOException, ParsingException {
        Program messageRec = getAST("src/test/fixtures/bugpattern/messageNeverSentTouching.json");
        MessageNeverSent finder = new MessageNeverSent();
        Set<Issue> reports = finder.check(messageRec);
        Truth.assertThat(reports).hasSize(1);
        for (Issue issue : reports) {
            Hint hint = new Hint(MessageNeverSentHintFactory.TOUCHING_USED);
            hint.setParameter(Hint.HINT_SPRITES, "Sprite1");
            hint.setParameter(Hint.HINT_SPRITE, "Bat");
            hint.setParameter(Hint.HINT_MESSAGE,"Bat berührt");
            Assertions.assertEquals(hint.getHintText(), issue.getHint());
        }
    }

    @Test
    public void testMessageNeverSentDoubles() throws IOException, ParsingException {
        Program messageRec = getAST("src/test/fixtures/bugpattern/messageNeverSentDoubles.json");
        MessageNeverSent finder = new MessageNeverSent();
        Set<Issue> reports = finder.check(messageRec);
        Truth.assertThat(reports).hasSize(2);
        List<Issue> list = new ArrayList<>(reports);
        Assertions.assertTrue(list.get(0).isDuplicateOf(list.get(1)));
    }
}
