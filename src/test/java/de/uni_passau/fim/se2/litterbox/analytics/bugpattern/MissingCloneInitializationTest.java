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
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MissingCloneInitializationTest implements JsonTest {

    @Test
    public void testCloneInit() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/missingCloneInitialization.json");
        MissingCloneInitialization finder = new MissingCloneInitialization();
        Set<Issue> reports = finder.check(program);
        Truth.assertThat(reports).hasSize(1);
        for (Issue issue : reports) {
            Truth.assertThat(((StrId) ((AsString) ((CreateCloneOf) issue.getCodeLocation()).getStringExpr()).getOperand1()).getName()).isEqualTo("Anina Dance");
        }
    }

    @Test
    public void testCloningWithClicked() throws IOException, ParsingException {
        Program clicked = getAST("src/test/fixtures/bugpattern/cloningWithClicked.json");
        MissingCloneInitialization finder = new MissingCloneInitialization();
        Set<Issue> reports = finder.check(clicked);
        Truth.assertThat(reports).isEmpty();
    }

    @Test
    public void testSnake() throws IOException, ParsingException {
        Program clicked = getAST("src/test/fixtures/bugpattern/snake.json");
        MissingCloneInitialization finder = new MissingCloneInitialization();
        Set<Issue> reports = finder.check(clicked);
        Truth.assertThat(reports).hasSize(1);
        Hint hint = new Hint(MissingCloneInitialization.HAS_DELETE_CLONE);
        hint.setParameter(Hint.HINT_SPRITE, "Körper");
        for (Issue issue : reports) {
            Truth.assertThat(issue.getHint()).isEqualTo(hint.getHintText());
        }
    }

    @Test
    public void testMissingCloneInitDeleteMessage() throws IOException, ParsingException {
        Program clicked = getAST("src/test/fixtures/bugpattern/missingCloneInitDeleteMessage.json");
        MissingCloneInitialization finder = new MissingCloneInitialization();
        Set<Issue> reports = finder.check(clicked);
        Truth.assertThat(reports).hasSize(1);
        Hint hint = new Hint(MissingCloneInitialization.HAS_DELETE_CLONE_MESSAGE);
        hint.setParameter(Hint.HINT_MESSAGE, "Nachricht1");
        hint.setParameter(Hint.EVENT_HANDLER, IssueTranslator.getInstance().getInfo("greenflag"));
        hint.setParameter(Hint.HINT_SPRITE, "Sprite1");
        System.out.println(hint.getHintText());
        for (Issue issue : reports) {
            Truth.assertThat(issue.getHint()).isEqualTo(hint.getHintText());
        }
    }

    @Test
    public void testCloneInitDuplication() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/bugpattern/missingCloneInitDouble.json");
        MissingCloneInitialization finder = new MissingCloneInitialization();
        List<Issue> reports = new ArrayList<>(finder.check(prog));
        Assertions.assertEquals(3, reports.size());
        Assertions.assertTrue(reports.get(0).isDuplicateOf(reports.get(1)));
        Assertions.assertFalse(reports.get(0).isDuplicateOf(reports.get(2)));
    }
}
