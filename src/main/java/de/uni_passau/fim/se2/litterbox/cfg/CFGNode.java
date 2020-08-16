/*
 * Copyright (C) 2020 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;

import java.util.*;

public abstract class CFGNode {

    protected ActorDefinition actor = null;
    protected ASTNode scriptOrProcedure = null;
    private Set<Definition> definitions = null;
    private Set<Use> uses = null;

    public abstract ASTNode getASTNode();

    public ActorDefinition getActor() {
        return actor;
    }

    public ASTNode getScriptOrProcedure() {
        return scriptOrProcedure;
    }

    public Set<Definition> getDefinitions() {
        if (definitions == null) {
            definitions = calculateDefinitions();
        }

        return Collections.unmodifiableSet(definitions);
    }

    public Set<Use> getUses() {
        if (uses == null) {
            uses = calculateUses();
        }

        return Collections.unmodifiableSet(uses);
    }

    private Set<Definition> calculateDefinitions() {
        if (getASTNode() == null) {
            return Collections.emptySet();
        }

        Set<Definition> definitions = new LinkedHashSet<>();
        List<DefinableCollector> collectors = new ArrayList<>(Arrays.asList(
                new VariableDefinitionVisitor(),
                new ListDefinitionVisitor()
        ));

        if (getActor() != null) {
            collectors.add(new AttributeDefinitionVisitor(getActor()));
        }

        for (DefinableCollector collector : collectors) {
            getASTNode().accept(collector);
            Set<Defineable> defineables = collector.getDefineables();
            defineables.forEach(d -> definitions.add(new Definition(this, d)));
        }

        return definitions;
    }

    private Set<Use> calculateUses() {
        if (getASTNode() == null) {
            return Collections.emptySet();
        }

        Set<Use> uses = new LinkedHashSet<>();
        List<DefinableCollector> collectors = new ArrayList<>(Arrays.asList(
                new VariableUseVisitor(),
                new ListUseVisitor()
        ));

        if (getActor() != null) {
            collectors.add(new AttributeUseVisitor(getActor()));
        }

        for (DefinableCollector collector : collectors) {
            getASTNode().accept(collector);
            Set<Defineable> defineables = collector.getDefineables();
            defineables.forEach(d -> uses.add(new Use(this, d)));
        }
        return uses;
    }
}
