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
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawProject;

import java.io.IOException;
import java.nio.file.Path;

public class NewParser {

    private static final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public RawProject parse(final Path projectJson) throws IOException {
        return mapper.readValue(projectJson.toFile(), RawProject.class);
    }
}
