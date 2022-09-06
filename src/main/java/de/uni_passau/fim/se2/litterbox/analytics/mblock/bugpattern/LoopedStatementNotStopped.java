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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.LEDOff;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.LEDStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.RockyLight;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.RockyLightOff;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.LEDMatrixStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.PortStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.TurnOffFace;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.TurnOffFacePort;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveStop;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.RobotMoveStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.SpeakerStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.StopAllSounds2;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.LoopedStatementNotStopped.LoopStopState.NONE;

public class LoopedStatementNotStopped extends AbstractRobotFinder {

    private static final String NAME = "looped_statement_not_stopped";
    private final Set<Script> stopScripts = new HashSet<>();
    private final Map<Script, LoopStopState> motors = new HashMap<>();
    private final Map<Script, LoopStopState> matrix1 = new HashMap<>();
    private final Map<Script, LoopStopState> matrix2 = new HashMap<>();
    private final Map<Script, LoopStopState> matrix3 = new HashMap<>();
    private final Map<Script, LoopStopState> matrix4 = new HashMap<>();
    private final Map<Script, LoopStopState> rockyLight = new HashMap<>();
    private final Map<Script, LoopStopState> led = new HashMap<>();
    private final Map<Script, LoopStopState> sound = new HashMap<>();
    private final Map<Script, Stmt> motorsStopNode = new HashMap<>();
    private final Map<Script, Stmt> matrix1StopNode = new HashMap<>();
    private final Map<Script, Stmt> matrix2StopNode = new HashMap<>();
    private final Map<Script, Stmt> matrix3StopNode = new HashMap<>();
    private final Map<Script, Stmt> matrix4StopNode = new HashMap<>();
    private final Map<Script, Stmt> rockyLightStopNode = new HashMap<>();
    private final Map<Script, Stmt> ledStopNode = new HashMap<>();
    private final Map<Script, Stmt> soundStopNode = new HashMap<>();
    private boolean inLoop;

    @Override
    public Set<Issue> check(Program node) {
        parseProcedureDefinitions = false;
        putProceduresinScript = true;
        ignoreLooseBlocks = true;
        return super.check(node);
    }

    @Override
    public void visit(ScriptList node) {
        for (Script script : node.getScriptList()) {
            motors.put(script, NONE);
            matrix1.put(script, NONE);
            matrix2.put(script, NONE);
            matrix3.put(script, NONE);
            matrix4.put(script, NONE);
            rockyLight.put(script, NONE);
            led.put(script, NONE);
            sound.put(script, NONE);

            script.accept(this);
        }
        findAllIssues();
        resetMaps();
    }

    @Override
    public void visit(StopAll node) {
        stopScripts.add(currentScript);
    }

    @Override
    public void visit(LoopStmt node) {
        boolean nestedLoop = inLoop;
        inLoop = true;

        visitChildren(node);

        inLoop = nestedLoop;
    }

    @Override
    public void visit(RobotMoveStmt node) {
        if (inLoop) {
            motors.put(currentScript, motors.get(currentScript).setLooped());
        }
    }

    @Override
    public void visit(LEDMatrixStmt node) {
        if (inLoop) {
            if (node instanceof PortStmt) {
                switch (((PortStmt) node).getPort().getPortType()) {

                    default:
                    case PORT_1:
                        matrix1.put(currentScript, matrix1.get(currentScript).setLooped());
                        break;

                    case PORT_2:
                        matrix2.put(currentScript, matrix2.get(currentScript).setLooped());
                        break;

                    case PORT_3:
                        matrix3.put(currentScript, matrix3.get(currentScript).setLooped());
                        break;

                    case PORT_4:
                        matrix4.put(currentScript, matrix4.get(currentScript).setLooped());
                        break;
                }
            } else {
                matrix1.put(currentScript, matrix1.get(currentScript).setLooped());
            }
        }
    }

    @Override
    public void visit(RockyLight node) {
        if (inLoop) {
            rockyLight.put(currentScript, rockyLight.get(currentScript).setLooped());
        }
    }

    @Override
    public void visit(LEDStmt node) {
        if (inLoop) {
            led.put(currentScript, led.get(currentScript).setLooped());
        }
    }

    @Override
    public void visit(SpeakerStmt node) {
        if (inLoop) {
            sound.put(currentScript, sound.get(currentScript).setLooped());
        }
    }

    @Override
    public void visit(MoveStop node) {
        motors.put(currentScript, motors.get(currentScript).setStopped());
        motorsStopNode.put(currentScript, node);
    }

    @Override
    public void visit(TurnOffFace node) {
        matrix1.put(currentScript, matrix1.get(currentScript).setStopped());
        matrix1StopNode.put(currentScript, node);
    }

    @Override
    public void visit(TurnOffFacePort node) {
        switch (node.getPort().getPortType()) {

            default:
            case PORT_1:
                matrix1.put(currentScript, matrix1.get(currentScript).setStopped());
                matrix1StopNode.put(currentScript, node);
                break;

            case PORT_2:
                matrix2.put(currentScript, matrix2.get(currentScript).setStopped());
                matrix2StopNode.put(currentScript, node);
                break;

            case PORT_3:
                matrix3.put(currentScript, matrix3.get(currentScript).setStopped());
                matrix3StopNode.put(currentScript, node);
                break;

            case PORT_4:
                matrix4.put(currentScript, matrix4.get(currentScript).setStopped());
                matrix4StopNode.put(currentScript, node);
                break;
        }
    }

    @Override
    public void visit(RockyLightOff node) {
        rockyLight.put(currentScript, rockyLight.get(currentScript).setStopped());
        rockyLightStopNode.put(currentScript, node);
    }

    @Override
    public void visit(LEDOff node) {
        led.put(currentScript, led.get(currentScript).setStopped());
        ledStopNode.put(currentScript, node);
    }

    @Override
    public void visit(StopAllSounds2 node) {
        sound.put(currentScript, sound.get(currentScript).setStopped());
        soundStopNode.put(currentScript, node);
    }

    private void findIssues(Map<Script, LoopStopState> map, Map<Script, Stmt> list) {
        Set<Script> issueScripts = new HashSet<>();
        for (Entry<Script, LoopStopState> loopEntry : map.entrySet()) {
            boolean loop = loopEntry.getValue().isLooped();
            Script loopScript = loopEntry.getKey();
            for (Entry<Script, LoopStopState> stopEntry : map.entrySet()) {
                boolean stop = stopEntry.getValue().isStopped();
                Script stopScript = stopEntry.getKey();
                if (loop && stop && !(loopScript.equals(stopScript)) && !stopScripts.contains(stopScript)) {
                    issueScripts.add(stopScript);
                }
            }
        }
        for (Script script : issueScripts) {
            addIssue(list.get(script));
        }
    }

    private void findAllIssues() {
        findIssues(motors, motorsStopNode);
        findIssues(matrix1, matrix1StopNode);
        findIssues(matrix2, matrix2StopNode);
        findIssues(matrix3, matrix3StopNode);
        findIssues(matrix4, matrix4StopNode);
        findIssues(rockyLight, rockyLightStopNode);
        findIssues(led, ledStopNode);
        findIssues(sound, soundStopNode);
    }

    private void resetMaps() {
        motors.clear();
        motorsStopNode.clear();
        matrix1.clear();
        matrix1StopNode.clear();
        matrix2.clear();
        matrix2StopNode.clear();
        matrix3.clear();
        matrix3StopNode.clear();
        matrix4.clear();
        matrix4StopNode.clear();
        rockyLight.clear();
        rockyLightStopNode.clear();
        led.clear();
        ledStopNode.clear();
        sound.clear();
        soundStopNode.clear();
        stopScripts.clear();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    enum LoopStopState {
        NONE, LOOPED, STOPPED, BOTH;

        public LoopStopState setStopped() {
            switch (this) {
                case NONE:
                case STOPPED:
                default:
                    return STOPPED;

                case LOOPED:
                case BOTH:
                    return BOTH;
            }
        }

        public LoopStopState setLooped() {
            switch (this) {
                case NONE:
                case LOOPED:
                default:
                    return LOOPED;

                case STOPPED:
                case BOTH:
                    return BOTH;
            }
        }

        public boolean isStopped() {
            switch (this) {
                case NONE:
                case LOOPED:
                default:
                    return false;

                case STOPPED:
                case BOTH:
                    return true;
            }
        }

        public boolean isLooped() {
            switch (this) {
                case NONE:
                case STOPPED:
                default:
                    return false;

                case LOOPED:
                case BOTH:
                    return true;
            }
        }
    }
}
