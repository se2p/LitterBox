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

import de.uni_passau.fim.se2.litterbox.FinderTest;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class ScriptsForActorTest implements FinderTest, JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/emptyProject.json");
        Set<Question> questions = runFinder(prog, new ScriptsForActor(), false);
        assertThat(questions.size()).isEqualTo(2);

        for (Question question : questions) {
            assertThat(question.getCorrectAnswers()).contains("0");
        }
    }

    @Test
    public void testOneSpriteWithOneScript() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/questions/onlyStageOneScript.json");
        Set<Question> questions = runFinder(prog, new ScriptsForActor(), false);
        assertThat(questions.size()).isEqualTo(1);

        IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);

        for (Question question : questions) {
            assertThat(question.getCorrectAnswers()).contains("1");
            assertThat(question.getQuestionText(translator)).contains("Stage");
        }
    }

    @Test
    public void testOneSpriteWithTwoScripts() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/questions/onlyStageTwoScripts.json");
        Set<Question> questions = runFinder(prog, new ScriptsForActor(), false);
        assertThat(questions.size()).isEqualTo(1);

        for (Question question : questions) {
            assertThat(question.getCorrectAnswers()).contains("2");
        }
    }

    @Test
    public void testMultipleScriptsInDifferentActors() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/questions/eventsTriggeredByStatements.json");
        Set<Question> questions = runFinder(prog, new ScriptsForActor(), false);
        assertThat(questions.size()).isEqualTo(2);

        for (Question question : questions) {
            if (question.getActorName().equals("Sprite1"))
                assertThat(question.getCorrectAnswers()).contains("6");
            else
                assertThat(question.getCorrectAnswers()).contains("2");
        }
    }
}
