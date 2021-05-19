package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/*
if A:
  B
  C

to

if A:
  B
if A:
  C
 */
public class SplitIf extends CloneVisitor implements Refactoring {

    private final IfThenStmt ifThenStmt;
    private final Stmt splitPoint;
    private final IfThenStmt replacementIf1;
    private final IfThenStmt replacementIf2;
    public static final String NAME = "split_if";

    public SplitIf(IfThenStmt if1, Stmt splitPoint) {
        this.ifThenStmt = Preconditions.checkNotNull(if1);
        this.splitPoint = Preconditions.checkNotNull(splitPoint);
        Preconditions.checkArgument(ifThenStmt.getThenStmts().getNumberOfStatements() > 1);

        List<Stmt> remainingStatements = apply(if1.getThenStmts()).getStmts();
        List<Stmt> initialStatements   = apply(if1.getThenStmts()).getStmts();

        Iterator<Stmt> originalIterator  = if1.getThenStmts().getStmts().iterator();
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

        replacementIf1 = new IfThenStmt(apply(if1.getBoolExpr()), subStatements1, apply(if1.getMetadata()));
        replacementIf2 = new IfThenStmt(apply(if1.getBoolExpr()), subStatements2, apply(if1.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(ifThenStmt, replacementIf1, replacementIf2));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplitIf)) return false;
        SplitIf splitIf = (SplitIf) o;
        return Objects.equals(ifThenStmt, splitIf.ifThenStmt) && Objects.equals(splitPoint, splitIf.splitPoint) && Objects.equals(replacementIf1, splitIf.replacementIf1) && Objects.equals(replacementIf2, splitIf.replacementIf2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifThenStmt, splitPoint, replacementIf1, replacementIf2);
    }
}
