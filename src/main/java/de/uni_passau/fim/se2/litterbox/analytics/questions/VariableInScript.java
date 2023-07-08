package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.LinkedHashSet;
import java.util.Set;

public class VariableInScript extends AbstractIssueFinder {
    private Set<String> variablesInScript = new LinkedHashSet<>();

    @Override
    public void visit(Script node) {
        variablesInScript.clear();
        super.visit(node);

        if (variablesInScript.size() > 0) {
            IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW).withMetadata(node.getMetadata());
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.HINT_VARIABLE, variablesInScript.toString());
            addIssue(builder.withCurrentNode(node).withHint(hint));
        }
    }

    @Override
    public void visit(Variable node) {
        variablesInScript.add(node.getName().getName());
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.QUESTION;
    }

    @Override
    public String getName() {
        return "variable_in_script";
    }
}
