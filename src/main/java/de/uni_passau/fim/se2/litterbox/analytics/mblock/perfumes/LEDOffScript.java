package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.NumValueVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.*;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;

public class LEDOffScript extends AbstractRobotFinder {
    private static final String NAME = "led_off_script";
    private boolean turnsOnLED;
    private boolean turnsOffLED;
    private boolean secondRun;

    @Override
    public void visit(ActorDefinition node) {
        turnsOnLED = false;

        secondRun = false;
        super.visit(node);
        secondRun = true;
        super.visit(node);
    }

    @Override
    public void visit(Script node) {
        turnsOffLED = false;
        super.visit(node);
        if (secondRun && !node.getStmtList().getStmts().isEmpty()) {
            ASTNode last = node.getStmtList().getStmts().get(node.getStmtList().getStmts().size() - 1);
            if (last instanceof StopAll || last instanceof StopOtherScriptsInSprite) {
                if (!(node.getEvent() instanceof Never)
                        && turnsOnLED && turnsOffLED) {
                    addIssue(node.getEvent(), IssueSeverity.MEDIUM);
                }
            }
        }
    }

    @Override
    public void visit(LEDColorShow node) {
        if (!isBlack(node)) {
            turnsOnLED = true;
        }
        if (secondRun && isBlack(node)) {
            turnsOffLED = true;
        }
    }

    @Override
    public void visit(LEDColorShowPosition node) {
        if (!isBlack(node)) {
            turnsOnLED = true;
        }
        if (secondRun && isBlack(node)) {
            turnsOffLED = true;
        }
    }

    @Override
    public void visit(LEDColorTimed node) {
        if (!isBlack(node)) {
            turnsOnLED = true;
        }
        if (secondRun && isBlack(node)) {
            turnsOffLED = true;
        }
    }

    @Override
    public void visit(LEDColorTimedPosition node) {
        if (!isBlack(node)) {
            turnsOnLED = true;
        }
        if (secondRun && isBlack(node)) {
            turnsOffLED = true;
        }
    }

    @Override
    public void visit(RGBValue node) {
        if (!isBlack(node)) {
            turnsOnLED = true;
        }
        if (secondRun && isBlack(node)) {
            turnsOffLED = true;
        }
    }

    @Override
    public void visit(RGBValuesPosition node) {
        if (!isBlack(node)) {
            turnsOnLED = true;
        }
        if (secondRun && isBlack(node)) {
            turnsOffLED = true;
        }
    }

    @Override
    public void visit(LEDOff node) {
        if (secondRun) {
            turnsOffLED = true;
        }
    }

    private boolean isBlack(LEDStmt node) {
        if (node instanceof ColorStmt) {
            StringExpr stringExpr = ((ColorStmt) node).getColorString();
            if (stringExpr instanceof StringLiteral) {
                String string = ((StringLiteral) stringExpr).getText();
                return string.equals("#000000");
            }
        } else if (node instanceof RGBValuesPosition) {
            NumValueVisitor calc = new NumValueVisitor();
            try {
                double red = calc.calculateEndValue(((RGBValuesPosition) node).getRed());
                double green = calc.calculateEndValue(((RGBValuesPosition) node).getGreen());
                double blue = calc.calculateEndValue(((RGBValuesPosition) node).getBlue());
                return red == 0 && green == 0 && blue == 0;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
