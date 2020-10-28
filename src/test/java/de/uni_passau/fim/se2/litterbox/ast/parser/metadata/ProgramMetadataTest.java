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
package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.MONITORS_KEY;

public class ProgramMetadataTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testMissingMonitors() throws IOException {
        File f = new File("./src/test/fixtures/metadata/missingMonitorsNode.json");
        JsonNode prog = mapper.readTree(f);
        Assertions.assertFalse(prog.has(MONITORS_KEY));
        ProgramMetadata metadata = ProgramMetadataParser.parse(prog);
        Assertions.assertEquals(0, metadata.getMonitor().getList().size());
        Assertions.assertEquals(0, metadata.getExtension().getExtensionNames().size());
    }
}
