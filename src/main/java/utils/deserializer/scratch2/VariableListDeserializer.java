/**
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
package utils.deserializer.scratch2;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.data.ScVariable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * More information about the JSON Scratch 2 file format and its JSON arrays and nodes:
 * https://en.scratch-wiki.info/wiki/Scratch_File_Format
 */
class VariableListDeserializer {

    /**
     * Deserialize the JSON String and creating a List<ScVariable>
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<ScVariable> with Scratch variables
     */
    static List<ScVariable> deserialize(JsonNode rootNode) {
        JsonNode globalVariables = rootNode.path("variables");
        Iterator<JsonNode> elements = globalVariables.elements();
        List<ScVariable> vars = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode variable = elements.next();
            ScVariable scvariable = new ScVariable();
            scvariable.setName(variable.get("name").asText());
            scvariable.setValue(variable.get("value").asText());
            if (variable.get("value").isNumber()) {
                scvariable.setNumber(true);
            }
            vars.add(scvariable);
        }
        return vars;
    }

}
