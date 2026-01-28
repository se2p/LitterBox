/*
 * Copyright (C) 2019-2026 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.UnspecifiedNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.quantum.WhenEntanglementStarted;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.quantum.WhenMeasured;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.quantum.WhenSuperpositionStarted;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.WhenVideoMotionGreaterThan;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import de.uni_passau.fim.se2.litterbox.ast.util.KeyValueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsoncreation.BlockJsonCreatorHelper.*;
import static de.uni_passau.fim.se2.litterbox.jsoncreation.JSONStringCreator.createField;

public class ScriptJSONCreator {

    private static final Logger log = Logger.getLogger(ScriptJSONCreator.class.getName());

    private static final IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);

    public static String createScriptJSONString(Script script, SymbolTable symbol) {
        StringBuilder jsonString = new StringBuilder();
        Event event = script.getEvent();
        StmtListJSONCreator stmtListJSONCreator;
        StmtList stmtList = script.getStmtList();
        ExpressionJSONCreator exprCreator = new ExpressionJSONCreator();
        if (event instanceof Never) {
            stmtListJSONCreator = new StmtListJSONCreator(stmtList, symbol);
            jsonString.append(stmtListJSONCreator.createStmtListJSONString());
        } else {
            String blockId = null;
            String nextId = null;

            if (!stmtList.getStmts().isEmpty()) {
                IdVisitor vis = new IdVisitor();
                nextId = vis.getBlockId(stmtList.getStmts().getFirst());
            }

            if (event instanceof AttributeAboveValue attributeAboveValue) {
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) attributeAboveValue.getMetadata();
                blockId = meta.getBlockId();

                List<String> inputs = new ArrayList<>();
                NumExpr numExpr = attributeAboveValue.getValue();
                if (numExpr instanceof UnspecifiedNumExpr) {
                    inputs.add(createTypeInputWithName(VALUE_KEY, INPUT_SAME_BLOCK_SHADOW, MATH_NUM_PRIMITIVE, ""));
                } else if (numExpr instanceof NumberLiteral numberLiteral) {
                    final String number = BlockJsonCreatorHelper.formatNumberLiteral(numberLiteral);
                    inputs.add(createTypeInputWithName(VALUE_KEY, INPUT_SAME_BLOCK_SHADOW, MATH_NUM_PRIMITIVE, number));
                } else {
                    IdJsonStringTuple tuple = exprCreator.createExpressionJSON(meta.getBlockId(),
                            numExpr, symbol);
                    if (tuple.id() == null) {
                        StringBuilder inputString = new StringBuilder();
                        createField(inputString, VALUE_KEY).append(tuple.jsonString());
                        inputs.add(inputString.toString());
                    } else {
                        inputs.add(createReferenceJSON(tuple.id(), VALUE_KEY, true));
                        jsonString.append(tuple.jsonString()).append(",");
                    }
                }

                String attribute = attributeAboveValue.getAttribute().getTypeName();
                String fields = createFields(WHEN_GREATER_THAN_MENU, attribute, null);
                jsonString.append(createBlockWithoutMutationString(meta, nextId, null, createInputs(inputs),
                        fields, attributeAboveValue.getOpcode()));
            } else if (event instanceof WhenVideoMotionGreaterThan videoMotion) {
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) videoMotion.getMetadata();
                blockId = meta.getBlockId();

                List<String> inputs = new ArrayList<>();
                NumExpr numExpr = videoMotion.getReference();
                if (numExpr instanceof UnspecifiedNumExpr) {
                    inputs.add(createTypeInputWithName(REFERENCE_KEY, INPUT_SAME_BLOCK_SHADOW, MATH_NUM_PRIMITIVE, ""));
                } else if (numExpr instanceof NumberLiteral numberLiteral) {
                    final String number = BlockJsonCreatorHelper.formatNumberLiteral(numberLiteral);
                    inputs.add(
                            createTypeInputWithName(REFERENCE_KEY, INPUT_SAME_BLOCK_SHADOW, MATH_NUM_PRIMITIVE, number)
                    );
                } else {
                    IdJsonStringTuple tuple = exprCreator.createExpressionJSON(meta.getBlockId(),
                            numExpr, symbol);
                    if (tuple.id() == null) {
                        StringBuilder inputString = new StringBuilder();
                        createField(inputString, REFERENCE_KEY).append(tuple.jsonString());
                        inputs.add(inputString.toString());
                    } else {
                        inputs.add(createReferenceJSON(tuple.id(), REFERENCE_KEY, true));
                        jsonString.append(tuple.jsonString()).append(",");
                    }
                }

                jsonString.append(createBlockWithoutMutationString(meta, nextId, null, createInputs(inputs),
                        EMPTY_VALUE, videoMotion.getOpcode()));
            } else if (event instanceof BackdropSwitchTo backdropSwitchTo) {
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) backdropSwitchTo.getMetadata();
                blockId = meta.getBlockId();

                String name = backdropSwitchTo.getBackdrop().getName();
                String fields = createFields(BACKDROP_INPUT, name, null);
                String blockJson = createBlockWithoutMutationString(meta, nextId, null, EMPTY_VALUE, fields,
                        backdropSwitchTo.getOpcode());
                jsonString.append(blockJson);
            } else if (event instanceof GreenFlag greenFlag) {
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) greenFlag.getMetadata();
                blockId = meta.getBlockId();
                jsonString.append(createFixedBlock(meta, nextId, null, greenFlag.getOpcode()));
            } else if (event instanceof KeyPressed keyPressed) {
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) keyPressed.getMetadata();
                blockId = meta.getBlockId();

                Preconditions.checkArgument(keyPressed.getKey().getKey() instanceof NumberLiteral);
                String key = KeyValueTranslator.getKeyValue(
                        translator, (int) ((NumberLiteral) keyPressed.getKey().getKey()).getValue()
                );

                String fields = createFields(KEY_OPTION, key, null);
                String blockJson = createBlockWithoutMutationString(meta, nextId, null, EMPTY_VALUE, fields,
                        keyPressed.getOpcode());
                jsonString.append(blockJson);
            } else if (event instanceof ReceptionOfMessage receptionOfMessage) {
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) receptionOfMessage.getMetadata();
                blockId = meta.getBlockId();

                StringExpr expr = receptionOfMessage.getMsg().getMessage();
                Preconditions.checkArgument(expr instanceof StringLiteral);
                String messageText = ((StringLiteral) expr).getText();
                String id;
                if (symbol.getMessage(messageText).isPresent()) {
                    id = symbol.getMessage(messageText).get().identifier();
                } else {
                    id = "unspecified" + messageText;
                }
                String fields = createFields(BROADCAST_OPTION, messageText, id);
                String blockJson = createBlockWithoutMutationString(meta, nextId, null, EMPTY_VALUE, fields,
                        receptionOfMessage.getOpcode());
                jsonString.append(blockJson);
            } else if (event instanceof SpriteClicked spriteClicked) {
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) spriteClicked.getMetadata();
                blockId = meta.getBlockId();
                jsonString.append(createFixedBlock(meta, nextId, null, spriteClicked.getOpcode()));
            } else if (event instanceof StageClicked stageClicked) {
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) stageClicked.getMetadata();
                blockId = meta.getBlockId();
                jsonString.append(createFixedBlock(meta, nextId, null, stageClicked.getOpcode()));
            } else if (event instanceof StartedAsClone startedAsClone) {
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) startedAsClone.getMetadata();
                blockId = meta.getBlockId();
                jsonString.append(createFixedBlock(meta, nextId, null, startedAsClone.getOpcode()));
            } else if (event instanceof WhenMeasured whenMeasured) {
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) whenMeasured.getMetadata();
                blockId = meta.getBlockId();
                jsonString.append(createFixedBlock(meta, nextId, null, whenMeasured.getOpcode()));
            } else if (event instanceof WhenSuperpositionStarted whenSuperpositionStarted) {
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) whenSuperpositionStarted.getMetadata();
                blockId = meta.getBlockId();
                jsonString.append(createFixedBlock(meta, nextId, null, whenSuperpositionStarted.getOpcode()));
            } else if (event instanceof WhenEntanglementStarted whenEntanglementStarted) {
                TopNonDataBlockMetadata meta = (TopNonDataBlockMetadata) whenEntanglementStarted.getMetadata();
                blockId = meta.getBlockId();
                jsonString.append(createFixedBlock(meta, nextId, null, whenEntanglementStarted.getOpcode()));
            }

            if (!script.getStmtList().getStmts().isEmpty()) {
                assert blockId != null;
                stmtListJSONCreator = new StmtListJSONCreator(blockId, stmtList, symbol);
                String stmtListJson = stmtListJSONCreator.createStmtListJSONString();
                if (!stmtListJson.isBlank()) {
                    jsonString.append(",");
                    jsonString.append(stmtListJson);
                } else {
                    log.warning("Obtained empty JSON for non-empty statement list. Event ID: " + blockId);
                }
            }
        }
        return jsonString.toString();
    }
}
