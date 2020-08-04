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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.FieldsMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.InputMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.*;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class BlockMetadataParser {
    public static BlockMetadata parse(String blockId, JsonNode blockNode) throws ParsingException {
        if (blockNode.has(OPCODE_KEY)) {
            String commentId = null;
            if (blockNode.has(COMMENT_KEY)) {
                commentId = blockNode.get(COMMENT_KEY).asText();
            }
            String opcode = blockNode.get(OPCODE_KEY).asText();
            String next = null;
            if (blockNode.has(NEXT_KEY) && !(blockNode.get(NEXT_KEY) instanceof NullNode)) {
                next = blockNode.get(NEXT_KEY).asText();
            }
            String parent = null;
            if (blockNode.has(PARENT_KEY) && !(blockNode.get(PARENT_KEY) instanceof NullNode)) {
                parent = blockNode.get(PARENT_KEY).asText();
            }
            InputMetadataList inputMetadata = InputMetadataListParser.parse(blockNode.get(INPUTS_KEY));
            FieldsMetadataList fields = FieldsMetadataListParser.parse(blockNode.get(FIELDS_KEY));
            boolean topLevel = blockNode.get(TOPLEVEL_KEY).asBoolean();
            boolean shadow = blockNode.get(SHADOW_KEY).asBoolean();
            MutationMetadata mutation;
            if (blockNode.has(MUTATION_KEY)) {
                mutation = MutationMetadataParser.parse(blockNode.get(MUTATION_KEY));
            } else {
                mutation = new NoMutationMetadata();
            }
            if (!topLevel) {
                return new NonDataBlockMetadata(commentId, blockId, opcode, next, parent, inputMetadata, fields,
                        topLevel,
                        shadow,
                        mutation);
            }
            double x = blockNode.get(X_KEY).asDouble();
            double y = blockNode.get(Y_KEY).asDouble();
            return new TopNonDataBlockMetadata(commentId, blockId, opcode, next, parent, inputMetadata, fields,
                    topLevel,
                    shadow,
                    mutation, x, y);
        } else {
            Preconditions.checkArgument(blockNode instanceof ArrayNode, "This is neither a variable or list nor a "
                    + "NonDataBlock. ID: " + blockId);
            ArrayNode data = (ArrayNode) blockNode;
            Preconditions.checkArgument(data.size() == 5, "This data block does not have the required length for a "
                    + "top level data block. ID: " + blockId);
            int type = data.get(POS_INPUT_TYPE).asInt();
            String dataName = data.get(DATA_INPUT_NAME_POS).asText();
            String dataReference = data.get(DATA_INPUT_IDENTIFIER_POS).asText();
            double x = data.get(DATA_INPUT_X_POS).asDouble();
            double y = data.get(DATA_INPUT_Y_POS).asDouble();
            return new DataBlockMetadata(blockId, type, dataName, dataReference, x, y);
        }
    }
}
