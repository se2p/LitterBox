package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
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
public class InitialisationOfPosition extends AbstractIssueFinder {
    public static final String NAME = "initialisation_of_position";
    private boolean initializedX = false;
    private boolean initializedY = false;
    private boolean inCustomBlock = false;
    private boolean inGreenFlag = false;
    private boolean initializedInBlock = false;
    private List<String> customBlocks = new ArrayList<>();

    @Override
    public void visit(ActorDefinition actor) {
        customBlocks = new ArrayList<>();
        initializedX = false;
        initializedY = false;
        initializedInBlock = false;
        super.visit(actor);
    }

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        if (node.getEvent() instanceof GreenFlag) {
            inGreenFlag = true;
            if (initializedInBlock) {
                node.getStmtList().getStmts().forEach(stmt -> {
                    if (stmt instanceof CallStmt) {

                        // Remove duplicates from custom block list
                        customBlocks = customBlocks.stream().distinct().collect(Collectors.toList());

                        if (customBlocks.contains(((CallStmt) stmt).getIdent().getName())) {

                            addIssue(stmt, stmt.getMetadata(), IssueSeverity.MEDIUM);
                            initializedInBlock = false;
                            customBlocks.remove(((CallStmt) stmt).getIdent().getName());
                        }
                    }
                });
            }
            this.currentScript = node;
            this.currentProcedure = null;
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
                if (!(stmt instanceof ControlStmt || stmt instanceof IfStmt)) {
                    stmt.accept(this);
                }
            });
        }
    }

    @Override
    public void visit(SetXTo stmt) {
        if (stmt.getNum() instanceof NumberLiteral) {
            initializedX = true;
            if (initializedX && initializedY) {
                check(stmt);
                initializedX = false;
                initializedY = false;
            }
        }
    }

    @Override
    public void visit(SetYTo stmt) {
        if (stmt.getNum() instanceof NumberLiteral) {
            initializedY = true;
            if (initializedX && initializedY) {
                check(stmt);
                initializedX = false;
                initializedY = false;
            }
        }
    }

    @Override
    public void visit(GoToPosXY stmt) {
        if (stmt.getX() instanceof NumberLiteral
                && stmt.getY() instanceof NumberLiteral) {
            check(stmt);
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
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
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