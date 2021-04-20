package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ActorLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ActorSoundStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import java.util.ArrayList;

public class SeparateScriptBySemantics implements Refactoring, ScratchVisitor {

    private Stmt lastStmt;
    private Script refactoredScript;
    private final StmtList stmtList;
    private final Script script;
    private final ActorDefinition sprite;
    private final Event event;
    private static final String NAME = "separate_script_by_semantics";

    public SeparateScriptBySemantics(Script script) {
        this.script = script;
        this.sprite = (ActorDefinition) script.getParentNode();
        this.event = script.getEvent();
        this.lastStmt = (Stmt) event;
        this.stmtList = new StmtList(new ArrayList<>());
    }

    @Override
    public Program apply(Program program) {
        Program refactored = program.deepCopy();
        visit(script);
        if (refactoredScript != null) {
           removeScriptFromSprite();
        }
        return refactored;
    }

    @Override
    public void visit(Stmt stmt) {
        addStmt(stmt);
    }

    @Override
    public void visit(ActorLookStmt actorLookStmt) {
        addStmt(actorLookStmt);
        if (!(lastStmt instanceof ActorLookStmt)) {
            addRefactoredScript();
        }
        lastStmt = actorLookStmt;
    }

    @Override
    public void visit(ActorSoundStmt actorSoundStmt) {
        addStmt(actorSoundStmt);
        if (!(lastStmt instanceof ActorSoundStmt)) {
            addRefactoredScript();
        }
        lastStmt = actorSoundStmt;
    }

    @Override
    public void visit(PenStmt penStmt) {
        addStmt(penStmt);
        if (!(lastStmt instanceof PenStmt)) {
            addRefactoredScript();
        }
        lastStmt = penStmt;
    }

    @Override
    public void visit(SpriteLookStmt spriteLookStmt) {
        addStmt(spriteLookStmt);
        if (!(lastStmt instanceof SpriteLookStmt)) {
            addRefactoredScript();
        }
        lastStmt = spriteLookStmt;
    }

    @Override
    public void visit(SpriteMotionStmt spriteMotionStmt) {
        addStmt(spriteMotionStmt);
        if (!(lastStmt instanceof SpriteMotionStmt)) {
            addRefactoredScript();
        }
        lastStmt = spriteMotionStmt;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + "(" + script.getUniqueName() + ")";
    }

    private void addRefactoredScript() {
        refactoredScript = new Script(event, stmtList);
        sprite.getScripts().getScriptList().add(refactoredScript);
        stmtList.getStmts().clear();
    }

    private void addStmt(Stmt stmt) {
        stmtList.getStmts().add(stmt);
    }

    private void removeScriptFromSprite() {
        sprite.getScripts().getScriptList().remove(script);
    }
}
