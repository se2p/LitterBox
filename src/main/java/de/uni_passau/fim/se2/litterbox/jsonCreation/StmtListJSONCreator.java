package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;

import java.util.ArrayList;
import java.util.List;

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
}
