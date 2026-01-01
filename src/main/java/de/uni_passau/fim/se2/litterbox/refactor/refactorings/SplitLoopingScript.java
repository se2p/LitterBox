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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
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
import java.util.stream.Collectors;

public class SplitLoopingScript extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "split_looping_script";

    private final Script script;

    private final List<Script> replacementScripts;

    public SplitLoopingScript(Script script) {
        this.script = Preconditions.checkNotNull(script);
        this.replacementScripts = new ArrayList<>();

        Stmt loopStmt = script.getStmtList().getStmts().getFirst();
        Preconditions.checkArgument(loopStmt instanceof LoopStmt);
        
        StmtList loopBody;
        if (loopStmt instanceof RepeatForeverStmt forever) {
            loopBody = forever.getStmtList();
        } else if (loopStmt instanceof RepeatTimesStmt repeat) {
            loopBody = repeat.getStmtList();
        } else if (loopStmt instanceof UntilStmt until) {
            loopBody = until.getStmtList();
        } else {
            throw new IllegalArgumentException("Unknown loop type");
        }

        for (Stmt stmt : loopBody.getStmts()) {
            Preconditions.checkArgument(stmt instanceof IfStmt);
            
            // Create a new loop with just this statement
            StmtList newBody = new StmtList(apply(stmt));
            Stmt newLoop = switch (loopStmt) {
                case RepeatForeverStmt forever -> new RepeatForeverStmt(newBody, forever.getMetadata());
                case RepeatTimesStmt repeat -> new RepeatTimesStmt(repeat.getTimes(), newBody, repeat.getMetadata());
                case UntilStmt until -> new UntilStmt(until.getBoolExpr(), newBody, until.getMetadata());
                default -> throw new IllegalStateException("Should not happen");
            };
            
            replacementScripts.add(new Script(apply(script.getEvent()), new StmtList(newLoop)));
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
                scripts.addAll(replacementScripts);
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
        if (!(o instanceof SplitLoopingScript that)) {
            return false;
        }
        return Objects.equals(script, that.script)
                && Objects.equals(replacementScripts, that.replacementScripts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script, replacementScripts);
    }

    @Override
    public String getDescription() {
        String replacements = replacementScripts.stream()
                .map(ScratchBlocksVisitor::of)
                .collect(Collectors.joining("\n"));
        return String.format("""
                %s
                Splitting looping script
                %s
                into:
                %s
                """,
                NAME,
                ScratchBlocksVisitor.of(script),
                replacements
        );
    }
}
