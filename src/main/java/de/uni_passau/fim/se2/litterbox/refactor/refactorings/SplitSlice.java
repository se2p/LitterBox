/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SplitSlice extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "split_slice";

    private final Script script;

    private final Set<List<Stmt>> slices;

    private final List<Script> replacementScripts;

    public SplitSlice(Script script, Set<List<Stmt>> slices) {
        this.script = Preconditions.checkNotNull(script);
        this.slices = Preconditions.checkNotNull(slices);

        replacementScripts = new ArrayList<>();

        for (List<Stmt> slice : slices) {
            replacementScripts.add(new Script(apply(script.getEvent()), new StmtList(applyList(slice))));
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
        if (this == o) return true;
        if (!(o instanceof SplitSlice)) return false;
        SplitSlice that = (SplitSlice) o;
        return Objects.equals(script, that.script) && Objects.equals(slices, that.slices) && Objects.equals(replacementScripts, that.replacementScripts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script, slices, replacementScripts);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(NAME);
        sb.append(System.lineSeparator());
        sb.append("Original script:");
        sb.append(System.lineSeparator());
        sb.append(script.getScratchBlocks());
        sb.append(System.lineSeparator());
        sb.append("Slices:");
        sb.append(System.lineSeparator());
        for (Script script : replacementScripts) {
            sb.append("Script:");
            sb.append(script.getScratchBlocks());
            sb.append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        return sb.toString();
    }
}
