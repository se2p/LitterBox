package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.VariableMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.VariableMetadataList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class VariableMetadataListParser {

    public static VariableMetadataList parse(JsonNode variablesNode) {
        List<VariableMetadata> variableMetadataList = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> entries = variablesNode.fields();
        while (entries.hasNext()) {
            Map.Entry<String, JsonNode> current = entries.next();
            variableMetadataList.add(new VariableMetadata(current.getKey(), current.getValue().get(0).asText(),
                    current.getValue().get(1).asText()));
        }
        return new VariableMetadataList(variableMetadataList);
    }
}
