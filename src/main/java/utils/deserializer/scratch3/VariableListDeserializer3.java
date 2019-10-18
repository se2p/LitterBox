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
package utils.deserializer.scratch3;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.data.ScVariable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class VariableListDeserializer3 {

    /**
     * Deserialize the JSON String and creating a List<ScVariable>
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<ScVariable> with Scratch variables
     */
    static List<ScVariable> deserialize(JsonNode rootNode) {
        JsonNode globalVariables = rootNode.path("variables");
        Iterator<String> elements = globalVariables.fieldNames();
        List<ScVariable> vars = new ArrayList<>();
        while (elements.hasNext()) {
            String id = elements.next();
            JsonNode n = globalVariables.get(id);
            ScVariable variable = new ScVariable();
            variable.setId(id);
            List<String> var = new ArrayList<>();
            if (n.isArray()) {
                for (final JsonNode objNode : n) {
                    var.add(objNode.asText());
                }
            }
            variable.setName(var.get(0));
            variable.setValue(var.get(1));
            variable.setNumber(isNumeric(var.get(1)));
            vars.add(variable);
        }
        return vars;
    }

    private static boolean isNumeric(String strNum) {
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

}
