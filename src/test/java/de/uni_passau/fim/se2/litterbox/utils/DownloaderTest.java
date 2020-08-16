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
package de.uni_passau.fim.se2.litterbox.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import de.uni_passau.fim.se2.litterbox.ast.visitor.DotVisitor;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.fail;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class DownloaderTest {

    private JsonNode project;

    //@Test
    void downloadProjectJSON() {
        try {
            String json = Downloader.downloadProjectJSON("338832275");
            Downloader.saveDownloadedProject(json, "stuff", "/tmp/");

            ObjectMapper objectMapper = new ObjectMapper();
            project = objectMapper.readTree(json);
            Program program = ProgramParser.parseProgram("338832275", project);
            DotVisitor visitor = new DotVisitor();
            program.accept(visitor);
            //  visitor.printGraph();
            //  visitor.saveGraph("./target/graph.dot");
        } catch (IOException | ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    //@Test
    void testDownloaded() {
        String path = "/tmp/stuff.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
            Program parsed = ProgramParser.parseProgram("stuff", project);
        } catch (IOException | ParsingException e) {
            fail();
        }
    }
}
