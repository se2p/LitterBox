package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ClearGraphicEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Hide;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Show;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InitializeLooks extends AbstractIssueFinder {

    public static final String NAME = "initialize_looks";
    public static final String HINT_STAGE = "initialize_looks_stage";
    public static final String HINT_SPRITE = "initialize_looks_sprite";
    private boolean inGreenFlag = false;
    private boolean inCustomBlock = false;
    private List<String> customBlocks = new ArrayList<>();
    private boolean initializedInBlock = false;

    @Override
    public void visit(ActorDefinition actor) {
        initializedInBlock = false;
        customBlocks = new ArrayList<>();
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
                        if (customBlocks.contains(((CallStmt) stmt).getIdent().getName())) {
                            Hint hint;
                            if (currentActor.isStage()){
                                hint = new Hint(HINT_STAGE);
                            }else{
                                hint = new Hint(HINT_SPRITE);
                            }
                            addIssue(stmt, stmt.getMetadata(), IssueSeverity.MEDIUM, hint);
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
                    if (stmt instanceof SetSizeTo || stmt instanceof SwitchCostumeTo || stmt instanceof Show ||
                            stmt instanceof Hide || stmt instanceof ClearGraphicEffects
                            || stmt instanceof SetGraphicEffectTo || stmt instanceof SwitchBackdrop) {
                        customBlocks.add(procMap.get(parent.getIdent()).getName());
                        stmt.accept(this);
                        break;
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
            Hint hint;
            if (currentActor.isStage() || node instanceof SwitchBackdrop) {
                hint = new Hint(HINT_STAGE);
            } else {
                hint = new Hint(HINT_SPRITE);
            }
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM, hint);
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

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(HINT_STAGE);
        keys.add(HINT_SPRITE);
        return keys;
    }
}
