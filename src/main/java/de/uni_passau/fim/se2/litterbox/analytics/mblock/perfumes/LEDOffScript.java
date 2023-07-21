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
import de.uni_passau.fim.se2.litterbox.analytics.NumValueVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.*;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;

public class LEDOffScript extends AbstractRobotFinder {
    private static final String NAME = "led_off_script";
    private boolean turnsOnLED;
    private boolean turnsOffLED;
    private boolean secondRun;

    @Override
    public void visit(ActorDefinition node) {
        turnsOnLED = false;

        secondRun = false;
        super.visit(node);
        secondRun = true;
        super.visit(node);
    }

    @Override
    public void visit(Script node) {
        turnsOffLED = false;
        super.visit(node);
        if (secondRun && !node.getStmtList().getStmts().isEmpty()) {
            ASTNode last = node.getStmtList().getStmts().get(node.getStmtList().getStmts().size() - 1);
            if (last instanceof StopAll || last instanceof StopOtherScriptsInSprite) {
                if (!(node.getEvent() instanceof Never)
                        && turnsOnLED && turnsOffLED) {
                    addIssue(node.getEvent(), IssueSeverity.MEDIUM);
                }
            }
        }
    }

    @Override
    public void visit(LEDColorShow node) {
        if (!isBlack(node)) {
            turnsOnLED = true;
        }
        if (secondRun && isBlack(node)) {
            turnsOffLED = true;
        }
    }

    @Override
    public void visit(LEDColorShowPosition node) {
        if (!isBlack(node)) {
            turnsOnLED = true;
        }
        if (secondRun && isBlack(node)) {
            turnsOffLED = true;
        }
    }

    @Override
    public void visit(LEDColorTimed node) {
        if (!isBlack(node)) {
            turnsOnLED = true;
        }
        if (secondRun && isBlack(node)) {
            turnsOffLED = true;
        }
    }

    @Override
    public void visit(LEDColorTimedPosition node) {
        if (!isBlack(node)) {
            turnsOnLED = true;
        }
        if (secondRun && isBlack(node)) {
            turnsOffLED = true;
        }
    }

    @Override
    public void visit(RGBValue node) {
        if (!isBlack(node)) {
            turnsOnLED = true;
        }
        if (secondRun && isBlack(node)) {
            turnsOffLED = true;
        }
    }

    @Override
    public void visit(RGBValuesPosition node) {
        if (!isBlack(node)) {
            turnsOnLED = true;
        }
        if (secondRun && isBlack(node)) {
            turnsOffLED = true;
        }
    }

    @Override
    public void visit(LEDOff node) {
        if (secondRun) {
            turnsOffLED = true;
        }
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

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
