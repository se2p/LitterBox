/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.jsoncreation.JSONFileCreator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class JSONCreate {
    private static final ObjectMapper mapper = new ObjectMapper();

    @AfterAll
    static void cleanUp() throws IOException {
        Files.delete(Path.of("createBroadcast_annotated.json"));
    }

    @Test
    public void createJSON() throws ParsingException, IOException {
        File f = new File("./src/test/fixtures/stmtParser/manipulatedBroadcast.json");
        JsonNode prog = mapper.readTree(f);
        Scratch3Parser parser = new Scratch3Parser();
        Program test = parser.parseJsonNode("createBroadcast", prog);
        JSONFileCreator.writeJsonFromProgram(test, "_annotated");
    }
}
