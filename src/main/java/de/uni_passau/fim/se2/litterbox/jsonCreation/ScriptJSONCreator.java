package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;

import static de.uni_passau.fim.se2.litterbox.jsonCreation.StmtListJSONCreator.createBlockUpToParent;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.StmtListJSONCreator.createFixedBlock;

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
            String nextId = null;

            if (stmtList.getStmts().size() > 0) {
                IdVisitor vis = new IdVisitor();
                nextId = vis.getBlockId(stmtList.getStmts().get(0));
            }

            if (event instanceof AttributeAboveValue) {
                AttributeAboveValue attributeAboveValue = (AttributeAboveValue) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) attributeAboveValue.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
                createBlockUpToParent(jsonString, meta, nextId, null);
            } else if (event instanceof BackdropSwitchTo) {
                BackdropSwitchTo backdropSwitchTo = (BackdropSwitchTo) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) backdropSwitchTo.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
                createBlockUpToParent(jsonString, meta, nextId, null);
            } else if (event instanceof GreenFlag) {
                GreenFlag greenFlag = (GreenFlag) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) greenFlag.getMetadata();
                blockId = meta.getBlockId();
                createFixedBlock(jsonString, meta, nextId, null);
            } else if (event instanceof KeyPressed) {
                KeyPressed keyPressed = (KeyPressed) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) keyPressed.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
                createBlockUpToParent(jsonString, meta, nextId, null);
            } else if (event instanceof ReceptionOfMessage) {
                ReceptionOfMessage receptionOfMessage = (ReceptionOfMessage) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) receptionOfMessage.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
                createBlockUpToParent(jsonString, meta, nextId, null);
            } else if (event instanceof SpriteClicked) {
                SpriteClicked spriteClicked = (SpriteClicked) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) spriteClicked.getMetadata();
                blockId = meta.getBlockId();
                createFixedBlock(jsonString, meta, nextId, null);
            } else if (event instanceof StageClicked) {
                StageClicked stageClicked = (StageClicked) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) stageClicked.getMetadata();
                blockId = meta.getBlockId();
                createFixedBlock(jsonString, meta, nextId, null);
            } else if (event instanceof StartedAsClone) {
                StartedAsClone startedAsClone = (StartedAsClone) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) startedAsClone.getMetadata();
                blockId = meta.getBlockId();
                createFixedBlock(jsonString, meta, nextId, null);
            }
            if (script.getStmtList().getStmts().size() > 0) {
                assert blockId != null;
                stmtListJSONCreator = new StmtListJSONCreator(blockId, stmtList);
                jsonString.append(",");
                jsonString.append(stmtListJSONCreator.createStmtListJSONString());
            }
        }

        return jsonString.toString();
    }


}
