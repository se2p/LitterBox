package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SplitScriptAfterUntil extends CloneVisitor implements Refactoring {

    public static final String NAME = "split_script_after_until";

    private final Script script;
    private final UntilStmt untilStmt;
    private final Script replacementScript1;
    private final Script replacementScript2;

    public SplitScriptAfterUntil(Script script, UntilStmt untilStmt) {
        this.script = Preconditions.checkNotNull(script);
        this.untilStmt = Preconditions.checkNotNull(untilStmt);

        List<Stmt> remainingStatements = apply(script.getStmtList()).getStmts();
        List<Stmt> initialStatements   = apply(script.getStmtList()).getStmts();

        Iterator<Stmt> originalIterator  = script.getStmtList().getStmts().iterator();
        Iterator<Stmt> initialIterator   = initialStatements.iterator();
        Iterator<Stmt> remainingIterator = remainingStatements.iterator();

        boolean inInitial = true;
        while (originalIterator.hasNext()) {
            initialIterator.next();
            remainingIterator.next();
            if (inInitial) {
                remainingIterator.remove();
            } else {
                initialIterator.remove();
            }
            if (originalIterator.next() == untilStmt) {
                inInitial = false;
            }
        }

        remainingStatements.add(0, new WaitUntil(apply(untilStmt.getBoolExpr()), apply(untilStmt.getMetadata())));
        StmtList subStatements1 = new StmtList(initialStatements);
        StmtList subStatements2 = new StmtList(remainingStatements);

        replacementScript1 = new Script(apply(script.getEvent()), subStatements1);
        replacementScript2 = new Script(apply(script.getEvent()), subStatements2);
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
    public String toString() {
        return NAME + System.lineSeparator() + "Split script:" + System.lineSeparator() + script.getScratchBlocks() + System.lineSeparator() +
                "Replacement script 1:" + System.lineSeparator() + replacementScript1.getScratchBlocks() +  System.lineSeparator() +
                "Replacement script 2:" + System.lineSeparator() + replacementScript2.getScratchBlocks() +  System.lineSeparator();
    }
}
