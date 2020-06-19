package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.FieldsMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.FieldsMetadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FieldsMetadataListParser {
    public static FieldsMetadataList parse(JsonNode fieldsNode) {
        List<FieldsMetadata> fieldsMetadataList = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> entries = fieldsNode.fields();
        while (entries.hasNext()) {
            Map.Entry<String, JsonNode> current = entries.next();
            fieldsMetadataList.add(FieldsMetadataParser.parse(current.getKey(), current.getValue()));
        }
        return new FieldsMetadataList(fieldsMetadataList);
    }
}
