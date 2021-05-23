package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class SplitLoop extends CloneVisitor implements Refactoring {

    public static final String NAME = "split_loop";

    private final Script script;

    private final LoopStmt loopStmt;

    private final Stmt splitPoint;

    private final Script replacementScript1;

    private final Script replacementScript2;

    public SplitLoop(Script script, LoopStmt loop, Stmt splitPoint) {
        this.script     = Preconditions.checkNotNull(script);
        this.loopStmt   = Preconditions.checkNotNull(loop);
        this.splitPoint = Preconditions.checkNotNull(splitPoint);
        Preconditions.checkArgument(script.getStmtList().getNumberOfStatements() == 1);
        Preconditions.checkArgument(script.getStmtList().getStatement(0) == loopStmt);

        List<Stmt> remainingStatements = apply(loopStmt.getStmtList()).getStmts();
        List<Stmt> initialStatements = apply(loopStmt.getStmtList()).getStmts();

        Iterator<Stmt> originalIterator  = loopStmt.getStmtList().getStmts().iterator();
        Iterator<Stmt> initialIterator   = initialStatements.iterator();
        Iterator<Stmt> remainingIterator = remainingStatements.iterator();

        boolean inInitial = true;
        while (originalIterator.hasNext()) {
            if (originalIterator.next() == splitPoint) {
                inInitial = false;
            }
            initialIterator.next();
            remainingIterator.next();

            if (inInitial) {
                remainingIterator.remove();
            } else {
                initialIterator.remove();
            }
        }

        StmtList subStatements1 = new StmtList(initialStatements);
        StmtList subStatements2 = new StmtList(remainingStatements);

        replacementScript1 = new Script(apply(script.getEvent()), new StmtList(getLoop(loopStmt, subStatements1)));
        replacementScript2 = new Script(apply(script.getEvent()), new StmtList(getLoop(loopStmt, subStatements2)));
    }

    private LoopStmt getLoop(LoopStmt loopStmt, StmtList body) {
        if (loopStmt instanceof RepeatForeverStmt) {
            RepeatForeverStmt origLoop = (RepeatForeverStmt) loopStmt;
            return new RepeatForeverStmt(body, apply(origLoop.getMetadata()));
        } else if (loopStmt instanceof RepeatTimesStmt) {
            RepeatTimesStmt origLoop = (RepeatTimesStmt) loopStmt;
            return new RepeatTimesStmt(apply(origLoop.getTimes()), body, apply(origLoop.getMetadata()));
        } else if (loopStmt instanceof UntilStmt) {
            UntilStmt origLoop = (UntilStmt) loopStmt;
            return new UntilStmt(apply(origLoop.getBoolExpr()), body, apply(origLoop.getMetadata()));
        } else {
            throw new RuntimeException("Unknown loop statement: "+loopStmt);
        }
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
                scripts.add(replacementScript1);
                scripts.add(replacementScript2);
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
        if (!(o instanceof SplitLoop)) return false;
        SplitLoop splitLoop = (SplitLoop) o;
        return Objects.equals(script, splitLoop.script) && Objects.equals(loopStmt, splitLoop.loopStmt) && Objects.equals(splitPoint, splitLoop.splitPoint) && Objects.equals(replacementScript1, splitLoop.replacementScript1) && Objects.equals(replacementScript2, splitLoop.replacementScript2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script, loopStmt, splitPoint, replacementScript1, replacementScript2);
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Splitting" + System.lineSeparator() + script + " at " + splitPoint + System.lineSeparator() +
                "Script 1:" + System.lineSeparator() + replacementScript1.getScratchBlocks() +  System.lineSeparator() +
                "Script 2:" + System.lineSeparator() + replacementScript2.getScratchBlocks() +  System.lineSeparator();
    }
}
