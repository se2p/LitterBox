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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.FieldsMetadata;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.FIELD_REFERENCE;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.FIELD_VALUE;

public class FieldsMetadataParser {

    public static FieldsMetadata parse(String key, JsonNode fieldNode) {
        Preconditions.checkArgument(fieldNode instanceof ArrayNode, "The field is not an ArrayNode.");
        String value = fieldNode.get(FIELD_VALUE).asText();
        String reference = null;
        if (fieldNode.has(FIELD_REFERENCE) && !(fieldNode.get(FIELD_REFERENCE) instanceof NullNode)) {
            reference = fieldNode.get(FIELD_REFERENCE).asText();
        }
        return new FieldsMetadata(key, value, reference);
    }
}
