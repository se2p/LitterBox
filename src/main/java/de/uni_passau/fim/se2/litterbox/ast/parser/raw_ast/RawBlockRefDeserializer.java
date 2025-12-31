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
package de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast;

import tools.jackson.core.JsonParser;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.ValueNode;

class RawBlockRefDeserializer extends ValueDeserializer<BlockRef> {
    @Override
    public BlockRef deserialize(JsonParser p, DeserializationContext ctxt) {
        final TreeNode root = p.readValueAsTree();

        if (root.isValueNode()) {
            final ValueNode n = (ValueNode) root;
            return new BlockRef.IdRef(new RawBlockId(n.asString()));
        } else {
            try (final var parser = root.traverse(ctxt)) {
                final RawBlock input = parser.readValueAs(RawBlock.class);
                if (input instanceof RawBlock.ArrayBlock arrayBlock) {
                    return new BlockRef.Block(arrayBlock);
                } else {
                    throw new RawJsonException(p, "Encountered an invalid block input!");
                }
            }
        }
    }
}
