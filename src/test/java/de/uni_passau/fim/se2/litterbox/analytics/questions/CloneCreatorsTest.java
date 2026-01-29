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

public class CloneCreatorsTest implements JsonTest {

    @Test
    public void testCloneOfSprite() throws IOException, ParsingException {
        // "Sprite1" creates a clone of "Duck"
        // Use (Duck v) for menu selection syntax
        String script = """
                when green flag clicked
                create clone of (Duck v)
                """;

        // We need a program with at least two sprites for the choice list to be meaningful,
        // but parseScratchBlocks usually creates one sprite with the script.
        // For this test, we care about the question generated for the current sprite.
        Program program = parseScratchBlocks(script);

        CloneCreators finder = new CloneCreators();
        Set<Question> questions = finder.check(program);

        Assertions.assertEquals(1, questions.size());
        Question q = questions.iterator().next();

        IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);
        Assertions.assertTrue(q.getQuestionText(translator).contains("Which sprite creates clones of 'Duck'?"));
        // The default sprite name in parseScratchBlocks is usually "Sprite1" or similar.
        // We verified in emptyProject.json it's "Sprite1" generally.
        // But let's check what correct answer is contained.
        Assertions.assertFalse(q.getCorrectAnswers().isEmpty());
    }

    @Test
    public void testCloneOfMyself() throws IOException, ParsingException {
        // "Sprite1" creates a clone of "myself"
        String script = """
                when green flag clicked
                create clone of (myself v)
                """;

        Program program = parseScratchBlocks(script);

        CloneCreators finder = new CloneCreators();
        Set<Question> questions = finder.check(program);

        Assertions.assertEquals(1, questions.size());
        Question q = questions.iterator().next();

        // If "myself" is resolved to the actor name, and parser assigns "Sprite1"
        String correct = q.getCorrectAnswers().iterator().next();

        IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);
        Assertions.assertTrue(q.getQuestionText(translator).contains("Which sprite creates clones of '" + correct + "'?"));
    }
}
