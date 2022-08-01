package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.bool.MBlockBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.MBlockStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.IRStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.LongStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.AddTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.DeleteOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.InsertAt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.ReplaceItem;

import java.util.LinkedList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity.MEDIUM;

public class DetectRepeatInLoop extends AbstractRobotFinder {

    private static final String NAME = "detect_repeat_in_loop";
    private boolean inLoop;
    private boolean wait;
    private boolean issueState;
    private MBlockBoolExpr currentIssueExpr;
    private List<MBlockBoolExpr> issueExpr = new LinkedList<>();

    @Override
    public void visit(Program program) {
        putProceduresinScript = true;
        parseProcedureDefinitions = false;
        ignoreLooseBlocks = true;
        super.visit(program);
    }

    @Override
    public void visit(Script script) {
        ignoreLooseBlocks = true;
        proceduresInScript.put(script, new LinkedList<>());
        super.visit(script);
    }

    @Override
    public void visit(LoopStmt node) {
        boolean nestedLoop = inLoop;
        inLoop = true;
        boolean waitBefore = wait;
        wait = false;
        List<MBlockBoolExpr> issuesBefore = issueExpr;
        issueExpr = new LinkedList<>();

        visitChildren(node);

        if (!wait) {
            for (MBlockBoolExpr expr : issueExpr) {
                addIssue(expr, MEDIUM);
            }
        }

        issueExpr = issuesBefore;
        wait = wait || waitBefore;
        inLoop = nestedLoop;
    }

    @Override
    public void visit(IfStmt node) {
        currentIssueExpr = null;
        node.getBoolExpr().accept(this);
        MBlockBoolExpr localIssueExpr = currentIssueExpr;
        currentIssueExpr = null;
        boolean issueStateBefore = issueState;
        issueState = false;

        node.getThenStmts().accept(this);
        if (node instanceof IfElseStmt) {
            ((IfElseStmt) node).getElseStmts().accept(this);
        }

        if (inLoop && issueState && localIssueExpr != null) {
            issueExpr.add(localIssueExpr);
        }
        issueState = issueState || issueStateBefore;
    }

    @Override
    public void visit(ChangeVariableBy node) {
        if (inLoop) {
            issueState = true;
        }
    }

    @Override
    public void visit(IRStmt node) {
        if (inLoop) {
            issueState = true;
        }
    }

    @Override
    public void visit(AddTo node) {
        if (inLoop) {
            issueState = true;
        }
    }

    @Override
    public void visit(DeleteOf node) {
        if (inLoop) {
            issueState = true;
        }
    }

    @Override
    public void visit(InsertAt node) {
        if (inLoop) {
            issueState = true;
        }
    }

    @Override
    public void visit(ReplaceItem node) {
        if (inLoop) {
            issueState = true;
        }
    }

    @Override
    public void visit(Broadcast node) {
        if (inLoop) {
            issueState = true;
        }
    }

    @Override
    public void visit(WaitSeconds node) {
        if (inLoop) {
            wait = true;
        }
    }

    @Override
    public void visit(WaitUntil node) {
        if (inLoop) {
            wait = true;
        }
    }

    @Override
    public void visit(MBlockStmt node) {
        if (inLoop && node instanceof LongStmt) {
            wait = true;
        }
    }

    @Override
    public void visit(MBlockBoolExpr node) {
        if (inLoop) {
            currentIssueExpr = node;
        }
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
