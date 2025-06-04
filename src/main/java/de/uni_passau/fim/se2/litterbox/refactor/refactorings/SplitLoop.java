/*
 * Copyright (C) 2019-2024 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class SplitLoop extends OnlyCodeCloneVisitor implements Refactoring {

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
        if (loopStmt instanceof RepeatForeverStmt origLoop) {
            return new RepeatForeverStmt(body, apply(origLoop.getMetadata()));
        } else if (loopStmt instanceof RepeatTimesStmt origLoop) {
            return new RepeatTimesStmt(apply(origLoop.getTimes()), body, apply(origLoop.getMetadata()));
        } else if (loopStmt instanceof UntilStmt origLoop) {
            return new UntilStmt(apply(origLoop.getBoolExpr()), body, apply(origLoop.getMetadata()));
        } else {
            throw new RuntimeException("Unknown loop statement: " + loopStmt);
        }
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(this);
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof SplitLoop splitLoop)) {
            return false;
        }
        return Objects.equals(script, splitLoop.script)
                && Objects.equals(loopStmt, splitLoop.loopStmt)
                && Objects.equals(splitPoint, splitLoop.splitPoint)
                && Objects.equals(replacementScript1, splitLoop.replacementScript1)
                && Objects.equals(replacementScript2, splitLoop.replacementScript2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script, loopStmt, splitPoint, replacementScript1, replacementScript2);
    }

    @Override
    public String getDescription() {
        return String.format("""
                %s
                Splitting
                %s
                at
                %s
                Script 1:
                %s
                Script 2:
                %s
                """,
                NAME,
                ScratchBlocksVisitor.of(script),
                ScratchBlocksVisitor.of(splitPoint),
                ScratchBlocksVisitor.of(replacementScript1),
                ScratchBlocksVisitor.of(replacementScript2)
        );
    }
}
