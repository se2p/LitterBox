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
package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorListMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorParamMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorSliderMetadata;

import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class JSONStringCreator {
    private static final String NULL_VALUE = null;

    public static String createProgramJSONString(Program program) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{");
        createField(jsonString, TARGETS_KEY).append("[");
        List<ActorDefinition> actorDefinitionList = program.getActorDefinitionList().getDefinitions();
        for (int i = 0; i < actorDefinitionList.size() - 1; i++) {
            jsonString.append(ActorJSONCreator.createActorJSONString(actorDefinitionList.get(i),
                    program.getSymbolTable(), program.getProcedureMapping()));
            jsonString.append(",");
        }
        jsonString.append(ActorJSONCreator.createActorJSONString(actorDefinitionList.get(actorDefinitionList.size() - 1)
                , program.getSymbolTable(), program.getProcedureMapping()));
        jsonString.append("],");
        createMonitorListJSONString(jsonString, program).append(",");
        createExtensionJSONString(jsonString, program).append(",");
        createMetaJSONString(jsonString, program);
        jsonString.append("}");
        return jsonString.toString();
    }

    private static StringBuilder createExtensionJSONString(StringBuilder jsonString, Program program) {
        createField(jsonString, EXTENSIONS_KEY).append("[");
        List<String> ext = program.getProgramMetadata().getExtension().getExtensionNames();
        for (int i = 0; i < ext.size() - 1; i++) {
            jsonString.append("\"").append(ext.get(i)).append("\"").append(",");
        }
        if (ext.size() > 0) {
            jsonString.append("\"").append(ext.get(ext.size() - 1)).append("\"");
        }
        jsonString.append("]");
        return jsonString;
    }

    private static StringBuilder createMetaJSONString(StringBuilder jsonString, Program program) {
        MetaMetadata meta = program.getProgramMetadata().getMeta();
        createField(jsonString, META_KEY).append("{");
        createFieldValue(jsonString, SEMVER_KEY, meta.getSemver()).append(",");
        createFieldValue(jsonString, VM_KEY, meta.getVm()).append(",");
        createFieldValue(jsonString, AGENT_KEY, meta.getAgent());
        jsonString.append("}");
        return jsonString;
    }

    public static StringBuilder createField(StringBuilder jsonString, String fieldName) {
        return jsonString.append("\"").append(fieldName).append("\": ");
    }

    public static StringBuilder createFieldValue(StringBuilder jsonString, String fieldName, String fieldValue) {
        return createField(jsonString, fieldName).append("\"").append(fieldValue).append("\"");
    }

    public static StringBuilder createFieldValueNull(StringBuilder jsonString, String fieldName) {
        return createField(jsonString, fieldName).append(NULL_VALUE);
    }

    public static StringBuilder createFieldValue(StringBuilder jsonString, String fieldName, double fieldValue) {
        return createField(jsonString, fieldName).append(fieldValue);
    }

    public static StringBuilder createFieldValue(StringBuilder jsonString, String fieldName, int fieldValue) {
        return createField(jsonString, fieldName).append(fieldValue);
    }

    public static StringBuilder createFieldValue(StringBuilder jsonString, String fieldName, boolean fieldValue) {
        return createField(jsonString, fieldName).append(fieldValue);
    }

    private static StringBuilder createMonitorListJSONString(StringBuilder jsonString, Program program) {
        List<MonitorMetadata> monitorMetadataList = program.getProgramMetadata().getMonitor().getList();
        createField(jsonString, MONITORS_KEY).append("[");
        for (int i = 0; i < monitorMetadataList.size() - 1; i++) {
            createMonitorJSONString(jsonString, monitorMetadataList.get(i)).append(",");
        }
        if (monitorMetadataList.size() > 0) {
            createMonitorJSONString(jsonString,
                    monitorMetadataList.get(monitorMetadataList.size() - 1));
        }
        jsonString.append("]");
        return jsonString;
    }

    private static StringBuilder createMonitorJSONString(StringBuilder jsonString, MonitorMetadata metadata) {
        boolean isSlider = metadata instanceof MonitorSliderMetadata;
        MonitorSliderMetadata slider = null;
        MonitorListMetadata list = null;

        if (isSlider) {
            slider = (MonitorSliderMetadata) metadata;
        } else {
            list = (MonitorListMetadata) metadata;
        }

        jsonString.append("{");
        createFieldValue(jsonString, ID_KEY, metadata.getId()).append(",");
        createFieldValue(jsonString, MODE_KEY, metadata.getMode()).append(",");
        createFieldValue(jsonString, OPCODE_KEY, metadata.getOpcode()).append(",");

        createField(jsonString, PARAMS_KEY).append("{");
        List<MonitorParamMetadata> monitors = metadata.getParamsMetadata().getList();
        for (int i = 0; i < monitors.size() - 1; i++) {
            createFieldValue(jsonString, monitors.get(i).getInputName(), monitors.get(i).getInputValue()).append(",");
        }
        if (monitors.size() > 0) {
            createFieldValue(jsonString, monitors.get(monitors.size() - 1).getInputName(),
                    monitors.get(monitors.size() - 1).getInputValue());
        }
        jsonString.append("},");

        createField(jsonString, SPRITE_NAME_KEY);
        if (metadata.getSpriteName() == null) {
            jsonString.append(metadata.getSpriteName()).append(",");
        } else {
            jsonString.append("\"").append(metadata.getSpriteName()).append("\",");
        }

        createField(jsonString, VALUE_LOWER_KEY);
        if (isSlider) {
            jsonString.append("\"").append(slider.getValue()).append("\"");
        } else {
            jsonString.append("[");
            List<String> values = list.getValues();
            for (int i = 0; i < values.size() - 1; i++) {
                jsonString.append("\"").append(values.get(i)).append("\"").append(",");
            }
            if (values.size() > 0) {
                jsonString.append("\"").append(values.get(values.size() - 1)).append("\"");
            }
            jsonString.append("]");
        }

        jsonString.append(",");
        createFieldValue(jsonString, WIDTH_KEY, metadata.getWidth()).append(",");
        createFieldValue(jsonString, HEIGHT_KEY, metadata.getHeight()).append(",");
        createFieldValue(jsonString, X_KEY, metadata.getX()).append(",");
        createFieldValue(jsonString, Y_KEY, metadata.getY()).append(",");
        createFieldValue(jsonString, VISIBLE_KEY, metadata.isVisible());

        if (isSlider) {
            jsonString.append(",");
            createFieldValue(jsonString, SLIDER_MIN_KEY, ((MonitorSliderMetadata) metadata).getSliderMin()).append(",");
            createFieldValue(jsonString, SLIDER_MAX_KEY, ((MonitorSliderMetadata) metadata).getSliderMax()).append(",");
            createFieldValue(jsonString, IS_DISCRETE_KEY, ((MonitorSliderMetadata) metadata).isDiscrete());
        }

        jsonString.append("}");
        return jsonString;
    }
}
