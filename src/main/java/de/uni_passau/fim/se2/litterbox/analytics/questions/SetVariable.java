package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;

/**
 * @QuestionType Strings
 * @NumAnswers 1
 * @Highlighted Statement
 * @Context Single script
 */
public class SetVariable extends AbstractQuestionFinder {

    private boolean inScript;

    @Override
    public void visit(Script node) {
        inScript = true;
        super.visit(node);
        inScript = false;
    }

    @Override
    public void visit(SetVariableTo node) {
        if (inScript) {
            Identifier identifier = node.getIdentifier();
            Expression expression = node.getExpr();

            if (expression instanceof NumberLiteral || expression instanceof StringLiteral) {
                IssueBuilder builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.HINT_VARIABLE, identifier.getScratchBlocks());
                hint.setParameter(Hint.ANSWER, getExpressionValue(expression));
                addIssue(builder.withHint(hint));
            }
        }
    }

    /**
     * Return expression value without open- and close-parentheses
     * @param node Expression node
     * @return expression value without parentheses
     */
    private String getExpressionValue(Expression node) {
        String value = node.getScratchBlocks();
        return value.substring(1, value.length() - 1);
    }

    @Override
    public String getName() {
        return "set_variable";
    }
}
