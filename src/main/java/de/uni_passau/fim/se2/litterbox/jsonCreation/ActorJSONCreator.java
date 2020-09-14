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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.BroadcastMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.CommentMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ListMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.VariableMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.SpriteMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.StageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;

import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class ActorJSONCreator {

    public static String createActorJSONString(ActorDefinition actor, SymbolTable symbol,
                                               ProcedureDefinitionNameMapping procDefNameMapping) {
        StringBuilder jsonString = new StringBuilder();
        ActorMetadata meta = actor.getActorMetadata();
        boolean isStage = false;
        if (actor.getActorType().equals(ActorType.STAGE)) {
            isStage = true;
        }
        jsonString.append("{");
        JSONStringCreator.createFieldValue(jsonString, IS_STAGE_KEY, isStage).append(",");
        JSONStringCreator.createFieldValue(jsonString, NAME_KEY, actor.getIdent().getName()).append(",");

        //variables
        JSONStringCreator.createField(jsonString, VARIABLES_KEY).append("{");
        List<VariableMetadata> variables = meta.getVariables().getList();
        for (int i = 0; i < variables.size() - 1; i++) {
            addVariable(jsonString, variables.get(i)).append(",");
        }
        if (variables.size() > 0) {
            addVariable(jsonString, variables.get(variables.size() - 1));
        }
        jsonString.append("},");

        //lists
        JSONStringCreator.createField(jsonString, LISTS_KEY).append("{");
        List<ListMetadata> lists = meta.getLists().getList();
        for (int i = 0; i < lists.size() - 1; i++) {
            addList(jsonString, lists.get(i)).append(",");
        }
        if (lists.size() > 0) {
            addList(jsonString, lists.get(lists.size() - 1));
        }
        jsonString.append("},");

        //broadcasts
        JSONStringCreator.createField(jsonString, BROADCASTS_KEY).append("{");
        List<BroadcastMetadata> broadcasts = meta.getBroadcasts().getList();
        for (int i = 0; i < broadcasts.size() - 1; i++) {
            BroadcastMetadata current = broadcasts.get(i);
            JSONStringCreator.createFieldValue(
                    jsonString,
                    current.getBroadcastID(),
                    current.getBroadcastName()
            ).append(",");
        }
        if (broadcasts.size() > 0) {
            BroadcastMetadata current = broadcasts.get(broadcasts.size() - 1);
            JSONStringCreator.createFieldValue(jsonString, current.getBroadcastID(), current.getBroadcastName());
        }
        jsonString.append("},");

        //scripts and procedures
        JSONStringCreator.createField(jsonString, BLOCKS_KEY).append("{");
        List<ProcedureDefinition> procedures = actor.getProcedureDefinitionList().getList();
        List<Script> scripts = actor.getScripts().getScriptList();
        for (int i = 0; i < procedures.size() - 1; i++) {
            jsonString.append(ProcedureJSONCreator.createProcedureJSONString(procedures.get(i),
                    actor.getIdent().getName(), symbol, procDefNameMapping)).append(",");
        }
        if (procedures.size() > 0) {
            jsonString.append(ProcedureJSONCreator.createProcedureJSONString(procedures.get(procedures.size() - 1),
                    actor.getIdent().getName(), symbol, procDefNameMapping));
            if (scripts.size() > 0) {
                jsonString.append(",");
            }
        }
        for (int i = 0; i < scripts.size() - 1; i++) {
            jsonString.append(ScriptJSONCreator.createScriptJSONString(scripts.get(i), symbol)).append(",");
        }
        if (scripts.size() > 0) {
            jsonString.append(ScriptJSONCreator.createScriptJSONString(scripts.get(scripts.size() - 1), symbol));
        }
        jsonString.append("},");

        //comments
        JSONStringCreator.createField(jsonString, COMMENTS_KEY).append("{");
        List<CommentMetadata> comments = meta.getCommentsMetadata().getList();
        for (int i = 0; i < comments.size() - 1; i++) {
            addComment(jsonString, comments.get(i)).append(",");
        }
        if (comments.size() > 0) {
            addComment(jsonString, comments.get(comments.size() - 1));
        }
        jsonString.append("},");

        JSONStringCreator.createFieldValue(jsonString, CURRENT_COSTUME_KEY, meta.getCurrentCostume()).append(",");

        //costumes
        JSONStringCreator.createField(jsonString, COSTUMES_KEY).append("[");
        List<ImageMetadata> images = meta.getCostumes().getList();
        for (int i = 0; i < images.size() - 1; i++) {
            addImage(jsonString, images.get(i)).append(",");
        }
        if (images.size() > 0) {
            addImage(jsonString, images.get(images.size() - 1));
        }
        jsonString.append("],");

        //sounds
        JSONStringCreator.createField(jsonString, SOUNDS_KEY).append("[");
        List<SoundMetadata> sounds = meta.getSounds().getList();
        for (int i = 0; i < sounds.size() - 1; i++) {
            addSound(jsonString, sounds.get(i)).append(",");
        }
        if (sounds.size() > 0) {
            addSound(jsonString, sounds.get(sounds.size() - 1));
        }
        jsonString.append("],");

        JSONStringCreator.createFieldValue(jsonString, VOLUME_KEY, meta.getVolume()).append(",");
        JSONStringCreator.createFieldValue(jsonString, LAYERORDER_KEY, meta.getLayerOrder()).append(",");

        //attributes that are only in stage or sprite
        if (isStage) {
            StageMetadata stageMetadata = (StageMetadata) meta;
            JSONStringCreator.createFieldValue(jsonString, TEMPO_KEY, stageMetadata.getTempo()).append(",");
            JSONStringCreator.createFieldValue(jsonString, VIDTRANSPARENCY_KEY, stageMetadata.getVideoTransparency())
                    .append(",");
            JSONStringCreator.createFieldValue(jsonString, VIDSTATE_KEY, stageMetadata.getVideoState()).append(",");
            if (stageMetadata.getTextToSpeechLanguage() == null) {
                JSONStringCreator.createFieldValueNull(jsonString, TEXT_TO_SPEECH_KEY);
            } else {
                JSONStringCreator.createFieldValue(jsonString, TEXT_TO_SPEECH_KEY,
                        stageMetadata.getTextToSpeechLanguage());
            }
        } else {
            SpriteMetadata spriteMetadata = (SpriteMetadata) meta;
            JSONStringCreator.createFieldValue(jsonString, VISIBLE_KEY, spriteMetadata.isVisible()).append(",");
            JSONStringCreator.createFieldValue(jsonString, X_KEY, spriteMetadata.getX()).append(",");
            JSONStringCreator.createFieldValue(jsonString, Y_KEY, spriteMetadata.getY()).append(",");
            JSONStringCreator.createFieldValue(jsonString, SIZE_KEY, spriteMetadata.getSize()).append(",");
            JSONStringCreator.createFieldValue(jsonString, DIRECTION_KEY, spriteMetadata.getDirection()).append(",");
            JSONStringCreator.createFieldValue(jsonString, DRAG_KEY, spriteMetadata.isDraggable()).append(",");
            JSONStringCreator.createFieldValue(jsonString, ROTATIONSTYLE_KEY, spriteMetadata.getRotationStyle());
        }
        jsonString.append("}");
        return jsonString.toString();
    }

    private static StringBuilder addSound(StringBuilder jsonString, SoundMetadata soundMetadata) {
        jsonString.append("{");
        JSONStringCreator.createFieldValue(jsonString, ASSET_ID_KEY, soundMetadata.getAssetId()).append(",");
        JSONStringCreator.createFieldValue(jsonString, NAME_KEY, soundMetadata.getName()).append(",");
        JSONStringCreator.createFieldValue(jsonString, DATA_FORMAT_KEY, soundMetadata.getDataFormat()).append(",");
        JSONStringCreator.createFieldValue(jsonString, RATE_KEY, soundMetadata.getRate()).append(",");
        JSONStringCreator.createFieldValue(jsonString, SAMPLE_COUNT_KEY, soundMetadata.getSampleCount()).append(",");
        JSONStringCreator.createFieldValue(jsonString, MD5EXT_KEY, soundMetadata.getMd5ext());
        jsonString.append("}");
        return jsonString;
    }

    private static StringBuilder addImage(StringBuilder jsonString, ImageMetadata imageMetadata) {
        jsonString.append("{");
        JSONStringCreator.createFieldValue(jsonString, ASSET_ID_KEY, imageMetadata.getAssetId()).append(",");
        JSONStringCreator.createFieldValue(jsonString, NAME_KEY, imageMetadata.getName()).append(",");
        if (imageMetadata.getBitmapResolution() != null) {
            JSONStringCreator.createFieldValue(jsonString, BITMAP_KEY, imageMetadata.getBitmapResolution()).append(",");
        }
        JSONStringCreator.createFieldValue(jsonString, MD5EXT_KEY, imageMetadata.getMd5ext()).append(",");
        JSONStringCreator.createFieldValue(jsonString, DATA_FORMAT_KEY, imageMetadata.getDataFormat()).append(",");
        JSONStringCreator.createFieldValue(jsonString, ROTATIONX_KEY, imageMetadata.getRotationCenterX()).append(",");
        JSONStringCreator.createFieldValue(jsonString, ROTATIONY_KEY, imageMetadata.getRotationCenterY());
        jsonString.append("}");
        return jsonString;
    }

    private static StringBuilder addVariable(StringBuilder jsonString, VariableMetadata meta) {
        JSONStringCreator.createField(jsonString, meta.getVariableId()).append("[");
        jsonString.append("\"").append(meta.getVariableName()).append("\", \"").append(meta.getValue()).append("\"]");
        return jsonString;
    }

    private static StringBuilder addList(StringBuilder jsonString, ListMetadata meta) {
        JSONStringCreator.createField(jsonString, meta.getListId()).append("[");
        jsonString.append("\"").append(meta.getListName()).append("\", [");
        List<String> values = meta.getValues();
        for (int i = 0; i < values.size() - 1; i++) {
            jsonString.append("\"").append(values.get(i)).append("\",");
        }
        if (values.size() > 0) {
            jsonString.append("\"").append(values.get(values.size() - 1)).append("\"");
        }
        jsonString.append("]]");
        return jsonString;
    }

    private static StringBuilder addComment(StringBuilder jsonString, CommentMetadata meta) {
        JSONStringCreator.createField(jsonString, meta.getCommentId()).append("{");
        if (meta.getBlockId() == null) {
            JSONStringCreator.createFieldValueNull(jsonString, BLOCK_ID_KEY).append(",");
        } else {
            JSONStringCreator.createFieldValue(jsonString, BLOCK_ID_KEY, meta.getBlockId()).append(",");
        }
        JSONStringCreator.createFieldValue(jsonString, X_KEY, meta.getX()).append(",");
        JSONStringCreator.createFieldValue(jsonString, Y_KEY, meta.getY()).append(",");
        JSONStringCreator.createFieldValue(jsonString, WIDTH_KEY, meta.getWidth()).append(",");
        JSONStringCreator.createFieldValue(jsonString, HEIGHT_KEY, meta.getHeight()).append(",");
        JSONStringCreator.createFieldValue(jsonString, MINIMIZED_KEY, meta.isMinimized()).append(",");
        JSONStringCreator.createFieldValue(jsonString, TEXT_KEY, meta.getText());
        jsonString.append("}");
        return jsonString;
    }
}
