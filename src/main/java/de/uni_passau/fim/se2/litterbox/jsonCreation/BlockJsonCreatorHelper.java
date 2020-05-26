package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.ast.parser.KeyParser.*;
import static de.uni_passau.fim.se2.litterbox.ast.parser.KeyParser.ANYKEY;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.JSONStringCreator.*;

public abstract class BlockJsonCreatorHelper {
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
            createFieldValue(jsonString, X_KEY, topNonDataBlockMetadata.getxPos()).append(",");
            createFieldValue(jsonString, Y_KEY, topNonDataBlockMetadata.getyPos());
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
        createFieldValue(jsonString,TAG_NAME_KEY,tagName).append(",");
        createField(jsonString, CHILDREN_KEY).append("[],");
        createFieldValue(jsonString,HAS_NEXT_KEY,hasNext);
        jsonString.append("}");
        return jsonString.toString();
    }

    public static String getKeyValue(int numberValue){
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
                key = "" + (char) numberValue;
        }
        return key;
    }
}
