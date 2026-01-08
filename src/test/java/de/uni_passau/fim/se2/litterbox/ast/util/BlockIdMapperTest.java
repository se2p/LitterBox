/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.util;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BlockIdMapperTest implements JsonTest {

    @Test
    void testBuildMappingForIdenticalPrograms() throws Exception {
        Program program = getAST("src/test/fixtures/bugs/missingWaitUntilCondition.json");

        // When comparing a program to itself, all block IDs should map to themselves
        Map<String, String> mapping = BlockIdMapper.buildMapping(program, program);

        // The mapping should contain entries (self-mappings)
        assertFalse(mapping.isEmpty(), "Mapping should not be empty for a program with blocks");

        // All mappings should be identity mappings
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            assertEquals(entry.getKey(), entry.getValue(),
                    "Self-mapping should map block ID to itself");
        }
    }

    @Test
    void testBuildMappingForDifferentPrograms() throws Exception {
        Program program1 = getAST("src/test/fixtures/bugs/missingWaitUntilCondition.json");
        Program program2 = getAST("src/test/fixtures/bugs/foreverInLoop.json");

        // Different programs with different structures should have empty or partial mappings
        Map<String, String> mapping = BlockIdMapper.buildMapping(program1, program2);

        // This is just a sanity check - the mapping may or may not be empty
        // depending on whether there are structurally matching scripts
        assertNotNull(mapping);
    }
}
