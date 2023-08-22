package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.*;

public class ElementInLoopBody extends AbstractIssueFinder {

    private boolean insideLoop;
    private Set<String> elementsInScript;
    private Set<String> elementsInLoop;

    @Override
    public Set<Issue> check(Program program) {
        elementsInScript = new LinkedHashSet<>();
        elementsInLoop = new LinkedHashSet<>();
        return super.check(program);
    }

    @Override
    public void visit(Script node) {
        elementsInScript.clear();
        elementsInLoop.clear();
        super.visit(node);

        if (elementsInLoop.size() > 0 && elementsInLoop.size() < elementsInScript.size()) {
            IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW).withMetadata(node.getMetadata());
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.CHOICES, elementsInScript.toString());
            hint.setParameter(Hint.ANSWER, elementsInLoop.toString());
            addIssue(builder.withCurrentNode(node).withHint(hint));
        }
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        insideLoop = true;
        visit(node.getStmtList());
        insideLoop = false;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        NumExpr times = node.getTimes();
        if (times.hasChildren()) {
            visit(times);
        }
        else {
            visit((NumberLiteral) times);
        }
        insideLoop = true;
        visit(node.getStmtList());
        insideLoop = false;
    }

    @Override
    public void visit(UntilStmt node) {
        visit(node.getBoolExpr());
        insideLoop = true;
        visit(node.getStmtList());
        insideLoop = false;
    }

    @Override
    public void visit(Variable node) {
        elementsInScript.add(node.getName().getName());
        if (insideLoop) {
            elementsInLoop.add(node.getName().getName());
        }
    }

    @Override
    public void visit(BoolLiteral node) {
        elementsInScript.add(Arrays.toString(node.toSimpleStringArray()));
        if (insideLoop) {
            elementsInLoop.add(Arrays.toString(node.toSimpleStringArray()));
        }
    }

    @Override
    public void visit(NumberLiteral node) {
        elementsInScript.add(String.valueOf(node.getValue()));
        if (insideLoop) {
            elementsInLoop.add(String.valueOf(node.getValue()));
        }
    }

    @Override
    public void visit(StringLiteral node) {
        elementsInScript.add(node.getText());
        if (insideLoop) {
            elementsInLoop.add(node.getText());
        }
    }

    @Override
    public void visit(Qualified node) {
        // Don't add actor to lists
        visit(node.getSecond());
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.QUESTION;
    }

    @Override
    public String getName() {
        return "element_in_loop_body";
    }
}
