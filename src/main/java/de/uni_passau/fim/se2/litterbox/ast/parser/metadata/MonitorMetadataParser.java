package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MonitorListMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MonitorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MonitorSliderMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorParamMetadataList;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;


public class MonitorMetadataParser {

    public static MonitorMetadata parse(JsonNode monitorNode) {
        String id = monitorNode.get("id").asText();
        String mode = monitorNode.get("mode").asText();
        String opcode = monitorNode.get(Constants.OPCODE_KEY).asText();
        MonitorParamMetadataList paramsMetadata = MonitorParamMetadataListParser.parse(monitorNode.get("params"));
        String spriteName = null;
        if (!(monitorNode.get("spriteName") instanceof NullNode)) {
            spriteName = monitorNode.get("spriteName").asText();
        }
        int width = monitorNode.get("width").asInt();
        int height = monitorNode.get("height").asInt();
        int x = monitorNode.get(X_KEY).asInt();
        int y = monitorNode.get(Y_KEY).asInt();
        boolean visible = monitorNode.get(VISIBLE_KEY).asBoolean();
        JsonNode valueNode = monitorNode.get("value");
        if (valueNode instanceof ArrayNode) {
            List<String> values = new ArrayList<>();
            ArrayNode valuesArray = (ArrayNode) valueNode;
            for (int i = 0; i < valuesArray.size(); i++) {
                values.add(valuesArray.get(i).asText());
            }
            return new MonitorListMetadata(id, mode, opcode, paramsMetadata, spriteName, width, height, x, y,
                    visible, values);
        } else {
            String value = valueNode.asText();
            int sliderMin = monitorNode.get("sliderMin").asInt();
            int sliderMax = monitorNode.get("sliderMax").asInt();
            boolean isDiscrete = monitorNode.get("isDiscrete").asBoolean();
            return new MonitorSliderMetadata(id, mode, opcode, paramsMetadata, spriteName, width, height, x, y,
                    visible, value,
                    sliderMin, sliderMax, isDiscrete);
        }
    }
}
