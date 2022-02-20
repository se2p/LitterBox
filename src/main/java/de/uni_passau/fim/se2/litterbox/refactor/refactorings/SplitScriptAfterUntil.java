/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class SplitScriptAfterUntil extends OnlyCodeCloneVisitor implements Refactoring {

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
        return NAME + System.lineSeparator() + "Split script:" + System.lineSeparator() + script.getScratchBlocks() + System.lineSeparator()
                + "Replacement script 1:" + System.lineSeparator() + replacementScript1.getScratchBlocks() +  System.lineSeparator()
                + "Replacement script 2:" + System.lineSeparator() + replacementScript2.getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SplitScriptAfterUntil that = (SplitScriptAfterUntil) o;
        return Objects.equals(script, that.script) && Objects.equals(untilStmt, that.untilStmt) && Objects.equals(replacementScript1, that.replacementScript1) && Objects.equals(replacementScript2, that.replacementScript2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script, untilStmt, replacementScript1, replacementScript2);
    }
}
