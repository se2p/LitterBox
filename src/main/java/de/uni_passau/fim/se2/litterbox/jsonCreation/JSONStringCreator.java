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

    public static String createProgramJSONString(Program program) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{\"" + TARGETS_KEY + "\": [");
        List<ActorDefinition> actorDefinitionList = program.getActorDefinitionList().getDefintions();
        for (int i = 0; i < actorDefinitionList.size() - 1; i++) {
            jsonString.append(ActorJSONCreator.createProgramJSONString(actorDefinitionList.get(i)));
            jsonString.append(",");
        }
        jsonString.append(ActorJSONCreator.createProgramJSONString(actorDefinitionList.get(actorDefinitionList.size() - 1)));
        jsonString.append("],");
        jsonString.append(createMonitorListJSONString(program)).append(",");
        jsonString.append(createExtensionJSONString(program)).append(",");
        jsonString.append(createMetaJSONString(program));
        jsonString.append("}");
        return jsonString.toString();
    }

    private static String createExtensionJSONString(Program program) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("\"" + EXTENSIONS_KEY + "\": [");
        List<String> ext = program.getMetadata().getExtension().getExtensionNames();
        for (int i = 0; i < ext.size() - 1; i++) {
            jsonString.append("\"").append(ext.get(i)).append("\"").append(",");
        }
        if (ext.size() > 0) {
            jsonString.append("\"").append(ext.get(ext.size() - 1)).append("\"");
        }
        jsonString.append("]");
        return jsonString.toString();
    }

    private static StringBuilder createMetaJSONString(Program program) {
        StringBuilder jsonString = new StringBuilder();
        MetaMetadata meta = program.getMetadata().getMeta();
        jsonString.append("\"").append(META_KEY).append("\": {");
        jsonString.append("\"" + SEMVER_KEY + "\": \"").append(meta.getSemver()).append("\"").append(",");
        jsonString.append("\"" + VM_KEY + "\": \"").append(meta.getVm()).append("\"").append(",");
        jsonString.append("\"" + AGENT_KEY + "\": \"").append(meta.getAgent()).append("\"");
        jsonString.append("}");
        return jsonString;
    }


    private static StringBuilder createMonitorListJSONString(Program program) {
        StringBuilder jsonString = new StringBuilder();
        List<MonitorMetadata> monitorMetadataList = program.getMetadata().getMonitor().getList();
        jsonString.append("\"" + MONITORS_KEY + "\": [");
        for (int i = 0; i < monitorMetadataList.size() - 1; i++) {
            jsonString.append(createMonitorJSONString(monitorMetadataList.get(i))).append(",");
        }
        if (monitorMetadataList.size() > 0) {
            jsonString.append(createMonitorJSONString(monitorMetadataList.get(monitorMetadataList.size() - 1)));
        }
        jsonString.append("]");
        return jsonString;
    }

    private static StringBuilder createMonitorJSONString(MonitorMetadata metadata) {
        StringBuilder jsonString = new StringBuilder();
        boolean isSlider = metadata instanceof MonitorSliderMetadata;
        MonitorSliderMetadata slider = null;
        MonitorListMetadata list = null;

        if (isSlider) {
            slider = (MonitorSliderMetadata) metadata;
        } else {
            list = (MonitorListMetadata) metadata;
        }

        jsonString.append("{");
        jsonString.append("\"" + ID_KEY + "\": \"").append(metadata.getId()).append("\"").append(",");
        jsonString.append("\"" + MODE_KEY + "\": \"").append(metadata.getMode()).append("\"").append(",");
        jsonString.append("\"" + OPCODE_KEY + "\": \"").append(metadata.getOpcode()).append("\"").append(",");
        jsonString.append("\"" + PARAMS_KEY + "\": {");
        List<MonitorParamMetadata> monitors = metadata.getParamsMetadata().getList();
        for (int i = 0; i < monitors.size() - 1; i++) {
            jsonString.append("\"").append(monitors.get(i).getInputName()).append("\": \"").append(monitors.get(i).getInputValue()).append("\"").append(",");
        }
        if (monitors.size() > 0) {
            jsonString.append("\"").append(monitors.get(monitors.size() - 1).getInputName()).append("\": \"").append(monitors.get(monitors.size() - 1).getInputValue()).append("\"");
        }
        jsonString.append("},");

        jsonString.append("\"" + SPRITE_NAME_KEY + "\":");
        if (metadata.getSpriteName() == null) {
            jsonString.append(metadata.getSpriteName()).append(",");
        } else {
            jsonString.append("\"").append(metadata.getSpriteName()).append("\",");
        }

        jsonString.append("\"" + VALUE_LOWER_KEY + "\": ");
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
        jsonString.append("\"" + WIDTH_KEY + "\": ").append(metadata.getWidth()).append(",");
        jsonString.append("\"" + HEIGHT_KEY + "\": ").append(metadata.getHeight()).append(",");
        jsonString.append("\"" + X_KEY + "\": ").append(metadata.getX()).append(",");
        jsonString.append("\"" + Y_KEY + "\": ").append(metadata.getY()).append(",");
        jsonString.append("\"" + VISIBLE_KEY + "\": ").append(metadata.isVisible());

        if (isSlider) {
            jsonString.append(",");
            jsonString.append("\"" + SLIDER_MIN_KEY + "\": ").append(slider.getSliderMin()).append(",");
            jsonString.append("\"" + SLIDER_MAX_KEY + "\": ").append(slider.getSliderMax()).append(",");
            jsonString.append("\"" + IS_DISCRETE_KEY + "\": ").append(slider.isDiscrete());
        }

        jsonString.append("}");
        return jsonString;
    }
}
