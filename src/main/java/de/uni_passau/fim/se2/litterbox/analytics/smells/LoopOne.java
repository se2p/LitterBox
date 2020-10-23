package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;

/**
 * This finder looks if a repeat times loop only does one repetition.
 */
public class LoopOne extends AbstractIssueFinder {
    public static final String NAME = "loop_one";

    @Override
    public void visit(RepeatTimesStmt node) {
        if (node.getTimes() instanceof NumberLiteral && ((NumberLiteral) node.getTimes()).getValue() == 1) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
