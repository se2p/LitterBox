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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ChangeGraphicEffectBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ChangeSoundEffectBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ChangeVolumeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetSoundEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetVolumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.Attribute;
import de.uni_passau.fim.se2.litterbox.cfg.Defineable;
import de.uni_passau.fim.se2.litterbox.cfg.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.cfg.Attribute.AttributeType.*;

/**
 * Checks if a variable is changed multiple times in a row.
 */
public class MultiAttributeModification extends AbstractIssueFinder implements PenExtensionVisitor {

    public static final String NAME = "multiple_attribute_modifications";
    public static final String HINT_PARAMETERISED = "multiple_attribute_modifications_custom";
    public static final String HINT_SAYTHINK = "multiple_attribute_modifications_saythink";
    private Identifier prevIdent = null;
    private ASTNode prevNode = null;

    // TODO: Does not check for list-related issues yet

    @Override
    public void visit(Script script) {
        prevIdent = null;
        prevNode = null;
        super.visit(script);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        prevIdent = null;
        prevNode = null;
        super.visit(node);
    }

    @Override
    public void visit(SetStmtList node) {
        //don't visit these
    }

    @Override
    public void visit(ActorDefinition node) {
        prevIdent = null;
        prevNode = null;
        super.visit(node);
    }

    public void generateMultiBlockIssue(ASTNode node, Hint hint) {
        List<ASTNode> concernedNodes = new ArrayList<>();
        concernedNodes.add(prevNode);
        concernedNodes.add(node);
        MultiBlockIssue issue;
        if (currentScript != null) {
            issue = new MultiBlockIssue(this, IssueSeverity.LOW, program, currentActor, currentScript, concernedNodes, node.getMetadata(), hint);
        } else {
            issue = new MultiBlockIssue(this, IssueSeverity.LOW, program, currentActor, currentProcedure, concernedNodes, node.getMetadata(), hint);
        }
        addIssue(issue);
    }

    public void generateMultiBlockIssue(ASTNode node, Defineable defineable) {
        Hint hint = new Hint(HINT_PARAMETERISED);
        hint.setParameter(Hint.HINT_VARIABLE, getDefineableName(defineable));
        generateMultiBlockIssue(node, hint);
    }

    public void generateMultiBlockIssue(ASTNode node) {
        generateMultiBlockIssue(node, new Hint(NAME));
    }

    @Override
    public void visit(SetVariableTo node) {
        if (prevIdent != null) {
            if (node.getIdentifier().equals(prevIdent)) {
                generateMultiBlockIssue(node, new Variable(node.getIdentifier()));
            }
        }
        prevIdent = node.getIdentifier();
        prevNode = node;
    }

    @Override
    public void visit(ChangeVariableBy node) {
        if (prevIdent != null) {
            if (node.getIdentifier().equals(prevIdent)) {
                generateMultiBlockIssue(node, new Variable(node.getIdentifier()));
            }
        }
        prevIdent = node.getIdentifier();
        prevNode = node;
    }

    @Override
    public void visit(ChangeYBy node) {
        if (prevNode != null && (prevNode instanceof SetYTo || prevNode instanceof ChangeYBy)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), POSITION));
        }

        prevNode = node;
    }

    @Override
    public void visit(SetYTo node) {
        if (prevNode != null && (prevNode instanceof SetYTo || prevNode instanceof ChangeYBy)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), POSITION));
        }

        prevNode = node;
    }

    @Override
    public void visit(ChangeXBy node) {
        if (prevNode != null && (prevNode instanceof SetXTo || prevNode instanceof ChangeXBy)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), POSITION));
        }

        prevNode = node;
    }

    @Override
    public void visit(SetXTo node) {
        if (prevNode != null && (prevNode instanceof SetXTo || prevNode instanceof ChangeXBy)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), POSITION));
        }

        prevNode = node;
    }

    @Override
    public void visit(SetSizeTo node) {
        if (prevNode != null && (prevNode instanceof SetSizeTo || prevNode instanceof ChangeSizeBy)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), SIZE));
        }

        prevNode = node;
    }

    @Override
    public void visit(ChangeSizeBy node) {
        if (prevNode != null && (prevNode instanceof SetSizeTo || prevNode instanceof ChangeSizeBy)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), SIZE));
        }

        prevNode = node;
    }

    @Override
    public void visit(SetVolumeTo node) {
        if (prevNode != null && (prevNode instanceof SetVolumeTo || prevNode instanceof ChangeVolumeBy)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), VOLUME));
        }

        prevNode = node;
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        if (prevNode != null && (prevNode instanceof SetVolumeTo || prevNode instanceof ChangeVolumeBy)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), VOLUME));
        }

        prevNode = node;
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        if (prevNode != null) {
            if ((prevNode instanceof SetGraphicEffectTo
                    && ((SetGraphicEffectTo) prevNode).getEffect().equals(node.getEffect()))
                    || (prevNode instanceof ChangeGraphicEffectBy
                    && ((ChangeGraphicEffectBy) prevNode).getEffect().equals(node.getEffect()))) {
                generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), GRAPHIC_EFFECT));
            }
        }
        prevNode = node;
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        if (prevNode != null) {
            if ((prevNode instanceof SetGraphicEffectTo
                    && ((SetGraphicEffectTo) prevNode).getEffect().equals(node.getEffect()))
                    || (prevNode instanceof ChangeGraphicEffectBy
                    && ((ChangeGraphicEffectBy) prevNode).getEffect().equals(node.getEffect()))) {
                generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), GRAPHIC_EFFECT));
            }
        }
        prevNode = node;
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        if (prevNode != null) {
            if ((prevNode instanceof SetSoundEffectTo
                    && ((SetSoundEffectTo) prevNode).getEffect().equals(node.getEffect()))
                    || (prevNode instanceof ChangeSoundEffectBy
                    && ((ChangeSoundEffectBy) prevNode).getEffect().equals(node.getEffect()))) {
                generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), SOUND_EFFECT));
            }
        }
        prevNode = node;
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        if (prevNode != null) {
            if ((prevNode instanceof SetSoundEffectTo
                    && ((SetSoundEffectTo) prevNode).getEffect().equals(node.getEffect()))
                    || (prevNode instanceof ChangeSoundEffectBy
                    && ((ChangeSoundEffectBy) prevNode).getEffect().equals(node.getEffect()))) {
                generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), SOUND_EFFECT));
            }
        }
        prevNode = node;
    }

    @Override
    public void visit(SetDragMode node) {
        // TODO: We have no attribute for drag mode yet
        if (prevNode instanceof SetDragMode) {
            generateMultiBlockIssue(node);
        }

        prevNode = node;
    }

    @Override
    public void visit(SetRotationStyle node) {
        // TODO: We have no attribute for rotation style yet
        if (prevNode instanceof SetRotationStyle) {
            generateMultiBlockIssue(node);
        }

        prevNode = node;
    }

    @Override
    public void visit(Hide node) {
        if (prevNode instanceof Hide || prevNode instanceof Show) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), VISIBILITY));
        }

        prevNode = node;
    }

    @Override
    public void visit(Show node) {
        if (prevNode != null && (prevNode instanceof Hide || prevNode instanceof Show)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), VISIBILITY));
        }

        prevNode = node;
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        if (prevNode != null && (prevNode instanceof SwitchCostumeTo || prevNode instanceof NextCostume)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), COSTUME));
        }

        prevNode = node;
    }

    @Override
    public void visit(NextCostume node) {
        if (prevNode != null && (prevNode instanceof SwitchCostumeTo || prevNode instanceof NextCostume)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), COSTUME));
        }

        prevNode = node;
    }

    @Override
    public void visit(GoToLayer node) {
        if (prevNode != null && (prevNode instanceof GoToLayer || prevNode instanceof ChangeLayerBy)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), LAYER));
        }

        prevNode = node;
    }

    @Override
    public void visit(ChangeLayerBy node) {
        if (prevNode != null && (prevNode instanceof GoToLayer || prevNode instanceof ChangeLayerBy)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), LAYER));
        }

        prevNode = node;
    }

    @Override
    public void visit(Say node) {
        if (prevNode != null && (prevNode instanceof Say || prevNode instanceof Think)) {
            generateMultiBlockIssue(node, new Hint(HINT_SAYTHINK));
        }

        prevNode = node;
    }

    @Override
    public void visit(Think node) {
        if (prevNode != null && (prevNode instanceof Say || prevNode instanceof Think)) {
            generateMultiBlockIssue(node, new Hint(HINT_SAYTHINK));
        }

        prevNode = node;
    }

    @Override
    public void visit(PointInDirection node) {
        if (prevNode != null && (prevNode instanceof TurnLeft || prevNode instanceof TurnRight || prevNode instanceof PointInDirection)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), ROTATION));
        }

        prevNode = node;
    }

    @Override
    public void visit(TurnLeft node) {
        if (prevNode != null && (prevNode instanceof TurnLeft || prevNode instanceof TurnRight || prevNode instanceof PointInDirection)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), ROTATION));
        }

        prevNode = node;
    }

    @Override
    public void visit(TurnRight node) {
        if (prevNode != null && (prevNode instanceof TurnLeft || prevNode instanceof TurnRight || prevNode instanceof PointInDirection)) {
            generateMultiBlockIssue(node, new Attribute(currentActor.getIdent(), ROTATION));
        }

        prevNode = node;
    }

    @Override
    public void visit(Stmt node) {
        // A statement that does not change a variable, thus at least one other statement between two changes
        // to a variable occurs
        prevIdent = null;
        prevNode = null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public void visit(SetPenSizeTo node) {
        if (prevNode != null && (prevNode instanceof SetPenSizeTo || prevNode instanceof ChangePenSizeBy)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }

        prevNode = node;
    }

    @Override
    public void visit(ChangePenSizeBy node) {
        if (prevNode != null && (prevNode instanceof SetPenSizeTo || prevNode instanceof ChangePenSizeBy)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }

        prevNode = node;
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        if (prevNode instanceof SetPenColorToColorStmt) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }

        prevNode = node;
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        if (prevNode != null) {
            if ((prevNode instanceof SetPenColorParamTo
                    && ((SetPenColorParamTo) prevNode).getParam().equals(node.getParam()))
                    || (prevNode instanceof ChangePenColorParamBy
                    && ((ChangePenColorParamBy) prevNode).getParam().equals(node.getParam()))) {
                addIssue(node, node.getMetadata(), IssueSeverity.LOW);
            }
        }
        prevNode = node;
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        if (prevNode != null) {
            if ((prevNode instanceof SetPenColorParamTo
                    && ((SetPenColorParamTo) prevNode).getParam().equals(node.getParam()))
                    || (prevNode instanceof ChangePenColorParamBy
                    && ((ChangePenColorParamBy) prevNode).getParam().equals(node.getParam()))) {
                addIssue(node, node.getMetadata(), IssueSeverity.LOW);
            }
        }
        prevNode = node;
    }

    @Override
    public Collection<String> getHintKeys() {
        return Arrays.asList(NAME, HINT_PARAMETERISED, HINT_SAYTHINK);
    }
}
