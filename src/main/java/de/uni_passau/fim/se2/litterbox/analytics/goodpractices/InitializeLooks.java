package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ClearGraphicEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Hide;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Show;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;

import java.util.ArrayList;
import java.util.List;

public class InitializeLooks extends AbstractIssueFinder {

    public static final String NAME = "initialize_looks";
    private boolean inGreenFlag = false;
    private boolean inCustomBlock = false;
    private List<String> customBlocks = new ArrayList<>();
    private boolean initializedInBlock = false;

    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof GreenFlag) {
            inGreenFlag = true;
            if (initializedInBlock) {
                node.getStmtList().getStmts().forEach(stmt -> {
                    if (stmt instanceof CallStmt) {
                        if (customBlocks.contains(((CallStmt) stmt).getIdent().getName())) {
                            addIssue(stmt, stmt.getMetadata(), IssueSeverity.MEDIUM);
                        }
                    }
                });
                initializedInBlock = false;
                customBlocks = new ArrayList<>();
            }
            node.getStmtList().accept(this);
            inGreenFlag = false;
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        inCustomBlock = true;
        node.getStmtList().accept(this);
        inCustomBlock = false;
    }

    @Override
    public void visit(StmtList node) {
        if (inCustomBlock) {
            if (node.getParentNode() instanceof ProcedureDefinition) {
                ProcedureDefinition parent = (ProcedureDefinition) node.getParentNode();

                for (Stmt stmt : node.getStmts()) {
                    if (stmt instanceof SetSizeTo || stmt instanceof SwitchCostumeTo || stmt instanceof Show ||
                            stmt instanceof Hide || stmt instanceof ClearGraphicEffects
                            || stmt instanceof SetGraphicEffectTo || stmt instanceof SwitchBackdrop) {
                        customBlocks.add(procMap.get(parent.getIdent()).getName());
                        System.out.println(customBlocks);
                        stmt.accept(this);
                        break;
                    }
                }

            }
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        check(node);
    }

    @Override
    public void visit(SetSizeTo node) {
        check(node);
    }

    @Override
    public void visit(Show node) {
        check(node);
    }

    @Override
    public void visit(Hide node) {
        check(node);
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        check(node);
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        check(node);
    }

    @Override
    public void visit(SwitchBackdrop node) {
        check(node);
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
