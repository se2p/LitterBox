package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.DetectRGBValue;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;

public class ColorDetectionInBounds extends AbstractRobotFinder {
    private static final String NAME = "color_detection_in_bounds";
    private static final int COLOR_MAX = 255;
    private static final int COLOR_MIN = 0;
    private boolean insideComparison;
    private boolean hasColor;
    private double sensorValue;
    private boolean setValue;
    private boolean firstHasColor;
    private boolean secondHasColor;
    private boolean visitFirst;
    private boolean visitSecond;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(BiggerThan node) {
        visitComp(node);
        if (setValue && hasColor) {
            if (((firstHasColor && sensorValue >= COLOR_MIN && sensorValue < COLOR_MAX)
                    || (secondHasColor && sensorValue <= COLOR_MAX && sensorValue > COLOR_MIN)) && (sensorValue % 1) == 0) {
                addIssue(node, IssueSeverity.LOW);
            }
        }
        insideComparison = false;
        hasColor = false;
        firstHasColor = false;
        secondHasColor = false;
    }

    @Override
    public void visit(LessThan node) {
        visitComp(node);
        if (setValue && hasColor) {
            if (((firstHasColor && sensorValue <= COLOR_MAX && sensorValue > COLOR_MIN)
                    || (secondHasColor && sensorValue >= COLOR_MIN && sensorValue < COLOR_MAX)) && (sensorValue % 1) == 0) {
                addIssue(node, IssueSeverity.LOW);
            }
        }
        insideComparison = false;
        hasColor = false;
        firstHasColor = false;
        secondHasColor = false;
    }

    private void visitComp(BinaryExpression node) {
        insideComparison = true;
        setValue = false;
        visitFirst = true;
        node.getOperand1().accept(this);
        visitFirst = false;
        visitSecond = true;
        node.getOperand2().accept(this);
        visitSecond = false;
    }

    @Override
    public void visit(DetectRGBValue node) {
        if (insideComparison) {
            hasColor = true;
        }
        if (visitFirst) {
            firstHasColor = true;
        } else if (visitSecond) {
            secondHasColor = true;
        }
    }

    @Override
    public void visit(NumberLiteral node) {
        if (insideComparison && !setValue) {
            setValue = true;
            sensorValue = node.getValue();
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
