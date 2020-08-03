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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetAttributeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationAttributeAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationBroadcastStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.ListType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class DeclarationStmtParser {

    public static List<DeclarationStmt> parseVariables(JsonNode variableNode, String actorName, boolean isStage) {
        Preconditions.checkNotNull(variableNode);
        List<DeclarationStmt> parsedVariables = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iter = variableNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> currentEntry = iter.next();
            Preconditions.checkArgument(currentEntry.getValue().isArray());
            ArrayNode arrNode = (ArrayNode) currentEntry.getValue();
            if (arrNode.get(DECLARATION_VARIABLE_VALUE_POS).isNumber()) {
                ProgramParser.symbolTable.addVariable(currentEntry.getKey(),
                        arrNode.get(DECLARATION_VARIABLE_NAME_POS).asText(),
                        new NumberType(), isStage, actorName);
                parsedVariables.add(new DeclarationIdentAsTypeStmt(
                        new Variable(new StrId(arrNode.get(DECLARATION_VARIABLE_NAME_POS).asText())),
                        new NumberType()));
            } else if (arrNode.get(DECLARATION_VARIABLE_VALUE_POS).isBoolean()) {
                ProgramParser.symbolTable.addVariable(currentEntry.getKey(),
                        arrNode.get(DECLARATION_VARIABLE_NAME_POS).asText(),
                        new BooleanType(), isStage, actorName);
                parsedVariables.add(new DeclarationIdentAsTypeStmt(
                        new Variable(new StrId(arrNode.get(DECLARATION_VARIABLE_NAME_POS).asText())),
                        new BooleanType()));
            } else {
                ProgramParser.symbolTable.addVariable(currentEntry.getKey(),
                        arrNode.get(DECLARATION_VARIABLE_NAME_POS).asText(),
                        new StringType(), isStage, actorName);
                parsedVariables.add(new DeclarationIdentAsTypeStmt(
                        new Variable(new StrId(arrNode.get(DECLARATION_VARIABLE_NAME_POS).asText())),
                        new StringType()));
            }
        }
        return parsedVariables;
    }

    public static List<SetStmt> parseVariableDeclarationSetStmts(JsonNode variableNode, String actorName) {
        Preconditions.checkNotNull(variableNode);
        List<SetStmt> parsedVariables = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iter = variableNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> currentEntry = iter.next();
            Preconditions.checkArgument(currentEntry.getValue().isArray());
            ArrayNode arrNode = (ArrayNode) currentEntry.getValue();

            if (arrNode.get(DECLARATION_VARIABLE_VALUE_POS).isNumber()) {
                parsedVariables.add(new SetVariableTo(new Qualified(new StrId(actorName),
                        new Variable(new StrId(arrNode.get(DECLARATION_VARIABLE_NAME_POS).asText()))),
                        new NumberLiteral((float) arrNode.get(DECLARATION_VARIABLE_VALUE_POS).asDouble()),
                        new NoBlockMetadata()));
            } else if (arrNode.get(DECLARATION_VARIABLE_VALUE_POS).isBoolean()) {
                parsedVariables.add(new SetVariableTo(new Qualified(new StrId(actorName),
                        new Variable(new StrId(arrNode.get(DECLARATION_VARIABLE_NAME_POS).asText()))),
                        new BoolLiteral(arrNode.get(DECLARATION_VARIABLE_VALUE_POS).asBoolean()),
                        new NoBlockMetadata()));
            } else {
                parsedVariables.add(new SetVariableTo(new Qualified(new StrId(actorName),
                        new Variable(new StrId(arrNode.get(DECLARATION_VARIABLE_NAME_POS).asText()))),
                        new StringLiteral(arrNode.get(DECLARATION_VARIABLE_VALUE_POS).asText()),
                        new NoBlockMetadata()));
            }
        }
        return parsedVariables;
    }

    public static List<DeclarationStmt> parseLists(JsonNode listsNode, String actorName, boolean isStage) {
        Preconditions.checkNotNull(listsNode);
        List<DeclarationStmt> parsedLists = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iter = listsNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> currentEntry = iter.next();
            Preconditions.checkArgument(currentEntry.getValue().isArray());
            ArrayNode arrNode = (ArrayNode) currentEntry.getValue();
            String listName = arrNode.get(DECLARATION_LIST_NAME_POS).asText();
            JsonNode listValues = arrNode.get(DECLARATION_LIST_VALUES_POS);
            Preconditions.checkArgument(listValues.isArray());
            ExpressionList expressionList = new ExpressionList(makeExpressionList((ArrayNode) listValues));
            ProgramParser.symbolTable.addExpressionListInfo(currentEntry.getKey(), listName, expressionList, isStage,
                    actorName);
            parsedLists.add(new DeclarationIdentAsTypeStmt(new ScratchList(new StrId(listName)), new ListType()));
        }
        return parsedLists;
    }

    private static List<Expression> makeExpressionList(ArrayNode valuesArray) {
        List<Expression> expressions = new ArrayList<>();
        for (int i = 0; i < valuesArray.size(); i++) {
            expressions.add(new StringLiteral(valuesArray.get(i).asText()));
        }
        return expressions;
    }

    public static List<SetStmt> parseListDeclarationSetStmts(JsonNode listNode, String actorName) {
        Preconditions.checkNotNull(listNode);
        List<SetStmt> parsedLists = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iter = listNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> currentEntry = iter.next();
            Preconditions.checkArgument(currentEntry.getValue().isArray());
            ArrayNode arrNode = (ArrayNode) currentEntry.getValue();
            String listName = arrNode.get(DECLARATION_LIST_NAME_POS).asText();
            JsonNode listValues = arrNode.get(DECLARATION_LIST_VALUES_POS);
            Preconditions.checkArgument(listValues.isArray());
            parsedLists.add(new SetVariableTo(new Qualified(new StrId(actorName),
                    new ScratchList(new StrId(listName))),
                    new ExpressionList(makeExpressionList((ArrayNode) listValues)),
                    new NoBlockMetadata()));
        }
        return parsedLists;
    }

    public static List<DeclarationStmt> parseBroadcasts(JsonNode broadcastsNode, String actorName,
                                                        boolean isStage) {
        Preconditions.checkNotNull(broadcastsNode);
        List<DeclarationStmt> parsedBroadcasts = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iter = broadcastsNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> current = iter.next();
            ProgramParser.symbolTable.addMessage(current.getValue().asText(),
                    new Message(new StringLiteral(current.getValue().asText())), isStage,
                    actorName, current.getKey());
            parsedBroadcasts.add(new DeclarationBroadcastStmt(new StrId(current.getValue().asText()),
                    new StringType()));
        }
        return parsedBroadcasts;
    }

    public static List<DeclarationStmt> parseAttributeDeclarations(JsonNode actorDefinitionNode) {
        StringExpr keyExpr;

        List<DeclarationStmt> list = new LinkedList<>();

        if (actorDefinitionNode.has(VOLUME_KEY)) {
            keyExpr = new StringLiteral(VOLUME_KEY);
            Preconditions.checkArgument(actorDefinitionNode.get(VOLUME_KEY).isNumber());
            list.add(new DeclarationAttributeAsTypeStmt(keyExpr, new NumberType()));
        }

        if (actorDefinitionNode.has(LAYERORDER_KEY)) {
            keyExpr = new StringLiteral(LAYERORDER_KEY);
            Preconditions.checkArgument(actorDefinitionNode.get(LAYERORDER_KEY).isNumber());
            list.add(new DeclarationAttributeAsTypeStmt(keyExpr, new NumberType()));
        }

        if (actorDefinitionNode.get(IS_STAGE_KEY).asBoolean()) {

            if (actorDefinitionNode.has(TEMPO_KEY)) {
                keyExpr = new StringLiteral(TEMPO_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(TEMPO_KEY).isNumber());
                list.add(new DeclarationAttributeAsTypeStmt(keyExpr, new NumberType()));
            }

            if (actorDefinitionNode.has(VIDTRANSPARENCY_KEY)) {
                keyExpr = new StringLiteral(VIDTRANSPARENCY_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(TEMPO_KEY).isNumber());
                list.add(new DeclarationAttributeAsTypeStmt(keyExpr, new NumberType()));
            }

            if (actorDefinitionNode.has(VIDSTATE_KEY)) {
                keyExpr = new StringLiteral(VIDSTATE_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(VIDSTATE_KEY).isTextual());
                list.add(new DeclarationAttributeAsTypeStmt(keyExpr, new StringType()));
            }
        } else {

            if (actorDefinitionNode.has(VISIBLE_KEY)) {
                keyExpr = new StringLiteral(VISIBLE_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(VISIBLE_KEY).isBoolean());
                list.add(new DeclarationAttributeAsTypeStmt(keyExpr, new BooleanType()));
            }

            if (actorDefinitionNode.has(X_KEY)) {
                keyExpr = new StringLiteral(X_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(X_KEY).isNumber());
                list.add(new DeclarationAttributeAsTypeStmt(keyExpr, new NumberType()));
            }

            if (actorDefinitionNode.has(Y_KEY)) {
                keyExpr = new StringLiteral(Y_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(Y_KEY).isNumber());
                list.add(new DeclarationAttributeAsTypeStmt(keyExpr, new NumberType()));
            }

            if (actorDefinitionNode.has(SIZE_KEY)) {
                keyExpr = new StringLiteral(SIZE_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(SIZE_KEY).isNumber());
                list.add(new DeclarationAttributeAsTypeStmt(keyExpr, new NumberType()));
            }

            if (actorDefinitionNode.has(DIRECTION_KEY)) {
                keyExpr = new StringLiteral(DIRECTION_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(DIRECTION_KEY).isNumber());
                list.add(new DeclarationAttributeAsTypeStmt(keyExpr, new NumberType()));
            }

            if (actorDefinitionNode.has(DRAG_KEY)) {
                keyExpr = new StringLiteral(DRAG_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(DRAG_KEY).isBoolean());
                list.add(new DeclarationAttributeAsTypeStmt(keyExpr, new BooleanType()));
            }

            if (actorDefinitionNode.has(ROTATIONSTYLE_KEY)) {
                keyExpr = new StringLiteral(ROTATIONSTYLE_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(ROTATIONSTYLE_KEY).isTextual());
                list.add(new DeclarationAttributeAsTypeStmt(keyExpr, new StringType()));
            }
        }
        return list;
    }

    public static List<SetStmt> parseAttributeDeclarationSetStmts(JsonNode actorDefinitionNode) {
        //String ttSLang = "textToSpeechLanguage"; // Ignored as this is an extension

        StringExpr keyExpr;
        double jsonDouble;
        String jsonString;
        boolean jsonBool;
        NumExpr numExpr;
        StringExpr stringExpr;
        BoolExpr boolExpr;
        SetStmt setStmt;

        List<SetStmt> list = new LinkedList<>();

        if (actorDefinitionNode.has(VOLUME_KEY)) {
            keyExpr = new StringLiteral(VOLUME_KEY);
            Preconditions.checkArgument(actorDefinitionNode.get(VOLUME_KEY).isNumber());
            jsonDouble = actorDefinitionNode.get(VOLUME_KEY).asDouble();
            numExpr = new NumberLiteral((float) jsonDouble);
            setStmt = new SetAttributeTo(keyExpr, numExpr,
                    new NoBlockMetadata());
            list.add(setStmt);
        }

        if (actorDefinitionNode.has(LAYERORDER_KEY)) {
            keyExpr = new StringLiteral(LAYERORDER_KEY);
            Preconditions.checkArgument(actorDefinitionNode.get(LAYERORDER_KEY).isNumber());
            jsonDouble = actorDefinitionNode.get(LAYERORDER_KEY).asDouble();
            numExpr = new NumberLiteral((float) jsonDouble);
            setStmt = new SetAttributeTo(keyExpr, numExpr,
                    new NoBlockMetadata());
            list.add(setStmt);
        }

        if (actorDefinitionNode.get(IS_STAGE_KEY).asBoolean()) {

            if (actorDefinitionNode.has(TEMPO_KEY)) {
                keyExpr = new StringLiteral(TEMPO_KEY);
                jsonDouble = actorDefinitionNode.get(TEMPO_KEY).asDouble();
                Preconditions.checkArgument(actorDefinitionNode.get(TEMPO_KEY).isNumber());
                numExpr = new NumberLiteral((float) jsonDouble);
                setStmt = new SetAttributeTo(keyExpr, numExpr,
                        new NoBlockMetadata());
                list.add(setStmt);
            }

            if (actorDefinitionNode.has(VIDTRANSPARENCY_KEY)) {
                keyExpr = new StringLiteral(VIDTRANSPARENCY_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(VIDTRANSPARENCY_KEY).isNumber());
                jsonDouble = actorDefinitionNode.get(VIDTRANSPARENCY_KEY).asDouble();
                numExpr = new NumberLiteral((float) jsonDouble);
                setStmt = new SetAttributeTo(keyExpr, numExpr,
                        new NoBlockMetadata());
                list.add(setStmt);
            }

            if (actorDefinitionNode.has(VIDSTATE_KEY)) {
                keyExpr = new StringLiteral(VIDSTATE_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(VIDSTATE_KEY).isTextual());
                jsonString = actorDefinitionNode.get(VIDSTATE_KEY).asText();
                stringExpr = new StringLiteral(jsonString);
                setStmt = new SetAttributeTo(keyExpr, stringExpr,
                        new NoBlockMetadata());
                list.add(setStmt);
            }
        } else {

            if (actorDefinitionNode.has(VISIBLE_KEY)) {
                keyExpr = new StringLiteral(VISIBLE_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(VISIBLE_KEY).isBoolean());
                jsonBool = actorDefinitionNode.get(VISIBLE_KEY).asBoolean();
                boolExpr = new BoolLiteral(jsonBool);
                setStmt = new SetAttributeTo(keyExpr, boolExpr,
                        new NoBlockMetadata());
                list.add(setStmt);
            }

            if (actorDefinitionNode.has(X_KEY)) {
                keyExpr = new StringLiteral(X_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(X_KEY).isNumber());
                jsonDouble = actorDefinitionNode.get(X_KEY).asDouble();
                numExpr = new NumberLiteral((float) jsonDouble);
                setStmt = new SetAttributeTo(keyExpr, numExpr,
                        new NoBlockMetadata());
                list.add(setStmt);
            }

            if (actorDefinitionNode.has(Y_KEY)) {
                keyExpr = new StringLiteral(Y_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(Y_KEY).isNumber());
                jsonDouble = actorDefinitionNode.get(Y_KEY).asDouble();
                numExpr = new NumberLiteral((float) jsonDouble);
                setStmt = new SetAttributeTo(keyExpr, numExpr,
                        new NoBlockMetadata());
                list.add(setStmt);
            }

            if (actorDefinitionNode.has(SIZE_KEY)) {
                keyExpr = new StringLiteral(SIZE_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(SIZE_KEY).isNumber());
                jsonDouble = actorDefinitionNode.get(SIZE_KEY).asDouble();
                numExpr = new NumberLiteral((float) jsonDouble);
                setStmt = new SetAttributeTo(keyExpr, numExpr,
                        new NoBlockMetadata());
                list.add(setStmt);
            }

            if (actorDefinitionNode.has(DIRECTION_KEY)) {
                keyExpr = new StringLiteral(DIRECTION_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(DIRECTION_KEY).isNumber());
                jsonDouble = actorDefinitionNode.get(DIRECTION_KEY).asDouble();
                numExpr = new NumberLiteral((float) jsonDouble);
                setStmt = new SetAttributeTo(keyExpr, numExpr,
                        new NoBlockMetadata());
                list.add(setStmt);
            }

            if (actorDefinitionNode.has(DRAG_KEY)) {
                keyExpr = new StringLiteral(DRAG_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(DRAG_KEY).isBoolean());
                jsonBool = actorDefinitionNode.get(DRAG_KEY).asBoolean();
                boolExpr = new BoolLiteral(jsonBool);
                setStmt = new SetAttributeTo(keyExpr, boolExpr,
                        new NoBlockMetadata());
                list.add(setStmt);
            }

            if (actorDefinitionNode.has(ROTATIONSTYLE_KEY)) {
                keyExpr = new StringLiteral(ROTATIONSTYLE_KEY);
                Preconditions.checkArgument(actorDefinitionNode.get(ROTATIONSTYLE_KEY).isTextual());
                jsonString = actorDefinitionNode.get(ROTATIONSTYLE_KEY).asText();
                stringExpr = new StringLiteral(jsonString);
                setStmt = new SetAttributeTo(keyExpr, stringExpr,
                        new NoBlockMetadata());
                list.add(setStmt);
            }
        }
        return list;
    }
}
