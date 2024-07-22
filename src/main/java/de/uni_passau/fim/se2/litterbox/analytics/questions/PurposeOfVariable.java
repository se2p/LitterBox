package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.HashSet;
import java.util.Set;

public class PurposeOfVariable extends AbstractQuestionFinder {


    private boolean inScript;
    private Set<String> variables;

    @Override
    public void visit(Program node) {
        variables = new HashSet<>();
        super.visit(node);
        currentScript = null;

        for (String variable: variables) {
            IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW);
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.HINT_VARIABLE, variable);
            addIssue(builder.withHint(hint));
        }
    }

    @Override
    public void visit(Script node) {
        inScript = true;
        super.visit(node);
        inScript = false;
    }

    @Override
    public void visit(Variable node) {
        if (inScript)
            variables.add(node.getScratchBlocks());
    }

    @Override
    public String getName() {
        return "purpose_of_variable";
    }
}
