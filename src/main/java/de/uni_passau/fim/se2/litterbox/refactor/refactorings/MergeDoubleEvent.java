package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class MergeDoubleEvent extends CloneVisitor implements Refactoring {

    private final Event event1;
    private final Event event2;
    private final Script script1;
    private final Script script2;
    private final Script replacement;
    private static final String NAME = "merge_double_event";
    private final String script1String;
    private final String script2String;
    private final String replacementString;

    public MergeDoubleEvent(Event event1, Event event2) {
        this.event1 = Preconditions.checkNotNull(event1);
        this.event2 = Preconditions.checkNotNull(event2);
        this.script1 = (Script) event1.getParentNode();
        this.script2 = (Script) event2.getParentNode();

        CloneVisitor cloneVisitor = new CloneVisitor();
        List<Stmt> mergedStmts = cloneVisitor.apply(script1.getStmtList()).getStmts();
        mergedStmts.addAll(cloneVisitor.apply(script2.getStmtList()).getStmts());
        Event event = cloneVisitor.apply(event1);
        replacement = new Script(event, new StmtList(mergedStmts));

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor();
        script1.accept(visitor);
        script1String = visitor.getScratchBlocks();
        visitor = new ScratchBlocksVisitor();
        script2.accept(visitor);
        script2String = visitor.getScratchBlocks();
        visitor = new ScratchBlocksVisitor();
        replacement.accept(visitor);
        replacementString = visitor.getScratchBlocks();
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(this);
    }

    @Override
    public ScriptList visit(ScriptList scriptList) {
        List<Script> scripts = new ArrayList<>();
        for (Script script : scriptList.getScriptList()) {
            if (script != script2) {
                if (script == script1) {
                    scripts.add(replacement);
                } else {
                    scripts.add(apply(script));
                }
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
        return NAME + "\nReplaced scripts:\n\n" + script1String + "\n" + script2String + "\nReplacement:\n\n" + replacementString;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MergeDoubleEvent)) {
            return false;
        }

        return event1.equals(((MergeDoubleEvent) other).event1)
                && event2.equals(((MergeDoubleEvent) other).event2);
    }

    @Override
    public int hashCode() {
        return event1.hashCode() + event2.hashCode();
    }
}
