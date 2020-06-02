package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorParamMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorListMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorSliderMetadata;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;


public class MonitorMetadataParser {

    public static MonitorMetadata parse(JsonNode monitorNode) {
        String id = monitorNode.get(ID_KEY).asText();
        String mode = monitorNode.get(MODE_KEY).asText();
        String opcode = monitorNode.get(Constants.OPCODE_KEY).asText();
        MonitorParamMetadataList paramsMetadata = MonitorParamMetadataListParser.parse(monitorNode.get(PARAMS_KEY));
        String spriteName = null;
        if (!(monitorNode.get(SPRITE_NAME_KEY) instanceof NullNode)) {
            spriteName = monitorNode.get(SPRITE_NAME_KEY).asText();
        }
        double width = monitorNode.get(WIDTH_KEY).asDouble();
        double height = monitorNode.get(HEIGHT_KEY).asDouble();
        double x = monitorNode.get(X_KEY).asDouble();
        double y = monitorNode.get(Y_KEY).asDouble();
        boolean visible = monitorNode.get(VISIBLE_KEY).asBoolean();
        JsonNode valueNode = monitorNode.get(VALUE_LOWER_KEY);
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
            double sliderMin = monitorNode.get(SLIDER_MIN_KEY).asDouble();
            double sliderMax = monitorNode.get(SLIDER_MAX_KEY).asDouble();
            boolean isDiscrete = monitorNode.get(IS_DISCRETE_KEY).asBoolean();
            return new MonitorSliderMetadata(id, mode, opcode, paramsMetadata, spriteName, width, height, x, y,
                    visible, value,
                    sliderMin, sliderMax, isDiscrete);
        }
    }
}
