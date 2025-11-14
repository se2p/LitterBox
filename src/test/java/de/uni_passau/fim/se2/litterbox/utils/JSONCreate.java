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
package de.uni_passau.fim.se2.litterbox.utils;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.ForeverInsideLoop;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.jsoncreation.JSONFileCreator;
import de.uni_passau.fim.se2.litterbox.report.CommentGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

class JSONCreate implements JsonTest {

    private final IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);

    @AfterAll
    static void cleanUp() throws IOException {
        Files.delete(Path.of("manipulatedBroadcast_annotated.json"));
        Files.delete(Path.of("foreverInLoop_annotated.json"));
    }

    @Test
    public void createJSON() throws ParsingException, IOException {
        Program test = getAST("./src/test/fixtures/stmtParser/manipulatedBroadcast.json");
        JSONFileCreator.writeJsonFromProgram(test, "_annotated");
    }

    @Test
    public void createAnnotatedJSON() throws ParsingException, IOException {
        Program prog = getAST("./src/test/fixtures/bugpattern/foreverInLoop.json");
        ForeverInsideLoop fil = new ForeverInsideLoop();
        Set<Issue> issues = fil.check(prog);
        CommentGenerator gen = new CommentGenerator(translator);
        gen.generateReport(prog, issues);
        JSONFileCreator.writeJsonFromProgram(prog, "_annotated");
    }
}
