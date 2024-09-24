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
package de.uni_passau.fim.se2.litterbox.ast.new_parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawProject;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.utils.ZipReader;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class NewParser {

    private static final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public Program parseFile(final File projectFile) throws ParsingException {
        final String fileName = projectFile.getName();

        if ((FilenameUtils.getExtension(fileName)).equalsIgnoreCase("json")) {
            return parseJsonFile(projectFile);
        } else {
            return parseSB3File(projectFile);
        }
    }

    public Program parseSB3File(final File fileEntry) throws ParsingException {
        final String fileName = fileEntry.getName();
        final String programName = getProjectName(fileName);

        try {
            final String jsonString = ZipReader.getJsonString(fileEntry.toPath());
            return parseString(programName, jsonString);
        } catch (IOException e) {
            throw new ParsingException("Could not read input file.", e);
        }
    }

    public Program parseJsonFile(final File projectJson) throws ParsingException {
        try {
            final RawProject rawProject = mapper.readValue(projectJson, RawProject.class);
            final Program program = RawProjectConverter.convert(rawProject, getProjectName(projectJson.toPath()));

            final ParentVisitor v = new ParentVisitor();
            program.accept(v);

            return program;
        } catch (IOException e) {
            throw new ParsingException("Could not read project JSON!", e);
        }
    }

    public Program parseString(final String name, final String projectJson) throws ParsingException {
        try {
            final RawProject project = mapper.readValue(projectJson, RawProject.class);
            final Program program = RawProjectConverter.convert(project, name);

            final ParentVisitor v = new ParentVisitor();
            program.accept(v);

            return program;
        } catch (IOException e) {
            throw new ParsingException("Could not read project JSON!", e);
        }
    }

    private String getProjectName(final Path projectPath) {
        return getProjectName(projectPath.getFileName().toString());
    }

    private String getProjectName(final String fileName) {
        return FilenameUtils.removeExtension(fileName);
    }
}
