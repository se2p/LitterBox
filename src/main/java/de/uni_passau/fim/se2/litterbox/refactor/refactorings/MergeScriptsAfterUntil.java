package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class MergeScriptsAfterUntil extends CloneVisitor implements Refactoring {

    public static final String NAME = "merge_scripts_after_until";

    private final Script script1;
    private final Script script2;
    private final UntilStmt untilStmt;
    private final Script replacementScript;

    public MergeScriptsAfterUntil(Script script1, Script script2, UntilStmt untilStmt) {
        this.script1 = Preconditions.checkNotNull(script1);
        this.script2 = Preconditions.checkNotNull(script2);
        Preconditions.checkArgument(script1.getEvent().equals(script2.getEvent()));
        this.untilStmt = Preconditions.checkNotNull(untilStmt);

        List<Stmt> mergedStatements = apply(script1.getStmtList()).getStmts();
        List<Stmt> script2Statements = apply(script2.getStmtList()).getStmts();
        WaitUntil waitUntil = (WaitUntil) script2Statements.remove(0);
        mergedStatements.addAll(script2Statements);

        replacementScript = new Script(apply(script1.getEvent()), new StmtList(mergedStatements));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(this);
    }

    @Override
    public ASTNode visit(ScriptList node) {
        List<Script> scripts = new ArrayList<>();
        for (Script currentScript : node.getScriptList()) {
            if (currentScript == this.script1) {
                scripts.add(replacementScript);
            } else if (currentScript != this.script2) {
                scripts.add(apply(currentScript));
            }
        }
        return new ScriptList(scripts);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Merging script1:" + System.lineSeparator() + script1.getScratchBlocks() + System.lineSeparator() +
                "with script 2:" + System.lineSeparator() + script2.getScratchBlocks() +  System.lineSeparator() +
                "Replacement script:" + System.lineSeparator() + replacementScript.getScratchBlocks() +  System.lineSeparator();
    }
}
