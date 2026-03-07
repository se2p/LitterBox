/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MergeLoopingScript extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "merge_looping_script";

    private final Script script1;
    private final Script script2;
    private final Script mergedScript;

    public MergeLoopingScript(Script script1, Script script2) {
        this.script1 = Preconditions.checkNotNull(script1);
        this.script2 = Preconditions.checkNotNull(script2);

        Stmt loopStmt1 = script1.getStmtList().getStmts().getFirst();
        Stmt loopStmt2 = script2.getStmtList().getStmts().getFirst();
        
        Preconditions.checkArgument(loopStmt1 instanceof LoopStmt);
        Preconditions.checkArgument(loopStmt2 instanceof LoopStmt);
        Preconditions.checkArgument(loopStmt1.getClass().equals(loopStmt2.getClass()));
        
        StmtList body1;
        StmtList body2;
        Stmt newLoop;
        
        if (loopStmt1 instanceof RepeatForeverStmt forever1) {
            RepeatForeverStmt forever2 = (RepeatForeverStmt) loopStmt2;
            body1 = forever1.getStmtList();
            body2 = forever2.getStmtList();
            
            List<Stmt> newStmts = new ArrayList<>(body1.getStmts());
            newStmts.addAll(body2.getStmts());
            
            newLoop = new RepeatForeverStmt(new StmtList(newStmts), forever1.getMetadata());
            
        } else if (loopStmt1 instanceof RepeatTimesStmt repeat1) {
            RepeatTimesStmt repeat2 = (RepeatTimesStmt) loopStmt2;
            Preconditions.checkArgument(repeat1.getTimes().equals(repeat2.getTimes())); // Should check value equality?
            
            body1 = repeat1.getStmtList();
            body2 = repeat2.getStmtList();
            
            List<Stmt> newStmts = new ArrayList<>(body1.getStmts());
            newStmts.addAll(body2.getStmts());
            
            newLoop = new RepeatTimesStmt(repeat1.getTimes(), new StmtList(newStmts), repeat1.getMetadata());
            
        } else if (loopStmt1 instanceof UntilStmt until1) {
            UntilStmt until2 = (UntilStmt) loopStmt2;
            Preconditions.checkArgument(until1.getBoolExpr().equals(until2.getBoolExpr()));
            
            body1 = until1.getStmtList();
            body2 = until2.getStmtList();
            
            List<Stmt> newStmts = new ArrayList<>(body1.getStmts());
            newStmts.addAll(body2.getStmts());
            
            newLoop = new UntilStmt(until1.getBoolExpr(), new StmtList(newStmts), until1.getMetadata());
        } else {
            throw new IllegalArgumentException("Unknown loop type");
        }
        
        this.mergedScript = new Script(apply(script1.getEvent()), new StmtList(newLoop));
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(this);
    }

    @Override
    public ASTNode visit(ScriptList node) {
        List<Script> scripts = new ArrayList<>();
        boolean added = false;
        for (Script currentScript : node.getScriptList()) {
            if (currentScript == this.script1) {
                if (!added) {
                    scripts.add(mergedScript);
                    added = true;
                }
            } else if (currentScript == this.script2) {
                if (!added) {
                    scripts.add(mergedScript);
                    added = true;
                }
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
        if (!(o instanceof MergeLoopingScript that)) {
            return false;
        }
        return Objects.equals(script1, that.script1)
                && Objects.equals(script2, that.script2)
                && Objects.equals(mergedScript, that.mergedScript);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script1, script2, mergedScript);
    }

    @Override
    public String getDescription() {
        return String.format("""
                %s
                Merging looping scripts
                %s
                and
                %s
                into:
                %s
                """,
                NAME,
                ScratchBlocksVisitor.of(script1),
                ScratchBlocksVisitor.of(script2),
                ScratchBlocksVisitor.of(mergedScript)
        );
    }
}
