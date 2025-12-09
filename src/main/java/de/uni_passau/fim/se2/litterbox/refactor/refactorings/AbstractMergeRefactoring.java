/*
 * Copyright (C) 2019-2025 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMergeRefactoring extends OnlyCodeCloneVisitor implements Refactoring {

    protected final Script script1;
    protected final Script script2;
    protected final Script replacementScript;

    protected AbstractMergeRefactoring(Script script1, Script script2, Script replacementScript) {
        this.script1 = Preconditions.checkNotNull(script1);
        this.script2 = Preconditions.checkNotNull(script2);
        this.replacementScript = replacementScript;
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
}
