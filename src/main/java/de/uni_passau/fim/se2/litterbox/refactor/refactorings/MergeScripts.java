package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MergeScripts extends CloneVisitor implements Refactoring {

    public static final String NAME = "merge_scripts";

    private final Script script1;

    private final Script script2;

    private final Script replacementScript;

    public MergeScripts(Script script1, Script script2) {
        this.script1 = Preconditions.checkNotNull(script1);
        this.script2 = Preconditions.checkNotNull(script2);
        Preconditions.checkArgument(script1.getEvent().equals(script2.getEvent()));

        List<Stmt> mergedStatements = new ArrayList<>();
        mergedStatements.addAll(apply(script1.getStmtList()).getStmts());
        mergedStatements.addAll(apply(script2.getStmtList()).getStmts());

        replacementScript = new Script(apply(script1.getEvent()), new StmtList(mergedStatements));
    }

    public Script getMergedScript() {
        return replacementScript;
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
            } else if (currentScript != this.script2){
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MergeScripts)) return false;
        MergeScripts that = (MergeScripts) o;
        return Objects.equals(script1, that.script1) && Objects.equals(script2, that.script2) && Objects.equals(replacementScript, that.replacementScript);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script1, script2, replacementScript);
    }
}
