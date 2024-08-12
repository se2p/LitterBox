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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.MultiBlockIssue;
import de.uni_passau.fim.se2.litterbox.analytics.NumValueVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDPosition.PositionType;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.MCorePort.PortType;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.EmotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.MovingEmotion;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.LEDMatrixStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.PortStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.TurnOffFace;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.TurnOffFacePort;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveSides;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveStop;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.RobotMoveStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.ChangeVolumeBy2;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.SetVolumeTo2;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.SpeakerStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.StopAllSounds2;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity.MEDIUM;
import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.*;

public class ParallelResourceUse extends AbstractRobotFinder {

    private static final String NAME = "parallel_resource_use";
    private final Map<Script, Stmt> motorsUseNode = new HashMap<>();
    private final Map<Script, Stmt> matrix1UseNode = new HashMap<>();
    private final Map<Script, Stmt> matrix2UseNode = new HashMap<>();
    private final Map<Script, Stmt> matrix3UseNode = new HashMap<>();
    private final Map<Script, Stmt> matrix4UseNode = new HashMap<>();
    private final Map<Script, Stmt> rockyLightUseNode = new HashMap<>();
    private final Map<Script, Stmt> led1UseNode = new HashMap<>();
    private final Map<Script, Stmt> led2UseNode = new HashMap<>();
    private final Map<Script, Stmt> soundUseNode = new HashMap<>();

    @Override
    public void visit(Program program) {
        putProceduresinScript = true;
        parseProcedureDefinitions = false;
        ignoreLooseBlocks = true;
        super.visit(program);
    }

    @Override
    public void visit(ActorDefinition node) {
        ignoreLooseBlocks = true;
        Preconditions.checkNotNull(program);
        currentActor = node;
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
        String actorName = node.getIdent().getName();
        robot = getRobot(actorName, node.getSetStmtList());
        if (!(robot == CODEY || robot == MCORE)) {
            finishRobotVisit();
            resetMaps();
            return;
        }
        visitChildren(node);
        detectAllIssues();
        resetMaps();
    }

    @Override
    public void visit(RobotMoveStmt node) {
        boolean zeroPower = isZeroPower(node);
        if (!zeroPower && !(node instanceof MoveStop)) {
            addEntry(node, motorsUseNode);
        }
    }

    @Override
    public void visit(LEDMatrixStmt node) {
        if (node instanceof TurnOffFace || node instanceof TurnOffFacePort) {
            return;
        }
        if (node instanceof PortStmt portStmt) {
            PortType port = portStmt.getPort().getPortType();
            switch (port) {
                case PORT_1, PORT_ON_BOARD -> addEntry(node, matrix1UseNode);
                case PORT_2 -> addEntry(node, matrix2UseNode);
                case PORT_3 -> addEntry(node, matrix3UseNode);
                case PORT_4 -> addEntry(node, matrix4UseNode);
            }
        } else {
            addEntry(node, matrix1UseNode);
        }
    }

    @Override
    public void visit(EmotionStmt node) {
        addEntry(node, matrix1UseNode);
        if (node instanceof MovingEmotion) {
            addEntry(node, motorsUseNode);
        }
    }

    @Override
    public void visit(RockyLight node) {
        addEntry(node, rockyLightUseNode);
    }

    @Override
    public void visit(LEDStmt node) {
        boolean black = isBlack(node);
        if (black || node instanceof LEDOff) {
            return;
        }
        if (node instanceof PositionStmt positionStmt) {
            PositionType position = positionStmt.getPosition().getPositionType();
            switch (position) {
                case ALL -> {
                    addEntry(node, led1UseNode);
                    addEntry(node, led2UseNode);
                }
                case LEFT -> addEntry(node, led1UseNode);
                case RIGHT -> addEntry(node, led2UseNode);
            }
        } else {
            addEntry(node, led1UseNode);
        }
    }

    @Override
    public void visit(SpeakerStmt node) {
        if (!(node instanceof ChangeVolumeBy2 || node instanceof SetVolumeTo2 || node instanceof StopAllSounds2)) {
            addEntry(node, soundUseNode);
        }
    }

    private void addEntry(Stmt node, Map<Script, Stmt> map) {
        if (!map.containsKey(currentScript)) {
            map.put(currentScript, node);
        }
    }

    private void detectAllIssues() {
        detectIssue(motorsUseNode);
        detectIssue(matrix1UseNode);
        detectIssue(matrix2UseNode);
        detectIssue(matrix3UseNode);
        detectIssue(matrix4UseNode);
        detectIssue(rockyLightUseNode);
        detectIssue(led1UseNode);
        detectIssue(led2UseNode);
        detectIssue(soundUseNode);
    }

    private void detectIssue(Map<Script, Stmt> map) {
        List<ScriptEntity> scriptList = new LinkedList<>(map.keySet());
        List<ASTNode> nodeList = new LinkedList<>(map.values());
        List<Metadata> metadataList = new LinkedList<>();
        nodeList.forEach(node -> metadataList.add(node.getMetadata()));
        if (nodeList.size() >= 2) {
            MultiBlockIssue issue = new MultiBlockIssue(this, MEDIUM, program, currentActor, scriptList, nodeList,
                    nodeList.get(0).getMetadata(), new Hint(getName()));
            addIssue(issue);
        }
    }

    private void resetMaps() {
        motorsUseNode.clear();
        matrix1UseNode.clear();
        matrix2UseNode.clear();
        matrix3UseNode.clear();
        matrix4UseNode.clear();
        rockyLightUseNode.clear();
        led1UseNode.clear();
        led2UseNode.clear();
        soundUseNode.clear();
    }

    private boolean isBlack(LEDStmt node) {
        if (node instanceof ColorStmt colorStmt) {
            StringExpr stringExpr = colorStmt.getColorString();
            if (stringExpr instanceof StringLiteral stringLiteral) {
                String string = stringLiteral.getText();
                return string.equals("#000000");
            }
        } else if (node instanceof RGBValuesPosition rgbValuesPosition) {
            NumValueVisitor calc = new NumValueVisitor();
            try {
                double red = calc.calculateEndValue(rgbValuesPosition.getRed());
                double green = calc.calculateEndValue(rgbValuesPosition.getGreen());
                double blue = calc.calculateEndValue(rgbValuesPosition.getBlue());
                return red == 0 && green == 0 && blue == 0;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private boolean isZeroPower(RobotMoveStmt node) {
        NumValueVisitor calc = new NumValueVisitor();
        try {
            if (node instanceof MoveDirection moveDirection) {
                return 0 == calc.calculateEndValue(moveDirection.getPercent());
            } else if (node instanceof MoveSides moveSides) {
                double left = calc.calculateEndValue(moveSides.getLeftPower());
                double right = calc.calculateEndValue(moveSides.getRightPower());
                return left == 0 && right == 0;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
