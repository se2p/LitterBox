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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmptyControlBodyTest implements JsonTest {

    private final IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.GERMAN);

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new EmptyControlBody(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testEmptyBodies() throws IOException, ParsingException {
        Program emptyBodies = getAST("./src/test/fixtures/smells/emptyBodies.json");
        EmptyControlBody parameterName = new EmptyControlBody();
        List<Issue> reports = new ArrayList<>(parameterName.check(emptyBodies));
        Assertions.assertEquals(6, reports.size());
        Hint repeat = Hint.fromKey(parameterName.getName());
        repeat.setParameter(Hint.BLOCK_NAME, translator.getInfo("repeat") + " ( )");
        Assertions.assertEquals(repeat.getHintText(translator), reports.getFirst().getHintText(translator));
        Hint forever = Hint.fromKey(parameterName.getName());
        forever.setParameter(Hint.BLOCK_NAME, translator.getInfo("forever"));
        Assertions.assertEquals(forever.getHintText(translator), reports.get(1).getHintText(translator));
        Hint ifThen = Hint.fromKey(parameterName.getName());
        ifThen.setParameter(Hint.BLOCK_NAME, translator.getInfo("if") + " < > " + translator.getInfo("then"));
        Assertions.assertEquals("[b]Problem:[/b] [newLine] Du hast einen [sbi]falls < > , dann[/sbi]-Baustein verwendet, der keine Bausteine enthält. Beim Aufruf dieses Bausteins passiert also nichts. Das macht dein Programm unübersichtlich. [newLine] [newLine] [b]Verbesserungsidee:[/b] [newLine] Entferne den Baustein oder füge passende Bausteine in die Definition ein.", reports.get(2).getHintText(translator));
    }
}
