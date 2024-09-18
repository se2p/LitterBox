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
package de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.Optional;

class RawBlockDeserializer extends JsonDeserializer<RawBlock> {

    @Override
    public RawBlock deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final ObjectMapper mapper = (ObjectMapper) p.getCodec();
        final TreeNode root = p.getCodec().readTree(p);

        if (root.isObject()) {
            return mapper.convertValue(root, RawBlock.RawRegularBlock.class);
        } else if (root.isArray()) {
            return deserializeArrayBlock((ArrayNode) root);
        } else {
            throw new JsonMappingException(p, "Scratch blocks have to be objects or arrays in JSON!");
        }
    }

    private RawBlock.ArrayBlock deserializeArrayBlock(final ArrayNode root) {
        final int blockType = root.get(0).asInt();
        return switch (blockType) {
            case 4, 5 -> new RawBlock.RawFloatBlockLiteral(root.get(1).asDouble());
            case 6, 7 -> new RawBlock.RawIntBlockLiteral(root.get(1).asLong());
            case 8 -> new RawBlock.RawAngleBlockLiteral(root.get(1).asDouble());
            case 9 -> new RawBlock.RawColorLiteral(root.get(1).asText());
            case 10 -> new RawBlock.RawStringLiteral(root.get(1).asText());
            case 11 -> {
                final String name = root.get(1).asText();
                final RawBlockId id = new RawBlockId(root.get(2).asText());
                yield new RawBlock.RawBroadcast(name, id);
            }
            case 12, 13 -> parseVariableOrListBlock(blockType, root);
            default -> throw new IllegalArgumentException("Unknown block type: " + blockType);
        };
    }

    private RawBlock.ArrayBlock parseVariableOrListBlock(int blockType, final ArrayNode root) {
        final String name = root.get(1).asText();
        final RawBlockId id = new RawBlockId(root.get(2).asText());

        final Optional<Coordinates> coordinates;
        if (root.size() == 5) {
            final double x = root.get(3).asDouble();
            final double y = root.get(4).asDouble();
            coordinates = Optional.of(new Coordinates(x, y));
        } else {
            coordinates = Optional.empty();
        }

        if (blockType == 12) {
            return new RawBlock.RawVariable(name, id, coordinates);
        } else {
            return new RawBlock.RawList(name, id, coordinates);
        }
    }
}
