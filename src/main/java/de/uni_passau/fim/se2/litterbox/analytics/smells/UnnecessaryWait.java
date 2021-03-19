package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;

/**
 * This finder looks if a wait block waits for 0 seconds and thus is unnecessary.
 */
public class UnnecessaryWait extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_wait";

    @Override
    public void visit(WaitSeconds node) {
        if (node.getSeconds() instanceof NumberLiteral) {
            NumberLiteral num = (NumberLiteral) node.getSeconds();
            if (num.getValue() == 0) {
                addIssue(node, node.getMetadata());
            }
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
