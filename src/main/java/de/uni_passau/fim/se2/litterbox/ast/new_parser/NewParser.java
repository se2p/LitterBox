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

import java.io.IOException;
import java.nio.file.Path;

public class NewParser {

    private static final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public Program parse(final Path projectJson) throws ParsingException {
        try {
            final RawProject rawProject = mapper.readValue(projectJson.toFile(), RawProject.class);
            final Program program = RawProjectConverter.convert(rawProject, getProjectName(projectJson));

            final ParentVisitor v = new ParentVisitor();
            program.accept(v);

            return program;
        } catch (IOException e) {
            throw new ParsingException("Could not read project JSON!", e);
        }
    }

    public Program parse(final String name, final String projectJson) throws ParsingException {
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

    private String getProjectName(final Path projectJson) {
        return projectJson.getFileName().toString().replace(".json", "");
    }
}
