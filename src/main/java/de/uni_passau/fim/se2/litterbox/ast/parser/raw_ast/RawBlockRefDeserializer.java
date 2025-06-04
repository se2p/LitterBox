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
package de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.IOException;

class RawBlockRefDeserializer extends JsonDeserializer<BlockRef> {
    @Override
    public BlockRef deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final ObjectMapper mapper = (ObjectMapper) p.getCodec();
        final TreeNode root = p.getCodec().readTree(p);

        if (root.isValueNode()) {
            final ValueNode n = (ValueNode) root;
            return new BlockRef.IdRef(new RawBlockId(n.asText()));
        } else {
            final RawBlock input = mapper.convertValue(root, RawBlock.class);
            if (input instanceof RawBlock.ArrayBlock arrayBlock) {
                return new BlockRef.Block(arrayBlock);
            } else {
                throw new JsonMappingException(p, "Encountered an invalid block input!");
            }
        }
    }
}
