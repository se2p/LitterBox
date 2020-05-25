package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class ScriptJSONCreator {
    public static String createScriptJSONString(Script script) {
        StringBuilder jsonString = new StringBuilder();
        Event event = script.getEvent();
        StmtListJSONCreator stmtListJSONCreator = null;
        StmtList stmtList = script.getStmtList();
        if (event instanceof Never) {
            stmtListJSONCreator = new StmtListJSONCreator(script.getStmtList());
            jsonString.append(stmtList);
        } else {
            StringBuilder endOfEventBlock = new StringBuilder();
            String blockId = null;
            if (event instanceof AttributeAboveValue) {
                AttributeAboveValue attributeAboveValue = (AttributeAboveValue) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) attributeAboveValue.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
                createBlockUpToParent(jsonString,meta,stmtList);
            } else if (event instanceof BackdropSwitchTo) {
                BackdropSwitchTo backdropSwitchTo = (BackdropSwitchTo) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) backdropSwitchTo.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
                createBlockUpToParent(jsonString,meta,stmtList);
            } else if (event instanceof GreenFlag) {
                GreenFlag greenFlag = (GreenFlag) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) greenFlag.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
                createBlockUpToParent(jsonString,meta,stmtList);
            } else if (event instanceof KeyPressed) {
                KeyPressed keyPressed = (KeyPressed) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) keyPressed.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
                createBlockUpToParent(jsonString,meta,stmtList);
            } else if (event instanceof ReceptionOfMessage) {
                ReceptionOfMessage receptionOfMessage = (ReceptionOfMessage) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) receptionOfMessage.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
                createBlockUpToParent(jsonString,meta,stmtList);
            } else if (event instanceof SpriteClicked) {
                SpriteClicked spriteClicked = (SpriteClicked) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) spriteClicked.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
                createBlockUpToParent(jsonString,meta,stmtList);
            } else if (event instanceof StageClicked) {
                StageClicked stageClicked = (StageClicked) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) stageClicked.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
                createBlockUpToParent(jsonString,meta,stmtList);
            } else if (event instanceof StartedAsClone) {
                StartedAsClone startedAsClone = (StartedAsClone) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) startedAsClone.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
                createBlockUpToParent(jsonString,meta,stmtList);
            }
            if (script.getStmtList().getStmts().size() > 0) {
                assert blockId != null;
                stmtListJSONCreator = new StmtListJSONCreator(blockId,stmtList);
                jsonString.append(stmtListJSONCreator.getStmtListJSONString());
            }
        }

        return jsonString.toString();
    }

    private static void createBlockUpToParent(StringBuilder jsonString, TopNonDataBlockMetadata meta,
                                              StmtList stmtList) {

        JSONStringCreator.createField(jsonString, meta.getBlockId()).append("{");
        JSONStringCreator.createFieldValue(jsonString, OPCODE_KEY, meta.getOpcode()).append(",");
        List<Stmt> stmts = stmtList.getStmts();
        if (stmts.size() == 0) {
            JSONStringCreator.createFieldValueNull(jsonString, NEXT_KEY).append(",");
        } else {
            IdVisitor vis = new IdVisitor();
            String nextId = vis.getBlockId(stmts.get(0));
            JSONStringCreator.createFieldValue(jsonString, NEXT_KEY, nextId).append(",");
        }
        JSONStringCreator.createFieldValueNull(jsonString, PARENT_KEY).append(",");
    }
}
