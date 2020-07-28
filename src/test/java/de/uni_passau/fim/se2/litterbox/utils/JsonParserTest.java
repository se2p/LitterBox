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