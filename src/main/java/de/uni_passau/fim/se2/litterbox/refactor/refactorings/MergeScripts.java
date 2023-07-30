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

public class MergeScripts extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "merge_scripts";

    private final Script script1;

    private final Script script2;

    private final Script replacementScript;

    public MergeScripts(Script script1, Script script2) {
        this.script1 = Preconditions.checkNotNull(script1);
        this.script2 = Preconditions.checkNotNull(script2);
        Preconditions.checkArgument(script1.getEvent().equals(script2.getEvent()));

        List<Stmt> mergedStatements = new ArrayList<>();
        mergedStatements.addAll(apply(script1.getStmtList()).getStmts());
        mergedStatements.addAll(apply(script2.getStmtList()).getStmts());

        replacementScript = new Script(apply(script1.getEvent()), new StmtList(mergedStatements));
    }

    public Script getMergedScript() {
        return replacementScript;
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(this);
    }

    @Override
    public ASTNode visit(ScriptList node) {
        List<Script> scripts = new ArrayList<>();
        for (Script currentScript : node.getScriptList()) {
            if (currentScript == this.script1) {
                scripts.add(replacementScript);
            } else if (currentScript != this.script2) {
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
        if (!(o instanceof MergeScripts that)) {
            return false;
        }
        return Objects.equals(script1, that.script1)
                && Objects.equals(script2, that.script2)
                && Objects.equals(replacementScript, that.replacementScript);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script1, script2, replacementScript);
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Merging" + System.lineSeparator() + script1.getScratchBlocks() + System.lineSeparator()
                + " and " + System.lineSeparator() + script2.getScratchBlocks() +  System.lineSeparator()
                + " to:" + System.lineSeparator() + replacementScript.getScratchBlocks() +  System.lineSeparator();
    }
}
