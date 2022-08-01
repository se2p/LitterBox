package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.BoardLaunch;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.LaunchButton;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.SpeakerStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.TimedStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.MCORE;

public class TimedStatementInLiveLoop extends AbstractRobotFinder {

    private static final String NAME = "timed_statement_in_live_loop";
    private static final String POSSIBLE_HINT = "timed_statement_in_possible_live_loop";
    private boolean live = false;
    private boolean livePossible = false;   // if true, it may or may not be in live mode
    private boolean inLoop = false;
    private int stmtCount = 0;
    private TimedStmt issueNode = null;

    @Override
    public void visit(Program program) {
        putProceduresinScript = true;
        parseProcedureDefinitions = false;
        ignoreLooseBlocks = true;
        super.visit(program);
    }

    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        currentScript = node;
        proceduresInScript.put(node, new LinkedList<>());
        currentProcedure = null;
        if (robot.isRobot()) {
            Event event = node.getEvent();
            if (event instanceof BoardLaunch) {
                live = false;
            } else if (event instanceof GreenFlag || event instanceof KeyPressed) {
                live = true;
            } else if (robot == MCORE && (event instanceof LaunchButton || event instanceof ReceptionOfMessage)) {
                live = true;
            } else {
                live = true;
                livePossible = true;
            }
            visitChildren(node.getStmtList());
            live = false;
            livePossible = false;
            inLoop = false;
            issueNode = null;
            stmtCount = 0;
        }
    }

    @Override
    public void visit(LoopStmt node) {
        if (live) {
            int beforeCount = stmtCount;
            TimedStmt beforeNode = issueNode;
            boolean nestedLoop = inLoop;
            stmtCount = 0;
            issueNode = null;
            inLoop = true;

            visitChildren(node);

            if (stmtCount == 1 && issueNode != null) {
                if (livePossible) {
                    addIssue(issueNode, IssueSeverity.LOW, new Hint(POSSIBLE_HINT));
                } else {
                    addIssue(issueNode, IssueSeverity.MEDIUM, new Hint(NAME));
                }
            }

            stmtCount = stmtCount + beforeCount;
            issueNode = beforeNode;
            inLoop = nestedLoop;
        }
    }

    @Override
    public void visit(ControlStmt node) {
        if (live) {
            // prevents Stmt counter
            visitChildren(node);
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        if (live) {
            if (inLoop) {
                int beforeCount = stmtCount;
                TimedStmt issueNodeBefore = issueNode;

                node.getThenStmts().accept(this);
                int aCount = stmtCount - beforeCount;
                stmtCount = beforeCount;
                TimedStmt trueNode = issueNode;
                issueNode = issueNodeBefore;

                node.getElseStmts().accept(this);
                int bCount = stmtCount - beforeCount;
                if (issueNode == null || aCount < bCount) {
                    stmtCount = beforeCount + aCount;
                    issueNode = trueNode;
                } else {
                    stmtCount = beforeCount + bCount;
                }
            } else {
                visitChildren(node);
            }
        }
    }

    @Override
    public void visit(Stmt node) {
        if (live) {
            if (inLoop) {
                stmtCount++;
                if (node instanceof TimedStmt && issueNode == null && !(node instanceof SpeakerStmt || node instanceof WaitSeconds || node instanceof BroadcastAndWait)) {
                    issueNode = (TimedStmt) node;
                } else {
                    visitChildren(node);
                }
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(NAME);
        keys.add(POSSIBLE_HINT);
        return keys;
    }
}
