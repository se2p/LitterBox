package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MonitorParamMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorParamMetadataList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class MonitorParamMetadataListParser {

    public static MonitorParamMetadataList parse(JsonNode monitorParamsNode) {
        List<MonitorParamMetadata> params = new ArrayList<>();
        Iterator<Entry<String, JsonNode>> entries = monitorParamsNode.fields();
        while (entries.hasNext()) {
            Entry<String, JsonNode> current = entries.next();
            params.add(new MonitorParamMetadata(current.getKey(), current.getValue().asText()));
        }
        return new MonitorParamMetadataList(params);
    }
}
