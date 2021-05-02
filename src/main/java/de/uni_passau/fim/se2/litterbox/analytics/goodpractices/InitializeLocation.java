package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This checks for an initialization for the sprite location. This initialization should usually happen in a
 * GreenFlag script or a CustomBlock. As initial position for the sprite we set X = 0, Y = 0.
 */
public class InitializeLocation extends AbstractIssueFinder {
    public static final String NAME = "initialize_location";
    private boolean initializedX = false;
    private boolean initializedY = false;
    private boolean inCustomBlock = false;
    private boolean inGreenFlag = false;
    private final int INIT_STATE = 0;
    private boolean initializedInBlock = false;
    private List<String> customBlocks = new ArrayList<>();



    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof GreenFlag) {
            inGreenFlag = true;
            if (initializedInBlock) {
                node.getStmtList().getStmts().forEach(stmt -> {
                    if (stmt instanceof CallStmt) {

                        // Remove duplicates from custom block list
                        customBlocks = customBlocks.stream().distinct().collect(Collectors.toList());

                        if (customBlocks.contains(((CallStmt) stmt).getIdent().getName())) {

                            System.out.println(((CallStmt) stmt).getIdent().getName());

                            addIssue(stmt, stmt.getMetadata(), IssueSeverity.MEDIUM);
                            initializedInBlock = false;
                            customBlocks.remove(((CallStmt) stmt).getIdent().getName());
                        }
                    }
                });

            }

            node.getStmtList().accept(this);
            inGreenFlag = false;
            visitChildren(node);
        }
    }


    @Override
    public void visit(ProcedureDefinition node) {
        inCustomBlock = true;
        this.currentProcedure = node;
        this.currentScript = null;
        node.getStmtList().accept(this);
        inCustomBlock = false;
        visitChildren(node);
    }


    @Override
    public void visit(StmtList node) {
        if (inCustomBlock) {
            if (node.getParentNode() instanceof ProcedureDefinition) {
                ProcedureDefinition parent = (ProcedureDefinition) node.getParentNode();

                for (Stmt stmt : node.getStmts()) {
                    if (stmt instanceof GoToPosXY || stmt instanceof SetXTo || stmt instanceof SetYTo) {
                        customBlocks.add(procMap.get(parent.getIdent()).getName());
                        stmt.accept(this);
                    }
                }

            }
        } else {

            // Initialization should not be in a control- or if- statement
            node.getStmts().forEach(stmt -> {
                if (stmt instanceof ControlStmt || stmt instanceof IfStmt) {
                } else {
                    stmt.accept(this);
                }
            });
        }
    }

    @Override
    public void visit(SetXTo stmt) {
        if (stmt.getNum() instanceof NumberLiteral) {
            if ((((NumberLiteral) stmt.getNum()).getValue() == INIT_STATE)) {
                initializedX = true;
                if (initializedX && initializedY) {
                    check(stmt);
                    initializedX = false;
                    initializedY = false;
                }
            }
        }
    }

    @Override
    public void visit(SetYTo stmt) {
        if (stmt.getNum() instanceof NumberLiteral) {
            if ((((NumberLiteral) stmt.getNum()).getValue() == INIT_STATE)) {
                initializedY = true;
                if (initializedX && initializedY) {
                    check(stmt);
                    initializedX = false;
                    initializedY = false;
                }
            }
        }
    }

    @Override
    public void visit(GoToPosXY stmt) {
        if (stmt.getX() instanceof NumberLiteral
                && stmt.getY() instanceof NumberLiteral) {
            if ((((NumberLiteral) stmt.getX()).getValue() == INIT_STATE &&
                    ((NumberLiteral) stmt.getX()).getValue() == INIT_STATE)) {
                check(stmt);
            }
        }
    }

    private void check(AbstractNode node) {
        if (inGreenFlag) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        } else if (inCustomBlock) {
            initializedInBlock = true;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.GOOD_PRACTICE;
    }
}
