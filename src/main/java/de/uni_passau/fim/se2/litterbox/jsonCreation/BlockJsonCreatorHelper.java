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

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;

import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.ast.parser.KeyParser.*;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.JSONStringCreator.*;

public abstract class BlockJsonCreatorHelper {
    public static final String DEFAULT_VALUE = "[10,\"\"]";

    public static StringBuilder createBlockUpToParent(StringBuilder jsonString, NonDataBlockMetadata meta,
                                                      String nextId, String parentId) {

        createField(jsonString, meta.getBlockId()).append("{");
        createFieldValue(jsonString, OPCODE_KEY, meta.getOpcode()).append(",");
        if (nextId == null) {
            createFieldValueNull(jsonString, NEXT_KEY).append(",");
        } else {
            createFieldValue(jsonString, NEXT_KEY, nextId).append(",");
        }
        if (parentId == null) {
            createFieldValueNull(jsonString, PARENT_KEY);
        } else {
            createFieldValue(jsonString, PARENT_KEY, parentId);
        }
        return jsonString;
    }

    public static StringBuilder createBlockInputFieldForFixed(StringBuilder jsonString) {
        createField(jsonString, INPUTS_KEY).append("{},");
        createField(jsonString, FIELDS_KEY).append("{}");
        return jsonString;
    }

    public static StringBuilder createBlockAfterFields(StringBuilder jsonString, NonDataBlockMetadata meta) {
        createFieldValue(jsonString, SHADOW_KEY, meta.isShadow()).append(",");
        createFieldValue(jsonString, TOPLEVEL_KEY, meta.isTopLevel());
        if (meta.getCommentId() != null) {
            jsonString.append(",");
            createFieldValue(jsonString, COMMENT_KEY, meta.getCommentId());
        }
        if (meta instanceof TopNonDataBlockMetadata) {
            TopNonDataBlockMetadata topNonDataBlockMetadata = (TopNonDataBlockMetadata) meta;
            jsonString.append(",");
            createFieldValue(jsonString, X_KEY, topNonDataBlockMetadata.getXPos()).append(",");
            createFieldValue(jsonString, Y_KEY, topNonDataBlockMetadata.getYPos());
        }
        return jsonString;
    }

    public static String createFixedBlock(NonDataBlockMetadata meta,
                                          String nextId, String parentId) {
        StringBuilder jsonString = new StringBuilder();
        createBlockUpToParent(jsonString, meta, nextId, parentId).append(",");
        createBlockInputFieldForFixed(jsonString).append(",");
        createBlockAfterFields(jsonString, meta).append("}");
        return jsonString.toString();
    }

    private static StringBuilder createBlockString(NonDataBlockMetadata metadata, String nextId, String parentId,
                                                   String inputsString,
                                                   String fieldsString) {
        StringBuilder jsonString = new StringBuilder();
        createBlockUpToParent(jsonString, metadata, nextId, parentId).append(",");
        createField(jsonString, INPUTS_KEY).append(inputsString).append(",");
        createField(jsonString, FIELDS_KEY).append(fieldsString).append(",");
        createBlockAfterFields(jsonString, metadata);
        return jsonString;
    }

    public static String createBlockWithMutationString(NonDataBlockMetadata metadata, String nextId, String parentId,
                                                       String inputsString,
                                                       String fieldsString, String mutation) {
        StringBuilder jsonString = createBlockString(metadata, nextId, parentId, inputsString, fieldsString);
        jsonString.append(",");
        createField(jsonString, MUTATION_KEY).append(mutation);
        jsonString.append("}");
        return jsonString.toString();
    }

    public static String createBlockWithoutMutationString(NonDataBlockMetadata metadata, String nextId, String parentId,
                                                          String inputsString,
                                                          String fieldsString) {
        StringBuilder jsonString = createBlockString(metadata, nextId, parentId, inputsString, fieldsString);
        jsonString.append("}");
        return jsonString.toString();
    }

    public static String createFields(String fieldName, String fieldValue, String fieldReference) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{");
        createField(jsonString, fieldName).append("[");
        jsonString.append("\"").append(fieldValue).append("\",");
        if (fieldReference == null) {
            jsonString.append(fieldReference);
        } else {
            jsonString.append("\"").append(fieldReference).append("\"");
        }
        jsonString.append("]}");
        return jsonString.toString();
    }

    public static String createStopMetadata(String tagName, boolean hasNext) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{");
        createFieldValue(jsonString, TAG_NAME_KEY, tagName).append(",");
        createField(jsonString, CHILDREN_KEY).append("[],");
        createFieldValue(jsonString, HAS_NEXT_KEY, hasNext);
        jsonString.append("}");
        return jsonString.toString();
    }

    public static String createPrototypeMetadata(String tagName, String proccode, List<String> argumentId,
                                                 List<ParameterInfo> parameterInfos,
                                                 boolean warp) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{");
        createFieldValue(jsonString, TAG_NAME_KEY, tagName).append(",");
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

    public static String createCallMetadata(String tagName, String proccode, List<String> argumentId,
                                            boolean warp) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{");
        createFieldValue(jsonString, TAG_NAME_KEY, tagName).append(",");
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
        if (parameterInfos.size() > 0) {
            jsonString.append(parameterInfos.get(parameterInfos.size() - 1).getDefaultValue());
        }
        jsonString.append("]\"");
        return jsonString;
    }

    private static StringBuilder createArgumentNames(StringBuilder jsonString, List<ParameterInfo> parameterInfos) {
        jsonString.append("\"[");
        for (int i = 0; i < parameterInfos.size() - 1; i++) {
            jsonString.append("\\\"").append(parameterInfos.get(i).getName()).append("\\\"").append(",");
        }
        if (parameterInfos.size() > 0) {
            jsonString.append("\\\"").append(parameterInfos.get(parameterInfos.size() - 1).getName()).append("\\\"");
        }
        jsonString.append("]\"");
        return jsonString;
    }

    private static StringBuilder createArgumentIds(StringBuilder jsonString, List<String> argumentId) {
        jsonString.append("\"[");
        for (int i = 0; i < argumentId.size() - 1; i++) {
            jsonString.append("\\\"").append(argumentId.get(i)).append("\\\"").append(",");
        }
        if (argumentId.size() > 0) {
            jsonString.append("\\\"").append(argumentId.get(argumentId.size() - 1)).append("\\\"");
        }
        jsonString.append("]\"");
        return jsonString;
    }

    public static String getKeyValue(int numberValue) {
        String key;
        switch (numberValue) {
            case UPARROW:
                key = "up arrow";
                break;
            case DOWNARROW:
                key = "down arrow";
                break;
            case LEFTARROW:
                key = "left arrow";
                break;
            case RIGHTARROW:
                key = "right arrow";
                break;
            case SPACE:
                key = "space";
                break;
            case ANYKEY:
                key = "any";
                break;
            default:
                key = String.valueOf((char) numberValue);
        }
        return key;
    }

    public static String createReferenceInput(String inputName, int shadowIndicator, String reference,
                                              boolean withDefault) {
        StringBuilder jsonString = new StringBuilder();
        createField(jsonString, inputName).append("[").append(shadowIndicator).append(",");
        if (reference == null) {
            jsonString.append(reference);
        } else {
            jsonString.append("\"").append(reference).append("\"");
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
        if (inputsList.size() > 0) {
            jsonString.append(inputsList.get(inputsList.size() - 1));
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

    public static String createTypeInput(int shadowIndicator, int typeNumber,
                                         String value) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("[")
                .append(shadowIndicator)
                .append(",").append("[")
                .append(typeNumber)
                .append(",\"")
                .append(value)
                .append("\"]]");
        return jsonString.toString();
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
        jsonString.append("[").append(shadowIndicator)
                .append(",")
                .append("[")
                .append(typeNumber)
                .append(",\"")
                .append(value)
                .append("\",\"")
                .append(reference)
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
}
