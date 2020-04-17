/*
 * Copyright (C) 2019 LitterBox contributors
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

package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Qualified;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class CFGNode {

    private Set<Definition> definitions = null;

    private Set<Use> uses = null;

    public abstract ASTNode getASTNode();

    public Set<Definition> getDefinitions() {
        if(definitions == null) {
            definitions = calculateDefinitions();
        }
        return definitions;
    }

    public Set<Use> getUses() {
        if(uses == null) {
            uses = calculateUses();
        }
        return uses;
    }

    private Set<Definition> calculateDefinitions() {
        if(getASTNode() == null) {
            return Collections.emptySet();
        }
        DefinitionVisitor visitor = new DefinitionVisitor();
        getASTNode().accept(visitor);

        Set<Definition> definitions = new LinkedHashSet<>();
        for(Qualified q : visitor.getDefinitions()) {
            definitions.add(new Definition(this, new Variable(q)));
        }
        return definitions;
    }

    private Set<Use> calculateUses() {
        if(getASTNode() == null) {
            return Collections.emptySet();
        }
        UseVisitor visitor = new UseVisitor();
        getASTNode().accept(visitor);

        Set<Use> uses = new LinkedHashSet<>();
        for(Qualified q : visitor.getUses()) {
            uses.add(new Use(this, new Variable(q)));
        }
        return uses;
    }

}
