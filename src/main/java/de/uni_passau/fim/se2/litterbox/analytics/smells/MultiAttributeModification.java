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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;

/**
 * Checks if a variable is changed multiple times in a row.
 */
public class MultiAttributeModification extends AbstractIssueFinder {

    public static final String NAME = "multiple_attribute_modifications";
    private Identifier prevIdent = null;
    private ASTNode prevNode = null;

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

    @Override
    public void visit(SetVariableTo node) {
        if (prevIdent != null) {
            if (node.getIdentifier().equals(prevIdent)) {
                addIssue(node, node.getMetadata());
            }
        }
        prevIdent = node.getIdentifier();
    }

    @Override
    public void visit(ChangeVariableBy node) {
        if (prevIdent != null) {
            if (node.getIdentifier().equals(prevIdent)) {
                addIssue(node, node.getMetadata());
            }
        }
        prevIdent = node.getIdentifier();
    }

    @Override
    public void visit(ChangeYBy node) {
        if (prevNode != null && (prevNode instanceof SetYTo || prevNode instanceof ChangeYBy)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(SetYTo node) {
        if (prevNode != null && (prevNode instanceof SetYTo || prevNode instanceof ChangeYBy)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(ChangeXBy node) {
        if (prevNode != null && (prevNode instanceof SetXTo || prevNode instanceof ChangeXBy)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(SetXTo node) {
        if (prevNode != null && (prevNode instanceof SetXTo || prevNode instanceof ChangeXBy)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(SetSizeTo node) {
        if (prevNode != null && (prevNode instanceof SetSizeTo || prevNode instanceof ChangeSizeBy)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(ChangeSizeBy node) {
        if (prevNode != null && (prevNode instanceof SetSizeTo || prevNode instanceof ChangeSizeBy)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(SetPenSizeTo node) {
        if (prevNode != null && (prevNode instanceof SetPenSizeTo || prevNode instanceof ChangePenSizeBy)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(ChangePenSizeBy node) {
        if (prevNode != null && (prevNode instanceof SetPenSizeTo || prevNode instanceof ChangePenSizeBy)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(SetVolumeTo node) {
        if (prevNode != null && (prevNode instanceof SetVolumeTo || prevNode instanceof ChangeVolumeBy)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        if (prevNode != null && (prevNode instanceof SetVolumeTo || prevNode instanceof ChangeVolumeBy)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        if (prevNode != null && prevNode instanceof SetPenColorToColorStmt) {
            addIssue(node, node.getMetadata());
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
                addIssue(node, node.getMetadata());
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
                addIssue(node, node.getMetadata());
            }
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
                addIssue(node, node.getMetadata());
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
                addIssue(node, node.getMetadata());
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
                addIssue(node, node.getMetadata());
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
                addIssue(node, node.getMetadata());
            }
        }
        prevNode = node;
    }

    @Override
    public void visit(SetDragMode node) {
        if (prevNode != null && (prevNode instanceof SetDragMode)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(SetRotationStyle node) {
        if (prevNode != null && (prevNode instanceof SetRotationStyle)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(Hide node) {
        if (prevNode != null && (prevNode instanceof Hide || prevNode instanceof Show)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(Show node) {
        if (prevNode != null && (prevNode instanceof Hide || prevNode instanceof Show)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        if (prevNode != null && (prevNode instanceof SwitchCostumeTo || prevNode instanceof NextCostume)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(NextCostume node) {
        if (prevNode != null && (prevNode instanceof SwitchCostumeTo || prevNode instanceof NextCostume)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(GoToLayer node) {
        if (prevNode != null && (prevNode instanceof GoToLayer || prevNode instanceof ChangeLayerBy)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(ChangeLayerBy node) {
        if (prevNode != null && (prevNode instanceof GoToLayer || prevNode instanceof ChangeLayerBy)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(Say node) {
        if (prevNode != null && (prevNode instanceof Say || prevNode instanceof Think)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(Think node) {
        if (prevNode != null && (prevNode instanceof Say || prevNode instanceof Think)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(PointInDirection node) {
        if (prevNode != null && (prevNode instanceof TurnLeft || prevNode instanceof TurnRight || prevNode instanceof PointInDirection)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(TurnLeft node) {
        if (prevNode != null && (prevNode instanceof TurnLeft || prevNode instanceof TurnRight || prevNode instanceof PointInDirection)) {
            addIssue(node, node.getMetadata());
        }

        prevNode = node;
    }

    @Override
    public void visit(TurnRight node) {
        if (prevNode != null && (prevNode instanceof TurnLeft || prevNode instanceof TurnRight || prevNode instanceof PointInDirection)) {
            addIssue(node, node.getMetadata());
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
}
