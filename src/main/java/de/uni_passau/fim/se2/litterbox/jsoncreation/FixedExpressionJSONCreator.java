/*
 * Copyright (C) 2019-2024 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.WithCloneExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.ChangePenColorParamBy;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.SetPenColorParamTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockWithMenuMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockWithMenuMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StartSound;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointTowards;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsoncreation.BlockJsonCreatorHelper.*;

/**
 * This class is for creating the JSONs of predetermined drop downs, that could be replaced by expressions such as
 * {@link de.uni_passau.fim.se2.litterbox.ast.model.position.Position} or
 * {@link de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice}.
 */
public class FixedExpressionJSONCreator implements ScratchVisitor, PenExtensionVisitor, TextToSpeechExtensionVisitor {
    private List<String> finishedJSONStrings;
    private String previousBlockId = null;
    private String topExpressionId = null;
    private Opcode dependantOpcode;

    public IdJsonStringTuple createFixedExpressionJSON(String parentId, ASTNode expression, Opcode dependantOpcode) {
        finishedJSONStrings = new ArrayList<>();
        topExpressionId = null;
        previousBlockId = parentId;
        this.dependantOpcode = dependantOpcode;
        expression.accept(this);
        StringBuilder jsonString = new StringBuilder();
        for (int i = 0; i < finishedJSONStrings.size() - 1; i++) {
            jsonString.append(finishedJSONStrings.get(i)).append(",");
        }
        if (!finishedJSONStrings.isEmpty()) {
            jsonString.append(finishedJSONStrings.get(finishedJSONStrings.size() - 1));
        }
        return new IdJsonStringTuple(topExpressionId, jsonString.toString());
    }

    @Override
    public void visit(RandomPos node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), TO_KEY, RANDOM);
    }

    @Override
    public void visit(MousePos node) {
        if (node.getParentNode() instanceof GoToPos || node.getParentNode() instanceof GlideSecsTo) {
            createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), TO_KEY, MOUSE);
        } else if (node.getParentNode() instanceof PointTowards) {
            createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), TOWARDS_KEY, MOUSE);
        } else if (node.getParentNode() instanceof DistanceTo) {
            createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), DISTANCETOMENU_KEY, MOUSE);
        }
    }

    @Override
    public void visit(FromExpression node) {
        if (node.getStringExpr() instanceof AsString asString && asString.getOperand1() instanceof StrId strId) {
            NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();

            if (node.getParentNode() instanceof GoToPos || node.getParentNode() instanceof GlideSecsTo) {
                createFieldsExpression(metadata, TO_KEY, strId.getName());
            } else if (node.getParentNode() instanceof PointTowards) {
                createFieldsExpression(metadata, TOWARDS_KEY, strId.getName());
            } else if (node.getParentNode() instanceof DistanceTo) {
                createFieldsExpression(metadata, DISTANCETOMENU_KEY, strId.getName());
            }
        }
    }

    @Override
    public void visit(Next node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), BACKDROP_INPUT, NEXT_BACKDROP);
    }

    @Override
    public void visit(Prev node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), BACKDROP_INPUT, PREVIOUS_BACKDROP);
    }

    @Override
    public void visit(Random node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), BACKDROP_INPUT, RANDOM_BACKDROP);
    }

    @Override
    public void visit(WithExpr node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();

        if (node.getExpression() instanceof StrId strId) {
            if (node.getParentNode() instanceof AttributeOf) {
                createFieldsExpression(metadata, OBJECT_KEY, strId.getName());
            } else if (node.getParentNode() instanceof PlaySoundUntilDone
                    || node.getParentNode() instanceof StartSound) {
                createFieldsExpression(metadata, SOUND_MENU, strId.getName());
            } else if (node.getParentNode() instanceof SwitchBackdrop
                    || node.getParentNode() instanceof SwitchBackdropAndWait) {
                createFieldsExpression(metadata, BACKDROP_INPUT, strId.getName());
            } else if (node.getParentNode() instanceof SwitchCostumeTo) {
                createFieldsExpression(metadata, COSTUME_INPUT, strId.getName());
            }
        }
    }

    @Override
    public void visit(Myself node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), CLONE_OPTION, "_myself_");
    }

    @Override
    public void visit(WithCloneExpr node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();

        if (node.getExpression() instanceof StrId strId) {
            createFieldsExpression(metadata, CLONE_OPTION, strId.getName());
        }
    }

    @Override
    public void visit(Key node) {
        NumExpr numExpr = node.getKey();
        if (numExpr instanceof NumberLiteral numberLiteral) {
            createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), KEY_OPTION,
                    getKeyValue((int) numberLiteral.getValue()));
        }
    }

    @Override
    public void visit(MousePointer node) {
        if (node.getParentNode() instanceof DistanceTo) {
            createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), DISTANCETOMENU_KEY, MOUSE);
        } else if (node.getParentNode() instanceof Touching) {
            createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), TOUCHINGOBJECTMENU, MOUSE);
        }
    }

    @Override
    public void visit(Edge node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), TOUCHINGOBJECTMENU, TOUCHING_EDGE);
    }

    @Override
    public void visit(SpriteTouchable node) {
        if (node.getParentNode() instanceof DistanceTo) {
            createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), DISTANCETOMENU_KEY, ((StringLiteral) node.getStringExpr()).getText());
        } else if (node.getParentNode() instanceof Touching) {
            createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), TOUCHINGOBJECTMENU, ((StringLiteral) node.getStringExpr()).getText());
        }
    }

    private void createFieldsExpression(NonDataBlockMetadata metadata, String fieldsName, String fieldsValue) {
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }

        String fieldsString = createFields(fieldsName, fieldsValue, null);
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, EMPTY_VALUE, fieldsString, dependantOpcode));
    }

    //pen

    @Override
    public void visit(ChangePenColorParamBy node) {
        StringExpr stringExpr = node.getParam();
        if (stringExpr instanceof StringLiteral stringLiteral) {
            String strid = stringLiteral.getText();
            NonDataBlockMetadata menuMetadata;
            if (node.getMetadata() instanceof TopNonDataBlockWithMenuMetadata metadata) {
                menuMetadata = (NonDataBlockMetadata) metadata.getMenuMetadata();
            } else {
                NonDataBlockWithMenuMetadata metadata = (NonDataBlockWithMenuMetadata) node.getMetadata();
                menuMetadata = (NonDataBlockMetadata) metadata.getMenuMetadata();
            }
            createFieldsExpression(menuMetadata, COLOR_PARAM_LITTLE_KEY,
                    strid);
        }
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        StringExpr stringExpr = node.getParam();
        if (stringExpr instanceof StringLiteral stringLiteral) {
            String strid = stringLiteral.getText();
            NonDataBlockMetadata menuMetadata;
            if (node.getMetadata() instanceof TopNonDataBlockWithMenuMetadata metadata) {
                menuMetadata = (NonDataBlockMetadata) metadata.getMenuMetadata();
            } else {
                NonDataBlockWithMenuMetadata metadata = (NonDataBlockWithMenuMetadata) node.getMetadata();
                menuMetadata = (NonDataBlockMetadata) metadata.getMenuMetadata();
            }
            createFieldsExpression(menuMetadata, COLOR_PARAM_LITTLE_KEY,
                    strid);
        }
    }

    //Text to Speech

    @Override
    public void visit(FixedLanguage node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), LANGUAGE_FIELDS_KEY, node.getType().getType());
    }

    @Override
    public void visit(FixedVoice node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), VOICE_FIELDS_KEY, node.getType().getType());
    }
}
