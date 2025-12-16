/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast;

import tools.jackson.core.JsonParser;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ValueNode;

import java.util.Optional;

class RawBlockDeserializer extends StdDeserializer<RawBlock> {

    protected RawBlockDeserializer() {
        super(RawBlock.class);
    }

    @Override
    public RawBlock deserialize(JsonParser p, DeserializationContext ctxt) {
        final TreeNode root = p.readValueAsTree();

        if (root.isObject()) {
            try (final var parser = root.traverse(ctxt)) {
                return parser.readValueAs(RawBlock.RawRegularBlock.class);
            }
        } else if (root.isArray()) {
            return deserializeArrayBlock((ArrayNode) root);
        } else {
            throw new RawJsonException(p, "Scratch blocks have to be objects or arrays in JSON!");
        }
    }

    private RawBlock.ArrayBlock deserializeArrayBlock(final ArrayNode root) {
        // The blockType just indicates what it *should* be if the user filled in the literal blocks as intended.
        // E.g. in case a number is expected, the block type will still be 4 even if the user filled in a random string
        // instead of a number.
        final int blockType = root.get(0).asInt();

        return switch (blockType) {
            case 4, 5 -> {
                if (root.get(1) instanceof ValueNode v && v.isNumber()) {
                    yield new RawBlock.RawFloatBlockLiteral(v.asDouble());
                } else {
                    yield new RawBlock.RawStringLiteral(root.get(1).asString());
                }
            }
            case 6, 7 -> {
                if (root.get(1) instanceof ValueNode v && v.isLong()) {
                    yield new RawBlock.RawIntBlockLiteral(v.asLong());
                } else {
                    yield new RawBlock.RawStringLiteral(root.get(1).asString());
                }
            }
            case 8 -> {
                if (root.get(1) instanceof ValueNode v && v.isNumber()) {
                    yield new RawBlock.RawAngleBlockLiteral(v.asDouble());
                } else {
                    yield new RawBlock.RawStringLiteral(root.get(1).asString());
                }
            }
            case 9 -> new RawBlock.RawColorLiteral(root.get(1).asString());
            case 10 -> new RawBlock.RawStringLiteral(root.get(1).asString());
            case 11 -> {
                final String name = root.get(1).asString();
                final RawBlockId id = new RawBlockId(root.get(2).asString());
                yield new RawBlock.RawBroadcast(name, id);
            }
            case 12, 13 -> parseVariableOrListBlock(blockType, root);
            default -> throw new IllegalArgumentException("Unknown block type: " + blockType);
        };
    }

    private RawBlock.ArrayBlock parseVariableOrListBlock(int blockType, final ArrayNode root) {
        final String name = root.get(1).asString();
        final RawBlockId id = new RawBlockId(root.get(2).asString());

        final Optional<Coordinates> coordinates;
        final Optional<RawBlockId> commentId;
        if (root.size() == 6) {
            commentId = Optional.of(new RawBlockId(root.get(3).asString()));
            final double x = root.get(4).asDouble();
            final double y = root.get(5).asDouble();
            coordinates = Optional.of(new Coordinates(x, y));
        } else if (root.size() == 5) {
            final double x = root.get(3).asDouble();
            final double y = root.get(4).asDouble();
            coordinates = Optional.of(new Coordinates(x, y));
            commentId = Optional.empty();
        } else if (root.size() == 4) {
            commentId = Optional.of(new RawBlockId(root.get(3).asString()));
            coordinates = Optional.empty();
        } else {
            coordinates = Optional.empty();
            commentId = Optional.empty();
        }

        if (blockType == 12) {
            return new RawBlock.RawVariable(name, id, commentId, coordinates);
        } else {
            return new RawBlock.RawList(name, id, commentId, coordinates);
        }
    }
}
