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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDMatrix;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.EmotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.MovingEmotion;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.FacePanelStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.LEDMatrixStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.LEDSwitchOff;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.TurnOffFace;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;

public class MatrixOffScript extends AbstractRobotFinder {
    private static final String NAME = "matrix_off_script";
    private boolean turnsOnMatrix;
    private boolean turnsOffMatrix;
    private boolean secondRun;

    @Override
    public void visit(ActorDefinition node) {
        turnsOnMatrix = false;
        secondRun = false;
        super.visit(node);
        secondRun = true;
        super.visit(node);
    }

    @Override
    public void visit(Script node) {
        turnsOffMatrix = false;
        super.visit(node);
        if (secondRun && !node.getStmtList().getStmts().isEmpty()) {
            ASTNode last = node.getStmtList().getStmts().get(node.getStmtList().getStmts().size() - 1);
            if (last instanceof StopAll || last instanceof StopOtherScriptsInSprite) {
                if (!(node.getEvent() instanceof Never) && turnsOnMatrix && turnsOffMatrix) {
                    addIssue(node.getEvent(), IssueSeverity.MEDIUM);
                }
            }
        }
    }

    @Override
    public void visit(EmotionStmt node) {
        turnsOnMatrix = true;
    }

    @Override
    public void visit(MovingEmotion node) {
        turnsOnMatrix = true;
    }

    @Override
    public void visit(LEDMatrixStmt node) {
        if (!isBlank(node)) {
            turnsOnMatrix = true;
        }
        if (secondRun && isBlank(node)) {
            turnsOffMatrix = true;
        }
    }

    @Override
    public void visit(LEDSwitchOff node) {
        if (secondRun) {
            turnsOffMatrix = true;
        }
    }

    @Override
    public void visit(TurnOffFace node) {
        if (secondRun) {
            turnsOffMatrix = true;
        }
    }

    private boolean isBlank(LEDMatrixStmt node) {
        if (node instanceof FacePanelStmt) {
            LEDMatrix matrix = ((FacePanelStmt) node).getLedMatrix();
            return matrix.getFaceString().equals("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        }
        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
