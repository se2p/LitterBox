package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.LinkedHashSet;
import java.util.Set;

public class ElementInLoopCondition extends AbstractIssueFinder {

    private boolean insideCondition;
    private Set<String> elementsInScript;
    private Set<String> elementsInCondition;

    @Override
    public Set<Issue> check(Program program) {
        elementsInScript = new LinkedHashSet<>();
        elementsInCondition = new LinkedHashSet<>();
        return super.check(program);
    }

    @Override
    public void visit(Script node) {
        elementsInScript.clear();
        elementsInCondition.clear();
        super.visit(node);

        if (elementsInCondition.size() > 0 && elementsInCondition.size() < elementsInScript.size()) {
            IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW).withMetadata(node.getMetadata());
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.CHOICES, elementsInScript.toString());
            hint.setParameter(Hint.ANSWER, elementsInCondition.toString());
            addIssue(builder.withCurrentNode(node).withHint(hint));
        }
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        insideCondition = true;
        NumExpr times = node.getTimes();
        if (times.hasChildren()) {
            visit(times);
        }
        else {
            visit((NumberLiteral) times);
        }
        insideCondition = false;
        visit(node.getStmtList());
    }

    @Override
    public void visit(UntilStmt node) {
        insideCondition = true;
        visit(node.getBoolExpr());
        insideCondition = false;
        visit(node.getStmtList());
    }

    @Override
    public void visit(Variable node) {
        elementsInScript.add("[var]" + node.getName().getName() + "[/var]");
        if (insideCondition) {
            elementsInCondition.add("[var]" + node.getName().getName() + "[/var]");
        }
    }

    @Override
    public void visit(NumberLiteral node) {
        elementsInScript.add("[lit]" + node.getScratchBlocks() + "[/lit]");
        if (insideCondition) {
            elementsInCondition.add("[lit]" + node.getScratchBlocks() + "[/lit]");
        }
    }

    @Override
    public void visit(StringLiteral node) {
        elementsInScript.add("[lit]" + node.getScratchBlocks() + "[/lit]");
        if (insideCondition) {
            elementsInCondition.add("[lit]" + node.getScratchBlocks() + "[/lit]");
        }
    }

    @Override
    public void visit(Qualified node) {
        // Don't add actor to lists
        if (node.getSecond() instanceof Variable) {
            visit((Variable) node.getSecond());
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.QUESTION;
    }

    @Override
    public String getName() {
        return "element_in_loop_condition";
    }
}
