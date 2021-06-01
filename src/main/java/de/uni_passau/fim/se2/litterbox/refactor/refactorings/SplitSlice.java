package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SplitSlice extends CloneVisitor implements Refactoring {

    public static final String NAME = "split_slice";

    private final Script script;

    private final Set<Stmt> slice;

    private final Script replacementScriptSlice;

    private final Script replacementScriptRemainder;

    public SplitSlice(Script script, Set<Stmt> slice) {
        this.script = Preconditions.checkNotNull(script);
        this.slice = Preconditions.checkNotNull(slice);

        List<Stmt> remainderStatements = script.getStmtList().getStmts().stream().filter(s -> !slice.contains(s)).map(s -> apply(s)).collect(Collectors.toList());
        List<Stmt> sliceStatements = script.getStmtList().getStmts().stream().filter(s -> slice.contains(s)).map(s -> apply(s)).collect(Collectors.toList());

        replacementScriptSlice     = new Script(apply(script.getEvent()), new StmtList(sliceStatements));
        replacementScriptRemainder = new Script(apply(script.getEvent()), new StmtList(remainderStatements));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(this);
    }

    @Override
    public ASTNode visit(ScriptList node) {
        List<Script> scripts = new ArrayList<>();
        for (Script currentScript : node.getScriptList()) {
            if (currentScript == this.script) {
                scripts.add(replacementScriptSlice);
                scripts.add(replacementScriptRemainder);
            } else {
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
        if (!(o instanceof SplitSlice)) return false;
        SplitSlice that = (SplitSlice) o;
        return Objects.equals(script, that.script) && Objects.equals(slice, that.slice) && Objects.equals(replacementScriptSlice, that.replacementScriptSlice) && Objects.equals(replacementScriptRemainder, that.replacementScriptRemainder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script, slice, replacementScriptSlice, replacementScriptRemainder);
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Slice:" + System.lineSeparator() + replacementScriptSlice.getScratchBlocks() + System.lineSeparator() +
                "Remainder:" + System.lineSeparator() + replacementScriptRemainder.getScratchBlocks() +  System.lineSeparator();
    }
}
