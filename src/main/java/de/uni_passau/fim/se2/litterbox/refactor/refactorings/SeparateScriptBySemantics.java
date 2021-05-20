package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ActorLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ActorSoundStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class SeparateScriptBySemantics implements Refactoring, ScratchVisitor {

    private Stmt lastStmt;
    private Script refactoredScript;
    private final List<Stmt> stmtList;
    private final Script script;
    private final ScriptList scriptList;
    private final Event event;
    private final List<Script> refactoredScriptList = new ArrayList<>();

    private static final String NAME = "separate_script_by_semantics";

    public SeparateScriptBySemantics(Script script) {
        this.script = Preconditions.checkNotNull(script);
        this.lastStmt = null;
        this.stmtList = new ArrayList<>();
        this.scriptList = (ScriptList) script.getParentNode();
        this.event = script.getEvent();
    }

    @Override
    public Program apply(Program program) {
        visit(script);
        if (refactoredScript != null && !stmtList.isEmpty()) {
            refactoredScript.getStmtList().getStmts().addAll(stmtList);
        }
        if (refactoredScript != null) {
            removeScriptFromSprite();
        }
        return program.deepCopy();
    }


    @Override
    public void visit(Stmt stmt) {
        stmtList.add(stmt);
        lastStmt = stmt;
    }

    @Override
    public void visit(ActorLookStmt actorLookStmt) {
        stmtList.add(actorLookStmt);
        if (!(lastStmt instanceof ActorLookStmt)) {
            addRefactoredScript();
        }
        lastStmt = actorLookStmt;
    }

    @Override
    public void visit(ActorSoundStmt actorSoundStmt) {
        stmtList.add(actorSoundStmt);
        if (!(lastStmt instanceof ActorSoundStmt)) {
            addRefactoredScript();
        }
        lastStmt = actorSoundStmt;
    }

    @Override
    public void visit(PenStmt penStmt) {
        stmtList.add(penStmt);
        if (!(lastStmt instanceof PenStmt)) {
            addRefactoredScript();
        }
        lastStmt = penStmt;
    }

    @Override
    public void visit(SpriteLookStmt spriteLookStmt) {
        stmtList.add(spriteLookStmt);
        if (!(lastStmt instanceof SpriteLookStmt)) {
            addRefactoredScript();
        }
        lastStmt = spriteLookStmt;
    }

    @Override
    public void visit(SpriteMotionStmt spriteMotionStmt) {
        stmtList.add(spriteMotionStmt);
        if (!(lastStmt instanceof SpriteMotionStmt)) {
            addRefactoredScript();
        }
        lastStmt = spriteMotionStmt;
    }

    @Override
    public void visit(RepeatTimesStmt repeatTimesStmt) {
        stmtList.add(repeatTimesStmt);
        lastStmt = repeatTimesStmt;
        visit(repeatTimesStmt.getStmtList());
    }

    @Override
    public void visit(UntilStmt untilStmt) {
        stmtList.add(untilStmt);
        lastStmt = untilStmt;
        visit(untilStmt.getStmtList());
    }

    @Override
    public void visit(RepeatForeverStmt repeatForeverStmt) {
        stmtList.add(repeatForeverStmt);
        lastStmt = repeatForeverStmt;
        visit(repeatForeverStmt.getStmtList());
    }

    @Override
    public void visit(IfThenStmt ifThenStmt) {
        stmtList.add(ifThenStmt);
        lastStmt = ifThenStmt;
        visit(ifThenStmt.getThenStmts());
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        stmtList.add(ifElseStmt);
        lastStmt = ifElseStmt;
        visit(ifElseStmt.getThenStmts());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        String originalScriptScratchBlocks = script.getScratchBlocks();
        StringBuilder result = new StringBuilder(NAME + " on script:\n" + originalScriptScratchBlocks + "\n\nRefactored scripts:\n");
        for (Script refactored : refactoredScriptList) {
            result.append(refactored.getScratchBlocks()).append("\n");
        }
        return result.toString();
    }


    private void addRefactoredScript() {
        List<Stmt> refactoredList = new ArrayList<>(stmtList);
        refactoredScript = new Script(event, new StmtList(refactoredList));
        refactoredScriptList.add(refactoredScript);
        scriptList.getScriptList().add(refactoredScript);
        stmtList.clear();
    }

    private void removeScriptFromSprite() {
        scriptList.getScriptList().remove(script);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SeparateScriptBySemantics)) {
            return false;
        }
        return script.equals(((SeparateScriptBySemantics) other).script);
    }

    @Override
    public int hashCode() {
        return script.hashCode();
    }
}
