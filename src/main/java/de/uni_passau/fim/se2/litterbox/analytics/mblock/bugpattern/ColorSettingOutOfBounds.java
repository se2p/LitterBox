package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.RGBValue;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.RGBValuesPosition;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;

public class ColorSettingOutOfBounds extends AbstractRobotFinder {
    private static final String NAME = "color_setting_out_of_bounds";
    private static final int COLOR_MAX = 255;
    private static final int COLOR_MIN = 0;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(RGBValue node) {
        if (colorOutOfBounds(node.getValue())) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(RGBValuesPosition node) {
        if (colorOutOfBounds(node.getBlue()) || colorOutOfBounds(node.getGreen()) || colorOutOfBounds(node.getRed())) {
            addIssue(node, node.getMetadata());
        }
    }

    private boolean colorOutOfBounds(NumExpr node) {
        if (node instanceof NumberLiteral) {
            double value = ((NumberLiteral) node).getValue();
            return value < COLOR_MIN || value > COLOR_MAX;
        }
        return false;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
