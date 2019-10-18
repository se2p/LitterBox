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
import scratch.data.Costume;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class CostumeDeserializer3 {

    /**
     * Deserialize the JSON String and creating a List<Costume> with Costume objects
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<Costume> with Costume objects
     */
    static List<Costume> deserialize(JsonNode rootNode) {
        JsonNode globalCostumes = rootNode.path("costumes");
        Iterator<JsonNode> elements = globalCostumes.elements();
        List<Costume> costumes = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode costume = elements.next();
            Costume scCostume = new Costume();
            scCostume.setName(costume.get("name").asText());
            scCostume.setAssetId(costume.get("assetId").asText());
            scCostume.setDataFormat(costume.get("dataFormat").asText());
            costumes.add(scCostume);
        }
        return costumes;
    }
}
