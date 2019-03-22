package utils.deserializer.scratch2;

import com.fasterxml.jackson.databind.JsonNode;
import scratch2.data.ScVariable;

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
        List<ScVariable> vars = new ArrayList<ScVariable>();
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
