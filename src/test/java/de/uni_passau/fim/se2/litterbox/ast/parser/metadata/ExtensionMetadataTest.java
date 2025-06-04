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
package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensionMetadataTest implements JsonTest {

    @Test
    void testEmptyProgram() throws Exception {
        final Program program = getAST("src/test/fixtures/emptyProject.json");
        assertTrue(program.getProgramMetadata().getExtension().getExtensionNames().isEmpty());
    }

    @Test
    void testTwoExtensions() throws Exception {
        final Program program = getAST("./src/test/fixtures/metadata/metaExtensionMonitorData.json");
        final ExtensionMetadata meta = program.getProgramMetadata().getExtension();

        assertEquals(2, meta.getExtensionNames().size());
        assertIterableEquals(List.of("pen", "music"), meta.getExtensionNames());
    }
}
