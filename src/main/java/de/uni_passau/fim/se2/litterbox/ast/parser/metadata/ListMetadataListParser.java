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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ListMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.ListMetadataList;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.DECLARATION_LIST_NAME_POS;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.DECLARATION_LIST_VALUES_POS;

public class ListMetadataListParser {
    public static ListMetadataList parse(JsonNode listsNode) {
        List<ListMetadata> listMetadataList = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> entries = listsNode.fields();
        while (entries.hasNext()) {
            Map.Entry<String, JsonNode> current = entries.next();
            JsonNode valuesNode = current.getValue().get(DECLARATION_LIST_VALUES_POS);
            Preconditions.checkArgument(valuesNode instanceof ArrayNode);
            ArrayNode valuesArray = (ArrayNode) valuesNode;
            List<String> valuesList = new ArrayList<>();
            for (int i = 0; i < valuesArray.size(); i++) {
                valuesList.add(valuesArray.get(i).asText());
            }
            listMetadataList.add(new ListMetadata(current.getKey(),
                    current.getValue().get(DECLARATION_LIST_NAME_POS).asText(),
                    valuesList));
        }
        return new ListMetadataList(listMetadataList);
    }
}
