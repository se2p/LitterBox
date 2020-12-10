package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

public class InterruptedLoopSensing extends AbstractIssueFinder {
    private final String NAME = "interrupted_loop_sensing";
    private boolean inCondition = false;
    private boolean insideEquals = false;
    private boolean sensing = false;
    private boolean insideForever = false;
    private boolean insideControl = false;
    private String blockName;

    @Override
    public void visit(RepeatForeverStmt node) {
        insideForever = true;
        visitChildren(node);
        insideForever = false;
    }

    @Override
    public void visit(UntilStmt node) {
        inCondition = true;
        node.getBoolExpr().accept(this);
        inCondition = false;
        insideControl = true;
        blockName = IssueTranslator.getInstance().getInfo("until");
        node.getStmtList().accept(this);
        insideForever = false;
    }

    @Override
    public void visit(IfElseStmt node) {
        if (insideForever) {
            inCondition = true;
            node.getBoolExpr().accept(this);
            inCondition = false;
            insideControl = true;
            blockName = IssueTranslator.getInstance().getInfo("if") + " " + IssueTranslator.getInstance().getInfo("then") + " " + IssueTranslator.getInstance().getInfo("else");
            node.getStmtList().accept(this);
            node.getElseStmts().accept(this);
            insideControl = false;
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideForever) {
            inCondition = true;
            node.getBoolExpr().accept(this);
            inCondition = false;
            insideControl = true;
            blockName = IssueTranslator.getInstance().getInfo("if") + " " + IssueTranslator.getInstance().getInfo("then");
            node.getThenStmts().accept(this);
            insideControl = false;
        }
    }

    @Override
    public void visit(GlideSecsTo node) {
        if (insideControl && sensing) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.THEN_ELSE, blockName);
            addIssue(node, node.getMetadata(), hint);
        }
    }

    @Override
    public void visit(GlideSecsToXY node) {
        if (insideControl && sensing) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.THEN_ELSE, blockName);
            addIssue(node, node.getMetadata(), hint);
        }
    }

    @Override
    public void visit(IsKeyPressed node) {
        if (inCondition) {
            sensing = true;
        }
    }

    @Override
    public void visit(Touching node) {
        if (inCondition) {
            sensing = true;
        }
    }

    @Override
    public void visit(IsMouseDown node) {
        if (inCondition) {
            sensing = true;
        }
    }

    @Override
    public void visit(ColorTouchingColor node) {
        if (inCondition) {
            sensing = true;
        }
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        if (inCondition) {
            sensing = true;
        }
    }

    @Override
    public void visit(DistanceTo node) {
        if (inCondition) {
            sensing = true;
        }
    }

    @Override
    public void visit(Equals node) {
        if (inCondition) {
            insideEquals = true;
        }
        visitChildren(node);
        insideEquals = false;
    }

    @Override
    public void visit(Variable node) {
        if (insideEquals) {
            sensing = true;
        }
    }

    @Override
    public void visit(ItemOfVariable node) {
        if (insideEquals) {
            sensing = true;
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
}
