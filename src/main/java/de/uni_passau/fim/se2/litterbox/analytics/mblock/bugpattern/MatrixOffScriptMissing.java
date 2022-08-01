package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.RunningState;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDMatrix;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.MCorePort.PortType;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.MBlockStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.EmotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.*;
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
import static de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.MCorePort.PortType.*;

public class MatrixOffScriptMissing extends AbstractRobotFinder {

    private static final String NAME = "matrix_off_script_missing";
    private final Map<PortType, RunningState> states = new HashMap<>();
    private final Map<PortType, MBlockStmt> lastStmts = new HashMap<>();
    private final Map<PortType, List<MBlockStmt>> lastStmtMap = new HashMap<>();
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
        lastStmtMap.put(PORT_1, new LinkedList<>());
        lastStmtMap.put(PORT_2, new LinkedList<>());
        lastStmtMap.put(PORT_3, new LinkedList<>());
        lastStmtMap.put(PORT_4, new LinkedList<>());
        for (Script script : node.getScriptList()) {
            script.accept(this);

            for (Entry<PortType, RunningState> entry : states.entrySet()) {
                PortType port = entry.getKey();
                if (entry.getValue() == STOPPED) {
                    lastStmtMap.put(port, null);
                } else if (entry.getValue() == RUNNING) {
                    List<MBlockStmt> lastStmtList = lastStmtMap.get(port);
                    if (lastStmtList != null) {
                        lastStmtList.add(lastStmts.get(port));
                    }
                }
            }
            resetMaps();
        }
        for (Entry<PortType, List<MBlockStmt>> entry : lastStmtMap.entrySet()) {
            List<MBlockStmt> list = entry.getValue();
            if (list != null) {
                for (MBlockStmt stmt : list) {
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
    public void visit(EmotionStmt node) {
        states.put(PORT_1, RUNNING);
        lastStmts.put(PORT_1, node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        forever = true;
        visit((LoopStmt) node);
    }

    @Override
    public void visit(LEDMatrixStmt node) {
        boolean blank = isBlank(node);
        if (blank || node instanceof TimedStmt || node instanceof TurnOffFace || node instanceof TurnOffFacePort) {
            setStopped(node);
        } else {
            setRunning(node);
        }
    }

    private void setStopped(LEDMatrixStmt node) {
        if (node instanceof PortStmt) {
            states.put(((PortStmt) node).getPort().getPortType(), STOPPED);
        } else {
            states.put(PORT_1, STOPPED);
        }
    }

    private void setRunning(LEDMatrixStmt node) {
        if (node instanceof PortStmt) {
            states.put(((PortStmt) node).getPort().getPortType(), RUNNING);
            lastStmts.put(((PortStmt) node).getPort().getPortType(), node);
        } else {
            states.put(PORT_1, RUNNING);
            lastStmts.put(PORT_1, node);
        }
    }

    private void resetMaps() {
        states.clear();
        lastStmts.clear();
        forever = false;
    }

    private boolean isBlank(LEDMatrixStmt node) {
        if (node instanceof FacePanelStmt) {
            LEDMatrix matrix = ((FacePanelStmt) node).getLedMatrix();
            return matrix.getFaceString().equals("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
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
