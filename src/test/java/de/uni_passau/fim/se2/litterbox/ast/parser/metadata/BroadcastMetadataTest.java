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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.BroadcastMetadataList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.BROADCASTS_KEY;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.TARGETS_KEY;

public class BroadcastMetadataTest {
    private static ObjectMapper mapper = new ObjectMapper();
    private static JsonNode prog;
    private static JsonNode empty;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = mapper.readTree(f);
        f = new File("./src/test/fixtures/bugpattern/broadcastSync.json");
        prog = mapper.readTree(f);
    }

    @Test
    public void testEmptyProgram() {
        BroadcastMetadataList monitors = BroadcastMetadataListParser.parse(empty.get(TARGETS_KEY).get(0)
                .get(BROADCASTS_KEY));
        Assertions.assertEquals(0, monitors.getList().size());
    }

    @Test
    public void testBroadcastsProgram() {
        BroadcastMetadataList monitors = BroadcastMetadataListParser.parse(prog.get(TARGETS_KEY).get(0)
                .get(BROADCASTS_KEY));
        Assertions.assertEquals(5, monitors.getList().size());
        Assertions.assertEquals("0kl{~LnXg|/EA][m;V;9", monitors.getList().get(0).getBroadcastID());
        Assertions.assertEquals("received", monitors.getList().get(0).getBroadcastName());
    }
}
