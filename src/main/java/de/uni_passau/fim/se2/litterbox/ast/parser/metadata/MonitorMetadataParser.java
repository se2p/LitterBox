/*
 * Copyright (C) 2020 LitterBox contributors
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
        double width = 100;
        if (monitorNode.has(WIDTH_KEY)) {
            width = monitorNode.get(WIDTH_KEY).asDouble();
        }
        double height = 100;
        if (monitorNode.has(HEIGHT_KEY)) {
            height = monitorNode.get(HEIGHT_KEY).asDouble();
        }
        double x = 0;
        if (monitorNode.has(X_KEY)) {
            x = monitorNode.get(X_KEY).asDouble();
        }
        double y = 0;
        if (monitorNode.has(Y_KEY)) {
            y = monitorNode.get(Y_KEY).asDouble();
        }

        boolean visible = false;
        if (monitorNode.has(VISIBLE_KEY)) {
            visible = monitorNode.get(VISIBLE_KEY).asBoolean();
        }
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
            double sliderMin = 0;
            if (monitorNode.has(SLIDER_MIN_KEY)) {
                sliderMin = monitorNode.get(SLIDER_MIN_KEY).asDouble();
            } else if (monitorNode.has(MIN_KEY)) {
                sliderMin = monitorNode.get(MIN_KEY).asDouble();
            }
            double sliderMax = 100;
            if (monitorNode.has(SLIDER_MAX_KEY)) {
                sliderMax = monitorNode.get(SLIDER_MAX_KEY).asDouble();
            } else if (monitorNode.has(MAX_KEY)) {
                sliderMin = monitorNode.get(MAX_KEY).asDouble();
            }
            boolean isDiscrete = false;
            if (monitorNode.has(IS_DISCRETE_KEY)) {
                isDiscrete = monitorNode.get(IS_DISCRETE_KEY).asBoolean();
            }
            return new MonitorSliderMetadata(id, mode, opcode, paramsMetadata, spriteName, width, height, x, y,
                    visible, value,
                    sliderMin, sliderMax, isDiscrete);
        }
    }
}
