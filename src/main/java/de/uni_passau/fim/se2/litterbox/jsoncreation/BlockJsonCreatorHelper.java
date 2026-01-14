/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.jsoncreation;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;

import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsoncreation.JSONStringCreator.createField;
import static de.uni_passau.fim.se2.litterbox.jsoncreation.JSONStringCreator.createFieldValue;

public abstract class BlockJsonCreatorHelper {

    public static final String DEFAULT_VALUE = "[10,\"\"]";

    public static StringBuilder createBlockUpToParent(StringBuilder jsonString, BlockMetadata meta,
                                                      String nextId, String parentId, Opcode opcode) {

        createField(jsonString, meta.getBlockId()).append("{");
        createFieldValue(jsonString, OPCODE_KEY, opcode.getName()).append(",");
        createFieldValue(jsonString, NEXT_KEY, nextId).append(",");
        createFieldValue(jsonString, PARENT_KEY, parentId);
        return jsonString;
    }

    public static StringBuilder createBlockInputFieldForFixed(StringBuilder jsonString) {
        createField(jsonString, INPUTS_KEY).append("{},");
        createField(jsonString, FIELDS_KEY).append("{}");
        return jsonString;
    }

    public static StringBuilder createBlockAfterFields(StringBuilder jsonString, BlockMetadata meta) {
        boolean shadow = false;
        if (meta instanceof NonDataBlockMetadata nonData) {
            shadow = nonData.isShadow();
        }
        createFieldValue(jsonString, SHADOW_KEY, shadow).append(",");

        boolean topLevel = false;
        double x = 0;
        double y = 0;

        if (meta instanceof TopNonDataBlockMetadata topMeta) {
            topLevel = true;
            x = topMeta.getXPos();
            y = topMeta.getYPos();
        } else if (meta instanceof DataBlockMetadata dataMeta) {
            topLevel = true; // Data blocks are often top-level but treated differently, but if we are generating blocks for them they likely have coords
            x = dataMeta.getX();
            y = dataMeta.getY();
        }

        createFieldValue(jsonString, TOPLEVEL_KEY, topLevel);
        if (meta.getCommentId() != null) {
            jsonString.append(",");
            createFieldValue(jsonString, COMMENT_KEY, meta.getCommentId());
        }
        if (topLevel) {
            jsonString.append(",");
            createFieldValue(jsonString, X_KEY, x).append(",");
            createFieldValue(jsonString, Y_KEY, y);
        }
        return jsonString;
    }

    public static String createFixedBlock(BlockMetadata meta,
                                          String nextId, String parentId, Opcode opcode) {
        StringBuilder jsonString = new StringBuilder();
        createBlockUpToParent(jsonString, meta, nextId, parentId, opcode).append(",");
        createBlockInputFieldForFixed(jsonString).append(",");
        createBlockAfterFields(jsonString, meta).append("}");
        return jsonString.toString();
    }

    private static StringBuilder createBlockString(BlockMetadata metadata, String nextId, String parentId,
                                                   String inputsString,
                                                   String fieldsString, Opcode opcode) {
        StringBuilder jsonString = new StringBuilder();
        createBlockUpToParent(jsonString, metadata, nextId, parentId, opcode).append(",");
        createField(jsonString, INPUTS_KEY).append(inputsString).append(",");
        createField(jsonString, FIELDS_KEY).append(fieldsString).append(",");
        createBlockAfterFields(jsonString, metadata);
        return jsonString;
    }

    public static String createBlockWithMutationString(BlockMetadata metadata, String nextId, String parentId,
                                                       String inputsString,
                                                       String fieldsString, String mutation, Opcode opcode) {
        StringBuilder jsonString = createBlockString(metadata, nextId, parentId, inputsString, fieldsString, opcode);
        jsonString.append(",");
        createField(jsonString, MUTATION_KEY).append(mutation);
        jsonString.append("}");
        return jsonString.toString();
    }

    public static String createBlockWithoutMutationString(BlockMetadata metadata, String nextId, String parentId,
                                                          String inputsString,
                                                          String fieldsString, Opcode opcode) {
        StringBuilder jsonString = createBlockString(metadata, nextId, parentId, inputsString, fieldsString, opcode);
        jsonString.append("}");
        return jsonString.toString();
    }

    public static String createFields(String fieldName, String fieldValue, String fieldReference) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{");
        createField(jsonString, fieldName).append("[");
        jsonString.append("\"").append(escape(fieldValue)).append("\",");
        if (fieldReference == null) {
            //No " needed because it should represent the value null
            jsonString.append((String) null);
        } else {
            jsonString.append("\"").append(escape(fieldReference)).append("\"");
        }
        jsonString.append("]}");
        return jsonString.toString();
    }

    public static String createStopMetadata(boolean hasNext) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{");
        createFieldValue(jsonString, TAG_NAME_KEY, "mutation").append(",");
        createField(jsonString, CHILDREN_KEY).append("[],");
        createFieldValue(jsonString, HAS_NEXT_KEY, hasNext);
        jsonString.append("}");
        return jsonString.toString();
    }

    public static String createPrototypeMetadata(String proccode, List<String> argumentId,
                                                 List<ParameterInfo> parameterInfos,
                                                 boolean warp) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{");
        createFieldValue(jsonString, TAG_NAME_KEY, "mutation").append(",");
        createField(jsonString, CHILDREN_KEY).append("[],");
        createFieldValue(jsonString, PROCCODE_KEY, proccode).append(",");
        createField(jsonString, ARGUMENTIDS_KEY);
        createArgumentIds(jsonString, argumentId).append(",");
        createField(jsonString, ARGUMENTNAMES_KEY);
        createArgumentNames(jsonString, parameterInfos).append(",");
        createField(jsonString, ARGUMENT_DEFAULTS_KEY);
        createArgumentDefaults(jsonString, parameterInfos).append(",");
        createFieldValue(jsonString, WARP_KEY, warp);
        jsonString.append("}");
        return jsonString.toString();
    }

    public static String createCallMetadata(String proccode, List<String> argumentId,
                                            boolean warp) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{");
        createFieldValue(jsonString, TAG_NAME_KEY, "mutation").append(",");
        createField(jsonString, CHILDREN_KEY).append("[],");
        createFieldValue(jsonString, PROCCODE_KEY, proccode).append(",");
        createField(jsonString, ARGUMENTIDS_KEY);
        createArgumentIds(jsonString, argumentId).append(",");
        createFieldValue(jsonString, WARP_KEY, warp);
        jsonString.append("}");
        return jsonString.toString();
    }

    private static StringBuilder createArgumentDefaults(StringBuilder jsonString, List<ParameterInfo> parameterInfos) {
        jsonString.append("\"[");
        for (int i = 0; i < parameterInfos.size() - 1; i++) {
            jsonString.append(parameterInfos.get(i).getDefaultValue()).append(",");
        }
        if (!parameterInfos.isEmpty()) {
            jsonString.append(parameterInfos.getLast().getDefaultValue());
        }
        jsonString.append("]\"");
        return jsonString;
    }

    private static StringBuilder createArgumentNames(StringBuilder jsonString, List<ParameterInfo> parameterInfos) {
        jsonString.append("\"[");
        for (int i = 0; i < parameterInfos.size() - 1; i++) {
            jsonString.append("\\\"").append(parameterInfos.get(i).getName()).append("\\\"").append(",");
        }
        if (!parameterInfos.isEmpty()) {
            jsonString.append("\\\"").append(parameterInfos.getLast().getName()).append("\\\"");
        }
        jsonString.append("]\"");
        return jsonString;
    }

    private static StringBuilder createArgumentIds(StringBuilder jsonString, List<String> argumentId) {
        jsonString.append("\"[");
        for (int i = 0; i < argumentId.size() - 1; i++) {
            jsonString.append("\\\"").append(argumentId.get(i)).append("\\\"").append(",");
        }
        if (!argumentId.isEmpty()) {
            jsonString.append("\\\"").append(argumentId.getLast()).append("\\\"");
        }
        jsonString.append("]\"");
        return jsonString;
    }

    public static String createReferenceInput(String inputName, int shadowIndicator, String reference,
                                              boolean withDefault) {
        StringBuilder jsonString = new StringBuilder();
        createField(jsonString, inputName).append("[").append(shadowIndicator).append(",");
        if (reference == null) {
            //In this case no " are needed around the null because it represents the value null
            jsonString.append((String) null);
        } else {
            jsonString.append("\"").append(escape(reference)).append("\"");
        }
        if (withDefault) {
            jsonString.append(",").append(DEFAULT_VALUE);
        }
        jsonString.append("]");
        return jsonString.toString();
    }

    public static String createInputs(List<String> inputsList) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{");
        for (int i = 0; i < inputsList.size() - 1; i++) {
            jsonString.append(inputsList.get(i)).append(",");
        }
        if (!inputsList.isEmpty()) {
            jsonString.append(inputsList.getLast());
        }
        jsonString.append("}");
        return jsonString.toString();
    }

    public static String createTypeInputWithName(String inputName, int shadowIndicator, int typeNumber,
                                                 String value) {
        StringBuilder jsonString = new StringBuilder();
        createField(jsonString, inputName).append(createTypeInput(shadowIndicator, typeNumber, value));
        return jsonString.toString();
    }

    public static String createTypeInput(int shadowIndicator, int typeNumber, String value) {
        return "["
                + shadowIndicator
                + ",["
                + typeNumber
                + ",\""
                + escape(value)
                + "\"]]";
    }

    public static String createReferenceTypeInput(String inputName, int shadowIndicator, int typeNumber,
                                                  String value, String reference, boolean withDefault) {
        StringBuilder jsonString = new StringBuilder();
        createField(jsonString, inputName).append(createReferenceType(shadowIndicator, typeNumber, value, reference,
                withDefault));
        return jsonString.toString();
    }

    public static String createReferenceType(int shadowIndicator, int typeNumber,
                                             String value, String reference, boolean withDefault) {
        StringBuilder jsonString = new StringBuilder();
        value = escape(value);
        jsonString.append("[").append(shadowIndicator)
                .append(",")
                .append("[")
                .append(typeNumber)
                .append(",\"")
                .append(value)
                .append("\",\"")
                .append(escape(reference))
                .append("\"]");

        if (withDefault) {
            jsonString.append(",").append(DEFAULT_VALUE);
        }
        jsonString.append("]");
        return jsonString.toString();
    }

    public static String createReferenceJSON(
            String blockId, String inputName, boolean withDefault) {
        if (blockId == null) {
            return createReferenceInput(inputName, INPUT_SAME_BLOCK_SHADOW, null, false);
        } else {
            return createReferenceInput(inputName, INPUT_DIFF_BLOCK_SHADOW,
                    blockId, withDefault);
        }
    }

    public static String escape(String jsString) {
        jsString = jsString.replace("\\", "\\\\");
        jsString = jsString.replace("\"", "\\\"");
        jsString = jsString.replace("\b", "\\b");
        jsString = jsString.replace("\f", "\\f");
        jsString = jsString.replace("\n", "\\n");
        jsString = jsString.replace("\r", "\\r");
        jsString = jsString.replace("\t", "\\t");
        return jsString;
    }
}
