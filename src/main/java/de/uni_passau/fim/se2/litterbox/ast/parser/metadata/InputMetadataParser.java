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
import com.fasterxml.jackson.databind.node.TextNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.DataInputMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.InputMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.ReferenceInputMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.TypeInputMetadata;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class InputMetadataParser {
    public static InputMetadata parse(String inputName, JsonNode inputNode) {
        Preconditions.checkArgument(inputNode instanceof ArrayNode, "The input is not an ArrayNode.");
        ArrayNode inputArray = (ArrayNode) inputNode;
        if (inputArray.get(POS_BLOCK_ID) instanceof TextNode) {
            return new ReferenceInputMetadata(inputName, inputArray.get(POS_BLOCK_ID).asText());
        } else if (inputArray.get(POS_BLOCK_ID) instanceof NullNode) {
            return new ReferenceInputMetadata(inputName, null);
        } else {
            Preconditions.checkArgument(inputNode.get(POS_BLOCK_ID) instanceof ArrayNode, "The entry at POS_BLOCK_ID "
                    + "is not an "
                    + "ArrayNode.");
            ArrayNode valueArray = (ArrayNode) inputNode.get(POS_BLOCK_ID);
            int type = valueArray.get(POS_INPUT_TYPE).asInt();
            if (type == VAR_PRIMITIVE || type == LIST_PRIMITIVE || type == BROADCAST_PRIMITIVE) {
                String dataName = valueArray.get(DATA_INPUT_NAME_POS).asText();
                String identifier = valueArray.get(DATA_INPUT_IDENTIFIER_POS).asText();
                return new DataInputMetadata(inputName, type, dataName, identifier);
            } else {
                String value = valueArray.get(POS_INPUT_VALUE).textValue();
                return new TypeInputMetadata(inputName, type, value);
            }
        }
    }
}
