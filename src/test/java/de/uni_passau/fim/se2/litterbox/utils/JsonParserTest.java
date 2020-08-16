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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonParserTest {

    @Test
    public void testGetBlockNode() {
        JsonNode node = JsonParser.getBlocksNodeFromJSON("./src/test/fixtures/stmtParser/changeLayerBy.json");
        Assertions.assertEquals("{\"J-bCHx$3S*!PBsa2/8mU\":{\"opcode\":\"looks_goforwardbackwardlayers\",\"next\":\"ifW%0O=`%`j(2?Cm1W9O\",\"parent\":null,\"inputs\":{\"NUM\":[1,[7,\"1\"]]},\"fields\":{\"FORWARD_BACKWARD\":[\"forward\",null]},\"shadow\":false,\"topLevel\":true,\"x\":528,\"y\":444},\"ifW%0O=`%`j(2?Cm1W9O\":{\"opcode\":\"looks_goforwardbackwardlayers\",\"next\":null,\"parent\":\"J-bCHx$3S*!PBsa2/8mU\",\"inputs\":{\"NUM\":[1,[7,\"1\"]]},\"fields\":{\"FORWARD_BACKWARD\":[\"backward\",null]},\"shadow\":false,\"topLevel\":false}}", node.toString());
    }
}