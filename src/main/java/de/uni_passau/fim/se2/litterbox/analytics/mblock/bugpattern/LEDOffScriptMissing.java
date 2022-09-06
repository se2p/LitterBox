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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.NumValueVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.RunningState;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDPosition.PositionType;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.*;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.TimedStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RunningState.RUNNING;
import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RunningState.STOPPED;
import static de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDPosition.PositionType.LEFT;
import static de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDPosition.PositionType.RIGHT;

public class LEDOffScriptMissing extends AbstractRobotFinder {

    private static final String NAME = "led_off_script_missing";
    private final Map<PositionType, RunningState> states = new HashMap<>();
    private final Map<PositionType, LEDStmt> lastStmts = new HashMap<>();
    private final Map<PositionType, List<LEDStmt>> lastStmtMap = new HashMap<>();
    private boolean forever = false;

    @Override
    public void visit(Program program) {
        putProceduresinScript = true;
        parseProcedureDefinitions = false;
        ignoreLooseBlocks = true;
        super.visit(program);
    }

    @Override
    public void visit(ScriptList node) {
        lastStmtMap.put(LEFT, new LinkedList<>());
        lastStmtMap.put(RIGHT, new LinkedList<>());
        for (Script script : node.getScriptList()) {
            script.accept(this);

            for (Entry<PositionType, RunningState> entry : states.entrySet()) {
                PositionType position = entry.getKey();
                if (entry.getValue() == STOPPED) {
                    lastStmtMap.put(position, null);
                } else if (entry.getValue() == RUNNING) {
                    List<LEDStmt> lastStmtList = lastStmtMap.get(position);
                    if (lastStmtList != null) {
                        lastStmtList.add(lastStmts.get(position));
                    }
                }
            }
            resetMaps();
        }
        for (Entry<PositionType, List<LEDStmt>> entry : lastStmtMap.entrySet()) {
            List<LEDStmt> list = entry.getValue();
            if (list != null) {
                for (LEDStmt stmt : list) {
                    addIssue(stmt);
                }
            }
        }
        lastStmtMap.clear();
    }

    @Override
    public void visit(Script node) {
        currentScript = node;
        proceduresInScript.put(node, new LinkedList<>());
        currentProcedure = null;
        if (!(node.getEvent() instanceof Never)) {
            node.getStmtList().accept(this);
        }
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        forever = true;
        visit((LoopStmt) node);
    }

    @Override
    public void visit(LEDStmt node) {
        if (!(node instanceof RockyLightStmt)) {
            boolean black = isBlack(node);
            if (black || node instanceof TimedStmt || node instanceof LEDOff) {
                setStopped(node);
            } else {
                setRunning(node);
            }
        }
    }

    private void setStopped(LEDStmt node) {
        if (node instanceof PositionStmt) {
            PositionType position = ((PositionStmt) node).getPosition().getPositionType();
            if (position == PositionType.ALL) {
                states.put(LEFT, STOPPED);
                states.put(RIGHT, STOPPED);
            } else {
                states.put(position, STOPPED);
            }
        } else {
            states.put(LEFT, STOPPED);
        }
    }

    private void setRunning(LEDStmt node) {
        if (node instanceof PositionStmt) {
            PositionType position = ((PositionStmt) node).getPosition().getPositionType();
            if (position == PositionType.ALL) {
                states.put(LEFT, RUNNING);
                states.put(RIGHT, RUNNING);
                lastStmts.put(LEFT, node);
                lastStmts.put(RIGHT, node);
            } else {
                states.put(position, RUNNING);
                lastStmts.put(position, node);
            }
        } else {
            states.put(LEFT, RUNNING);
            lastStmts.put(LEFT, node);
        }
    }

    private void resetMaps() {
        states.clear();
        lastStmts.clear();
        forever = false;
    }

    private boolean isBlack(LEDStmt node) {
        if (node instanceof ColorStmt) {
            StringExpr stringExpr = ((ColorStmt) node).getColorString();
            if (stringExpr instanceof StringLiteral) {
                String string = ((StringLiteral) stringExpr).getText();
                return string.equals("#000000");
            }
        } else if (node instanceof RGBValuesPosition) {
            NumValueVisitor calc = new NumValueVisitor();
            try {
                double red = calc.calculateEndValue(((RGBValuesPosition) node).getRed());
                double green = calc.calculateEndValue(((RGBValuesPosition) node).getGreen());
                double blue = calc.calculateEndValue(((RGBValuesPosition) node).getBlue());
                return red == 0 && green == 0 && blue == 0;
            } catch (Exception ignored) {
            }
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
