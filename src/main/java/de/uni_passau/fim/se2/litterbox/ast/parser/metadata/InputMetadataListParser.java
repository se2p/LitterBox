package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.InputMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.InputMetadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class InputMetadataListParser {
    public static InputMetadataList parse(JsonNode inputsNode) {
        List<InputMetadata> inputMetadataList = new ArrayList<>();
        Iterator<Entry<String, JsonNode>> entries = inputsNode.fields();
        while (entries.hasNext()) {
            Entry<String, JsonNode> current = entries.next();
            inputMetadataList.add(InputMetadataParser.parse(current.getKey(), current.getValue()));
        }
        return new InputMetadataList(inputMetadataList);
    }
}
