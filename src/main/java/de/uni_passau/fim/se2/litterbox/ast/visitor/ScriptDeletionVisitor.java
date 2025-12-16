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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ScriptDeletionVisitor extends CloneVisitor {

    private final Set<Script> scriptsToRemove;

    public ScriptDeletionVisitor(Set<Script> scriptsToRemove) {
        this.scriptsToRemove = scriptsToRemove;
    }

    @Override
    public ASTNode visit(Program node) {
        ActorDefinitionList newActors = (ActorDefinitionList) node.getActorDefinitionList().accept(this);
        return new Program(
                node.getIdent(), newActors, node.getSymbolTable(), node.getProcedureMapping(), node.getProgramMetadata()
        );
    }

    @Override
    public ASTNode visit(ActorDefinitionList node) {
        List<ActorDefinition> newDefs = new ArrayList<>();
        for (ActorDefinition def : node.getDefinitions()) {
            newDefs.add((ActorDefinition) def.accept(this));
        }
        return new ActorDefinitionList(newDefs);
    }

    @Override
    public ASTNode visit(ActorDefinition node) {
        ScriptList newScripts = (ScriptList) node.getScripts().accept(this);
        return new ActorDefinition(
                node.getActorType(),
                node.getIdent(),
                node.getDecls(),
                node.getSetStmtList(),
                node.getProcedureDefinitionList(),
                newScripts,
                node.getActorMetadata()
        );
    }

    @Override
    public ASTNode visit(ScriptList node) {
        List<Script> scripts = new ArrayList<>();
        for (Script script : node.getScriptList()) {
            if (!scriptsToRemove.contains(script)) {
                scripts.add(script);
            }
        }
        return new ScriptList(scripts);
    }
}
