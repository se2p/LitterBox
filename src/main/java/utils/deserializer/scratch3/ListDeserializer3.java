/*
 * Copyright (C) 2019 LitterBox contributors
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
package utils.deserializer.scratch3;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.data.ScList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ListDeserializer3 {

    /**
     * Deserialize the JSON String and creating a List<ScList> with ScList objects
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<ScList> with ScList objects
     */
    static List<ScList> deserialize(JsonNode rootNode) {
        JsonNode globalLists = rootNode.path("lists");
        Iterator<String> elements = globalLists.fieldNames();
        List<ScList> lists = new ArrayList<>();
        while (elements.hasNext()) {
            String id = elements.next();
            JsonNode n = globalLists.get(id);
            ScList scList = new ScList();
            scList.setId(id);
            List<String> list = new ArrayList<>();
            if (n.isArray()) {
                for (final JsonNode objNode : n) {
                    if(objNode.isArray()) {
                        for (final JsonNode obj : objNode) {
                            list.add(obj.asText());
                        }
                    } else {
                        scList.setName(objNode.asText());
                    }
                }
            }
            scList.setContent(list);
            lists.add(scList);
        }
        return lists;
    }

}
