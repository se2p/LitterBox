package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A script should execute actions when an event occurs. To ensure correct timing for the action, a continuous check for
 * the event to occur is necessary. So the check must be inside a forever or until loop (and not only in a conditional
 * construct). This is the solution pattern for the bug pattern "Missing Loop Sensing".
 */
public class LoopSensing extends AbstractIssueFinder {

    public static final String NAME = "loop_sensing";
    private boolean insideGreenFlagClone = false;
    private boolean insideLoop = false;
    private boolean inCondition = false;
    private List<ASTNode> loops;

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        if (node.getEvent() instanceof GreenFlag || node.getEvent() instanceof StartedAsClone) {
            insideGreenFlagClone = true;
        }
        loops = new ArrayList<>();
        insideLoop = false;
        inCondition = false;
        super.visit(node);
        insideGreenFlagClone = false;
    }


    @Override
    public void visit(ProcedureDefinition node) {
        loops = new ArrayList<>();
        insideLoop = false;
        inCondition = false;
        insideGreenFlagClone = false;
        super.visit(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        insideLoop = true;
        loops.add(node);
        visitChildren(node);
        loops.remove(node);
        insideLoop = false;
    }

    @Override
    public void visit(UntilStmt node) {
        insideLoop = true;
        loops.add(node);
        visitChildren(node);
        loops.remove(node);
        insideLoop = false;
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideGreenFlagClone && insideLoop) {
            inCondition = true;
            BoolExpr boolExpr = node.getBoolExpr();
            boolExpr.accept(this);
            inCondition = false;
        }
        node.getThenStmts().accept(this);
    }

    @Override
    public void visit(IsKeyPressed node) {
        if (insideGreenFlagClone && insideLoop && inCondition) {
            generateMultiBlockIssue(node);
        }
    }

    @Override
    public void visit(Touching node) {
        if (insideGreenFlagClone && insideLoop && inCondition) {
            generateMultiBlockIssue(node);
        }
    }

    @Override
    public void visit(IsMouseDown node) {
        if (insideGreenFlagClone && insideLoop && inCondition) {
            generateMultiBlockIssue(node);
        }
    }

    @Override
    public void visit(ColorTouchingColor node) {
        if (insideGreenFlagClone && insideLoop && inCondition) {
            generateMultiBlockIssue(node);
        }
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        if (insideGreenFlagClone && insideLoop && inCondition) {
            generateMultiBlockIssue(node);
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        if (insideGreenFlagClone && insideLoop) {
            inCondition = true;
            BoolExpr boolExpr = node.getBoolExpr();
            boolExpr.accept(this);
            inCondition = false;
        }
        node.getStmtList().accept(this);
        node.getElseStmts().accept(this);
    }

    public void generateMultiBlockIssue(ASTNode node) {
        ASTNode loop = loops.get(loops.size() - 1);
        ASTNode parent = node.getParentNode();
        while (!(parent instanceof IfStmt)) {
            parent = parent.getParentNode();
        }
        List<ASTNode> concernedNodes = new ArrayList<>();
        concernedNodes.add(loop);
        concernedNodes.add(parent);
        concernedNodes.add(node);
        Hint hint = new Hint(NAME);
        MultiBlockIssue issue;
        if (currentScript != null) {
            issue = new MultiBlockIssue(this, IssueSeverity.HIGH, program, currentActor, currentScript, concernedNodes, node.getMetadata(), hint);
        } else {
            issue = new MultiBlockIssue(this, IssueSeverity.HIGH, program, currentActor, currentProcedure, concernedNodes, node.getMetadata(), hint);
        }
        addIssue(issue);
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
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
