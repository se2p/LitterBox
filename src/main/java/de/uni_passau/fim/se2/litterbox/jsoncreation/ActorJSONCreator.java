/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.CommentMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.StageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetAttributeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.MessageInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class ActorJSONCreator {

    public static String createActorJSONString(ActorDefinition actor, SymbolTable symbol,
                                               ProcedureDefinitionNameMapping procDefNameMapping) {
        StringBuilder jsonString = new StringBuilder();
        ActorMetadata meta = actor.getActorMetadata();
        boolean isStage = actor.isStage();
        jsonString.append("{");
        JSONStringCreator.createFieldValue(jsonString, IS_STAGE_KEY, isStage).append(",");
        JSONStringCreator.createFieldValue(jsonString, NAME_KEY, actor.getIdent().getName()).append(",");

        //variables
        JSONStringCreator.createField(jsonString, VARIABLES_KEY).append("{");
        List<SetStmt> setStmts = actor.getSetStmtList().getStmts();
        List<SetVariableTo> variableSetStmts = new ArrayList<>();
        List<SetVariableTo> listSetStmts = new ArrayList<>();
        List<SetAttributeTo> attributeToStmts = new ArrayList<>();
        for (SetStmt setStmt : setStmts) {
            if (setStmt instanceof SetVariableTo setVariableTo) {
                Qualified qualified = (Qualified) setVariableTo.getIdentifier();
                if (qualified.getSecond() instanceof Variable) {
                    variableSetStmts.add(setVariableTo);
                } else if (qualified.getSecond() instanceof ScratchList) {
                    listSetStmts.add(setVariableTo);
                }
            } else if (setStmt instanceof SetAttributeTo setAttributeTo) {
                attributeToStmts.add(setAttributeTo);
            }
        }

        for (int i = 0; i < variableSetStmts.size() - 1; i++) {
            addVariable(jsonString, variableSetStmts.get(i), symbol).append(",");
        }
        if (!variableSetStmts.isEmpty()) {
            addVariable(jsonString, variableSetStmts.get(variableSetStmts.size() - 1), symbol);
        }
        jsonString.append("},");

        //lists
        JSONStringCreator.createField(jsonString, LISTS_KEY).append("{");
        for (int i = 0; i < listSetStmts.size() - 1; i++) {
            addList(jsonString, listSetStmts.get(i), symbol).append(",");
        }
        if (!listSetStmts.isEmpty()) {
            addList(jsonString, listSetStmts.get(listSetStmts.size() - 1), symbol);
        }
        jsonString.append("},");

        //broadcasts
        JSONStringCreator.createField(jsonString, BROADCASTS_KEY).append("{");
        List<MessageInfo> messages = new ArrayList<>(symbol.getMessages().values());
        List<MessageInfo> currentMessages = new ArrayList<>();
        for (MessageInfo message : messages) {
            if (message.getActor().equals(actor.getIdent().getName())) {
                currentMessages.add(message);
            }
        }
        for (int i = 0; i < currentMessages.size() - 1; i++) {
            MessageInfo info = currentMessages.get(i);
            JSONStringCreator.createFieldValue(
                    jsonString,
                    info.getIdentifier(),
                    ((StringLiteral) info.getMessage().getMessage()).getText()
            ).append(",");
        }
        if (!currentMessages.isEmpty()) {
            MessageInfo info = currentMessages.get(currentMessages.size() - 1);
            JSONStringCreator.createFieldValue(
                    jsonString,
                    info.getIdentifier(),
                    ((StringLiteral) info.getMessage().getMessage()).getText()
            );
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
        if (!procedures.isEmpty()) {
            jsonString.append(ProcedureJSONCreator.createProcedureJSONString(procedures.get(procedures.size() - 1),
                    actor.getIdent().getName(), symbol, procDefNameMapping));
            if (!scripts.isEmpty()) {
                jsonString.append(",");
            }
        }
        for (int i = 0; i < scripts.size() - 1; i++) {
            jsonString.append(ScriptJSONCreator.createScriptJSONString(scripts.get(i), symbol)).append(",");
        }
        if (!scripts.isEmpty()) {
            jsonString.append(ScriptJSONCreator.createScriptJSONString(scripts.get(scripts.size() - 1), symbol));
        }
        jsonString.append("},");

        //comments
        JSONStringCreator.createField(jsonString, COMMENTS_KEY).append("{");
        List<CommentMetadata> comments = meta.getCommentsMetadata().getList();
        for (int i = 0; i < comments.size() - 1; i++) {
            addComment(jsonString, comments.get(i)).append(",");
        }
        if (!comments.isEmpty()) {
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
        if (!images.isEmpty()) {
            addImage(jsonString, images.get(images.size() - 1));
        }
        jsonString.append("],");

        //sounds
        JSONStringCreator.createField(jsonString, SOUNDS_KEY).append("[");
        List<SoundMetadata> sounds = meta.getSounds().getList();
        for (int i = 0; i < sounds.size() - 1; i++) {
            addSound(jsonString, sounds.get(i)).append(",");
        }
        if (!sounds.isEmpty()) {
            addSound(jsonString, sounds.get(sounds.size() - 1));
        }
        jsonString.append("],");

        //attributes
        for (int i = 0; i < attributeToStmts.size() - 1; i++) {
            SetAttributeTo attributeTo = attributeToStmts.get(i);
            String attribute = ((StringLiteral) attributeTo.getStringExpr()).getText();
            if (attributeTo.getExpr() instanceof StringLiteral stringLiteral) {
                JSONStringCreator.createFieldValue(jsonString, attribute, stringLiteral.getText()).append(",");
            } else if (attributeTo.getExpr() instanceof NumberLiteral numberLiteral) {
                JSONStringCreator.createFieldValue(jsonString, attribute, numberLiteral.getValue()).append(",");
            }
        }
        if (!attributeToStmts.isEmpty()) {
            SetAttributeTo attributeTo = attributeToStmts.get(attributeToStmts.size() - 1);
            String attribute = ((StringLiteral) attributeTo.getStringExpr()).getText();
            if (attributeTo.getExpr() instanceof StringLiteral stringLiteral) {
                JSONStringCreator.createFieldValue(jsonString, attribute, stringLiteral.getText());
            } else if (attributeTo.getExpr() instanceof NumberLiteral numberLiteral) {
                JSONStringCreator.createFieldValue(jsonString, attribute, numberLiteral.getValue());
            }
        }

        //attributes that are only in stage
        if (isStage) {
            jsonString.append(",");
            StageMetadata stageMetadata = (StageMetadata) meta;
            if (stageMetadata.getTextToSpeechLanguage() == null) {
                JSONStringCreator.createFieldValueNull(jsonString, TEXT_TO_SPEECH_KEY);
            } else {
                JSONStringCreator.createFieldValue(jsonString, TEXT_TO_SPEECH_KEY,
                        stageMetadata.getTextToSpeechLanguage());
            }
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

    private static StringBuilder addVariable(StringBuilder jsonString, SetVariableTo node, SymbolTable symbol) {
        Qualified qualified = (Qualified) node.getIdentifier();
        Variable variable = (Variable) qualified.getSecond();
        String variableName = variable.getName().getName();

        String id = symbol.getVariableIdentifierFromActorAndName(qualified.getFirst().getName(), variableName);
        JSONStringCreator.createField(jsonString, id).append("[");
        Expression expr = node.getExpr();
        if (expr instanceof StringLiteral stringLiteral) {
            jsonString.append("\"").append(variableName).append("\", \"").append(stringLiteral.getText()).append("\"]");
        } else if (expr instanceof NumberLiteral numberLiteral) {
            jsonString.append("\"").append(variableName).append("\", ").append(numberLiteral.getValue()).append("]");
        }
        return jsonString;
    }

    private static StringBuilder addList(StringBuilder jsonString, SetVariableTo node, SymbolTable symbol) {
        Qualified qualified = (Qualified) node.getIdentifier();
        ScratchList list = (ScratchList) qualified.getSecond();
        String id = symbol.getListIdentifierFromActorAndName(qualified.getFirst().getName(), list.getName().getName());
        JSONStringCreator.createField(jsonString, id).append("[");
        jsonString.append("\"").append(list.getName().getName()).append("\", [");
        Expression expression = node.getExpr();
        Preconditions.checkArgument(expression instanceof ExpressionList);
        List<Expression> expressionList = ((ExpressionList) expression).getExpressions();
        for (int i = 0; i < expressionList.size() - 1; i++) {
            Expression expr = expressionList.get(i);
            if (expr instanceof StringLiteral stringLiteral) {
                jsonString.append("\"").append(stringLiteral.getText()).append("\",");
            } else if (expr instanceof NumberLiteral numberLiteral) {
                jsonString.append(numberLiteral.getValue()).append(",");
            }
        }
        if (!expressionList.isEmpty()) {
            Expression expr = expressionList.get(expressionList.size() - 1);
            if (expr instanceof StringLiteral stringLiteral) {
                jsonString.append("\"").append(stringLiteral.getText()).append("\"");
            } else if (expr instanceof NumberLiteral numberLiteral) {
                jsonString.append(numberLiteral.getValue());
            }
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
