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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.smells;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.EmotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.MovingEmotion;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveSides;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveStop;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.cfg.Defineable;
import de.uni_passau.fim.se2.litterbox.cfg.RobotAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.cfg.RobotAttribute.AttributeType.*;

/**
 * Checks if a variable is changed multiple times in a row.
 */
public class MultiAttributeModificationRobot extends AbstractRobotFinder {

    public static final String NAME = "multiple_attribute_modifications_robot";
    public static final String HINT_PARAMETERISED = "multiple_attribute_modifications_custom_robot";
    private ASTNode prevNode = null;

    // TODO: Does not check for list-related issues yet

    @Override
    public void visit(Script script) {
        prevNode = null;
        super.visit(script);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        prevNode = null;
        super.visit(node);
    }

    @Override
    public void visit(SetStmtList node) {
        //don't visit these
    }

    @Override
    public void visit(ActorDefinition node) {
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
        Hint hint = Hint.fromKey(HINT_PARAMETERISED);
        hint.setParameter(Hint.HINT_VARIABLE, new HintPlaceholder.Defineable(defineable));
        generateMultiBlockIssue(node, hint);
    }

    @Override
    public void visit(LEDColorShow node) {
        if (prevNode != null && (prevNode instanceof LEDColorShow || prevNode instanceof LEDColorShowPosition || prevNode instanceof RGBValue || prevNode instanceof RGBValuesPosition || prevNode instanceof LEDOff)) {
            generateMultiBlockIssue(node, new RobotAttribute(currentActor.getIdent(), LED));
        }

        prevNode = node;
    }

    @Override
    public void visit(LEDColorShowPosition node) {
        if (prevNode != null && (prevNode instanceof LEDColorShow || prevNode instanceof LEDColorShowPosition || prevNode instanceof RGBValue || prevNode instanceof RGBValuesPosition || prevNode instanceof LEDOff)) {
            generateMultiBlockIssue(node, new RobotAttribute(currentActor.getIdent(), LED));
        }

        prevNode = node;
    }

    @Override
    public void visit(RGBValuesPosition node) {
        if (prevNode != null && (prevNode instanceof LEDColorShow || prevNode instanceof LEDColorShowPosition || prevNode instanceof RGBValue || prevNode instanceof RGBValuesPosition || prevNode instanceof LEDOff)) {
            generateMultiBlockIssue(node, new RobotAttribute(currentActor.getIdent(), LED));
        }

        prevNode = node;
    }

    @Override
    public void visit(RGBValue node) {
        if (prevNode != null && (prevNode instanceof LEDColorShow || prevNode instanceof LEDColorShowPosition || prevNode instanceof RGBValue || prevNode instanceof RGBValuesPosition || prevNode instanceof LEDOff)) {
            generateMultiBlockIssue(node, new RobotAttribute(currentActor.getIdent(), LED));
        }

        prevNode = node;
    }

    @Override
    public void visit(LEDOff node) {
        if (prevNode != null && (prevNode instanceof LEDColorShow || prevNode instanceof LEDColorShowPosition || prevNode instanceof RGBValue || prevNode instanceof RGBValuesPosition || prevNode instanceof LEDOff)) {
            generateMultiBlockIssue(node, new RobotAttribute(currentActor.getIdent(), LED));
        }

        prevNode = node;
    }

    @Override
    public void visit(RockyLight node) {
        if (prevNode != null && (prevNode instanceof RockyLight || prevNode instanceof RockyLightOff)) {
            generateMultiBlockIssue(node, new RobotAttribute(currentActor.getIdent(), ROCKY_LIGHT));
        }

        prevNode = node;
    }

    @Override
    public void visit(RockyLightOff node) {
        if (prevNode != null && (prevNode instanceof RockyLight || prevNode instanceof RockyLightOff)) {
            generateMultiBlockIssue(node, new RobotAttribute(currentActor.getIdent(), ROCKY_LIGHT));
        }

        prevNode = node;
    }

    @Override
    public void visit(FacePosition node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(FacePositionPort node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(LEDNumPort node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(LEDString node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(LEDStringPort node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(EmotionStmt node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(LEDStringScrolling node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(LEDStringPosition node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(LEDStringPositionPort node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(LEDSwitchOff node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(LEDSwitchOn node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(LEDToggle node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(ShowFace node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(ShowFacePort node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(TurnOffFace node) {
        handleMatrixNodes(node);
    }

    @Override
    public void visit(TurnOffFacePort node) {
        handleMatrixNodes(node);
    }

    private void handleMatrixNodes(ASTNode node) {
        if (prevNode != null && (prevNode instanceof FacePosition || prevNode instanceof FacePositionPort || prevNode instanceof LEDNumPort || prevNode instanceof LEDString || prevNode instanceof LEDStringPort || prevNode instanceof LEDStringPosition || prevNode instanceof LEDStringPositionPort || prevNode instanceof LEDSwitchOff || prevNode instanceof LEDSwitchOn || prevNode instanceof LEDToggle || prevNode instanceof ShowFace || prevNode instanceof ShowFacePort || prevNode instanceof TurnOffFace || prevNode instanceof TurnOffFacePort)) {
            generateMultiBlockIssue(node, new RobotAttribute(currentActor.getIdent(), MATRIX));
        }

        prevNode = node;
    }

    @Override
    public void visit(MoveDirection node) {
        if (prevNode != null && (prevNode instanceof MoveDirection || prevNode instanceof MoveSides || prevNode instanceof MoveStop)) {
            generateMultiBlockIssue(node, new RobotAttribute(currentActor.getIdent(), MOTOR_POWER));
        }

        prevNode = node;
    }

    @Override
    public void visit(MoveSides node) {
        if (prevNode != null && (prevNode instanceof MoveDirection || prevNode instanceof MoveSides || prevNode instanceof MoveStop)) {
            generateMultiBlockIssue(node, new RobotAttribute(currentActor.getIdent(), MOTOR_POWER));
        }

        prevNode = node;
    }

    @Override
    public void visit(MoveStop node) {
        if (prevNode != null && (prevNode instanceof MoveDirection || prevNode instanceof MoveSides || prevNode instanceof MoveStop)) {
            generateMultiBlockIssue(node, new RobotAttribute(currentActor.getIdent(), MOTOR_POWER));
        }

        prevNode = node;
    }

    @Override
    public void visit(MovingEmotion node) {
        if (prevNode != null && (prevNode instanceof MoveDirection || prevNode instanceof MoveSides || prevNode instanceof MoveStop)) {
            generateMultiBlockIssue(node, new RobotAttribute(currentActor.getIdent(), MOTOR_POWER));
        }

        prevNode = node;
    }

    @Override
    public void visit(Stmt node) {
        // A statement that does not change a variable, thus at least one other statement between two changes
        // to a variable occurs
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
    public Collection<String> getHintKeys() {
        return Arrays.asList(NAME, HINT_PARAMETERISED);
    }
}
