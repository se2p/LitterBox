/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

public class BroadcastReceiversTest implements JsonTest {

    private final IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);

    @Test
    public void testBroadcastReceiverIdentification() throws IOException, ParsingException {
        // Program with two sprites: Sprite1 broadcasts "msg1", Sprite2 receives "msg1"
        // Note: For parseScratchBlocks, we define additional sprites/scripts beyond the base project
        // But parseScratchBlocks adds to a single sprite "Sprite1" by default for the given code.
        // To test multiple actors correctly with parseScratchBlocks is tricky as it treats input as one script set.
        // HOWEVER, we can simulate multiple actors by treating the whole text as "one program state" if the parser supports "Sprite: ..." syntax or similar
        // BUT parseScratchBlocks extends an existing one-sprite project.

        // Wait, JsonTest.parseScratchBlocks extends an Empty Project which has ONE Sprite.
        // We cannot easily create a multi-sprite project with parseScratchBlocks unless we hack it or use a different helper.
        // Let's re-read JsonTest.parseScratchBlocks.

        // "Program program = parser.extendProject(baseProject, spriteName, code);"
        // It puts ALL code into THAT ONE sprite.

        // BroadcastReceivers logic: "Which sprites receive the message?"
        // If everything is in one sprite, then that one sprite is the receiver and sender.
        // That is sufficient to test the LOGIC of "finding the receiver".
        // The QuestionFinder logic is:
        // 1. Find "when I receive [msg]" -> map msg to Actor.
        // 2. Find "broadcast [msg]" -> look up Actor in map.

        // So if we put both in Sprite1, it should say Sprite1 receives it.

        String script = """
                when green flag clicked
                broadcast [Message1]

                when I receive [Message1 v]
                say [Hello!]
                """;

        Program program = parseScratchBlocks(script);
        BroadcastReceivers finder = new BroadcastReceivers();
        Set<Question> questions = finder.check(program);

        Assertions.assertEquals(1, questions.size());
        Question q = questions.iterator().next();

        // Expected usage: Sprite1 (the default sprite name in emptyProject.json)
        // Check translated question text includes the message
        Assertions.assertTrue(q.getChoices().stream()
                .map(c -> c.resolveText(translator, translator::getInfo))
                .anyMatch(c -> c.contains("Sprite1")), "Correct receiver Sprite1 not found in choices");
    }
}
