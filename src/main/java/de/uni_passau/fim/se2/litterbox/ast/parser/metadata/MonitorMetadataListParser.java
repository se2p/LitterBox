package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorMetadataList;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class MonitorMetadataListParser {
    public static MonitorMetadataList parse(JsonNode monitorsNode) {
        List<MonitorMetadata> monitorsList = new ArrayList<>();
        Preconditions.checkArgument(monitorsNode instanceof ArrayNode);
        ArrayNode monitorsArray = (ArrayNode) monitorsNode;
        for (int i = 0; i < monitorsArray.size(); i++) {
            monitorsList.add(MonitorMetadataParser.parse(monitorsArray.get(i)));
        }
        return new MonitorMetadataList(monitorsList);
    }
}
