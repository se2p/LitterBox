package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;

public class ScriptJSONCreator {
    public static String createScriptJSONString(Script script) {
        StringBuilder jsonString = new StringBuilder();
        Event event = script.getEvent();
        StmtListJSONCreator stmtListJSONCreator = null;
        if (event instanceof Never) {
            stmtListJSONCreator = new StmtListJSONCreator(script.getStmtList());
            jsonString.append(stmtListJSONCreator.getStmtListJSONString());
        } else {
            String blockId = null;
            if (event instanceof AttributeAboveValue) {
                AttributeAboveValue attributeAboveValue = (AttributeAboveValue) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) attributeAboveValue.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
            } else if (event instanceof BackdropSwitchTo) {
                BackdropSwitchTo backdropSwitchTo = (BackdropSwitchTo) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) backdropSwitchTo.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
            } else if (event instanceof GreenFlag) {
                GreenFlag greenFlag = (GreenFlag) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) greenFlag.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
            } else if (event instanceof KeyPressed) {
                KeyPressed keyPressed = (KeyPressed) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) keyPressed.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
            } else if (event instanceof ReceptionOfMessage) {
                ReceptionOfMessage receptionOfMessage = (ReceptionOfMessage) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) receptionOfMessage.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
            } else if (event instanceof SpriteClicked) {
                SpriteClicked spriteClicked = (SpriteClicked) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) spriteClicked.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
            } else if (event instanceof StageClicked) {
                StageClicked stageClicked = (StageClicked) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) stageClicked.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
            } else if (event instanceof StartedAsClone) {
                StartedAsClone startedAsClone = (StartedAsClone) event;
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) startedAsClone.getMetadata();
                //todo event handling
                blockId = meta.getBlockId();
            }
            if (script.getStmtList().getStmts().size() > 0) {
                assert blockId != null;
                stmtListJSONCreator = new StmtListJSONCreator(blockId, script.getStmtList());
                jsonString.append(stmtListJSONCreator.getStmtListJSONString());
            }
        }

        return jsonString.toString();
    }
}
