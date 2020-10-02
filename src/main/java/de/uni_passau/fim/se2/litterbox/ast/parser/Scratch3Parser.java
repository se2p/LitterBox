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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.utils.JsonParser;
import de.uni_passau.fim.se2.litterbox.utils.ZipReader;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

public class Scratch3Parser {

    public Program parseJsonNode(String programName, JsonNode node) throws ParsingException, RuntimeException {
        Program program = ProgramParser.parseProgram(programName, node);
        program.accept(new ParentVisitor());
        return program;
    }

    public Program parseFile(String fileName) throws IOException, ParsingException {
        return parseFile(new File(fileName));
    }

    public Program parseFile(File fileEntry) throws IOException, ParsingException {
        String fileName = fileEntry.getName();
        if ((FilenameUtils.getExtension(fileName)).toLowerCase().equals("json")) {
            return parseJsonFile(fileEntry);
        } else {
            return parseSB3File(fileEntry);
        }
    }

    public Program parseJsonFile(File fileEntry) throws IOException, ParsingException {
        String fileName = fileEntry.getName();
        String programName = getProgramName(fileName);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(fileEntry);
        return parseJsonNode(programName, rootNode);
    }

    public Program parseSB3File(File fileEntry) throws IOException, ParsingException {
        String fileName = fileEntry.getName();
        String programName = getProgramName(fileName);

        String jsonString = ZipReader.getJsonString(fileEntry.getPath());
        return parseString(programName, jsonString);
    }

    public Program parseString(String programName, String json) throws ParsingException {
        JsonNode rootNode = JsonParser.getTargetsNodeFromJSONString(json);
        return parseJsonNode(programName, rootNode);
    }

    private String getProgramName(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
}
