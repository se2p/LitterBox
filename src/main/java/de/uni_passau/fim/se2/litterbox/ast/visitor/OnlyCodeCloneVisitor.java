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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class OnlyCodeCloneVisitor extends CloneVisitor {
    /**
     * Default implementation of visit method for ActorDefinition.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ActorDefinition which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ActorDefinition node) {
        ActorDefinition actorDefinition = new ActorDefinition(node.getActorType(), node.getIdent(), node.getDecls(), node.getSetStmtList(), apply(node.getProcedureDefinitionList()), apply(node.getScripts()), node.getActorMetadata());
        actorDefinition.getScripts().accept(new ParentVisitor());
        actorDefinition.getProcedureDefinitionList().accept(new ParentVisitor());
        return actorDefinition;
    }

    /**
     * Default implementation of visit method for {@link Program}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Program  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Program node) {
        Program program = new Program(node.getIdent(),
                apply(node.getActorDefinitionList()),
                node.getSymbolTable(),
                node.getProcedureMapping(),
                node.getProgramMetadata());
        return program;
    }
}
