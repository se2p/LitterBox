package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.JSONStringCreator.*;

public class StmtListJSONCreator {
    private boolean startsWithTopBlock = false;
    private String currentBlockId = null;
    private List<String> finishedJSONStrings;

    public StmtListJSONCreator(String parentID, StmtList stmtList) {
        currentBlockId = parentID;
        finishedJSONStrings = new ArrayList<>();
    }

    public StmtListJSONCreator(StmtList stmtList) {
        startsWithTopBlock = true;
        finishedJSONStrings = new ArrayList<>();
    }

    public String getStmtListJSONString() {
        return "";
    }

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
            createFieldValueNull(jsonString, PARENT_KEY).append(",");
        } else {
            createFieldValue(jsonString, PARENT_KEY, parentId).append(",");
        }
        return jsonString;
    }

    public static StringBuilder createBlockInputFieldForFixed(StringBuilder jsonString) {
        createField(jsonString, INPUTS_KEY).append("{},");
        createField(jsonString, FIELDS_KEY).append("{},");
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
        jsonString.append("}");
        return jsonString;
    }

    public static StringBuilder createFixedBlock(StringBuilder jsonString, NonDataBlockMetadata meta,
                                                 String nextId, String parentId) {
        createBlockUpToParent(jsonString, meta, nextId, parentId);
        createBlockInputFieldForFixed(jsonString);
        createBlockAfterFields(jsonString, meta);
        return jsonString;
    }
}
