package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.parser.KeyParser;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.List;

public class KeySetPosition extends AbstractIssueFinder {
    public static final String NAME = "key_set_position";
    private boolean insideLoop = false;
    private boolean inCondition = false;
    private NumberLiteral lastKeyValue = null;

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        if (node.getEvent() instanceof GreenFlag || node.getEvent() instanceof StartedAsClone) {
            inCondition = false;
            lastKeyValue = null;
            super.visit(node);
        } else if (node.getEvent() instanceof KeyPressed && node.getStmtList().getStmts().size() > 0) {
            currentScript = node;
            Key keyNode = ((KeyPressed) node.getEvent()).getKey();
            NumberLiteral keyNum = (NumberLiteral) keyNode.getKey();
            int key = (int) keyNum.getValue();
            if (key == KeyParser.DOWNARROW || key == KeyParser.UPARROW || key == KeyParser.LEFTARROW || key == KeyParser.RIGHTARROW) {
                if (node.getStmtList().getStmts().size() == 1) {
                    checkStmt(node.getStmtList().getStmts().get(0), key);
                } else {
                    checkNumberStmts(node.getStmtList().getStmts(), key, 2); //used 2 as a placeholder
                }
            }
        }
    }

    private void checkNumberStmts(List<Stmt> stmts, int key, int numberOfStmts) {
        boolean foundSet;
        for (int i = 0; i < numberOfStmts && i < stmts.size(); i++) {
            foundSet = checkStmt(stmts.get(i), key);
            if (foundSet) {
                break;
            }
        }
    }

    private boolean checkStmt(Stmt stmt, int key) {
        if (key == KeyParser.DOWNARROW || key == KeyParser.UPARROW) {
            if (stmt instanceof SetYTo) {
                Hint hint = new Hint(NAME);
                hint.setParameter("XY", "y");
                if (key == KeyParser.DOWNARROW) {
                    hint.setParameter(Hint.HINT_KEY, IssueTranslator.getInstance().getInfo("down_arrow"));
                } else {
                    hint.setParameter(Hint.HINT_KEY, IssueTranslator.getInstance().getInfo("up_arrow"));
                }
                addIssue(stmt, stmt.getMetadata(), hint);
                return true;
            }
        } else if (key == KeyParser.LEFTARROW || key == KeyParser.RIGHTARROW) {
            if (stmt instanceof SetXTo) {
                Hint hint = new Hint(NAME);
                hint.setParameter("XY", "x");
                if (key == KeyParser.LEFTARROW) {
                    hint.setParameter(Hint.HINT_KEY, IssueTranslator.getInstance().getInfo("left_arrow"));
                } else {
                    hint.setParameter(Hint.HINT_KEY, IssueTranslator.getInstance().getInfo("right_arrow"));
                }
                addIssue(stmt, stmt.getMetadata(), hint);
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        //NOP should not look in Procedures
    }

    @Override
    public void visit(IfThenStmt node) {
        inCondition = true;
        node.getBoolExpr().accept(this);
        inCondition = false;
        if (lastKeyValue != null) {
            if (node.getThenStmts().getStmts().size() == 1) {
                checkStmt(node.getThenStmts().getStmts().get(0), (int) lastKeyValue.getValue());
            } else {
                checkNumberStmts(node.getThenStmts().getStmts(), (int) lastKeyValue.getValue(), 2); //used 2 as a placeholder
            }
        }
        lastKeyValue = null;
        node.getThenStmts().accept(this);
    }

    @Override
    public void visit(Key node) {
        if (inCondition) {
            if (node.getKey() instanceof NumberLiteral) {
                lastKeyValue = (NumberLiteral) node.getKey();
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public boolean isSubsumedBy(Issue first, Issue other) {
        if (first.getFinder() != this) {
            return super.isSubsumedBy(first, other);
        }

        if (other.getFinder() instanceof MissingLoopSensing) {
            ASTNode node = first.getCodeLocation().getParentNode().getParentNode();
            if(node instanceof IfThenStmt){
                return ((IfThenStmt) node).getBoolExpr() == (other.getCodeLocation());
            }

        }

        return false;
    }
}
