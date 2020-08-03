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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.VariableMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.VariableMetadataList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.DECLARATION_VARIABLE_NAME_POS;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.DECLARATION_VARIABLE_VALUE_POS;

public class VariableMetadataListParser {

    public static VariableMetadataList parse(JsonNode variablesNode) {
        List<VariableMetadata> variableMetadataList = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> entries = variablesNode.fields();
        while (entries.hasNext()) {
            Map.Entry<String, JsonNode> current = entries.next();
            variableMetadataList.add(new VariableMetadata(
                    current.getKey(),
                    current.getValue().get(DECLARATION_VARIABLE_NAME_POS).asText(),
                    current.getValue().get(DECLARATION_VARIABLE_VALUE_POS).asText()));
        }
        return new VariableMetadataList(variableMetadataList);
    }
}
