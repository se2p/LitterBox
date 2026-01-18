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
package de.uni_passau.fim.se2.litterbox.injection.util;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

/**
 * Base visitor that tracks the current actor name while traversing the AST.
 *
 * <p>
 * Subclasses can access {@link #getCurrentActorName()} to get the name of the actor
 * currently being visited. This is useful for operations that need to know which
 * sprite/stage context they are operating in.
 *
 * <p>
 * Example usage:
 * <pre>
 * program.accept(new ActorTrackingVisitor() {
 *     &#64;Override
 *     public void visit(CreateCloneOf node) {
 *         String actorName = getCurrentActorName();
 *         // ... use actorName
 *     }
 * });
 * </pre>
 */
public abstract class ActorTrackingVisitor implements ScratchVisitor {

    private String currentActorName = null;

    /**
     * Returns the name of the actor (sprite/stage) currently being visited.
     *
     * @return the current actor name, or null if not inside an ActorDefinition
     */
    protected String getCurrentActorName() {
        return currentActorName;
    }

    @Override
    public void visit(ActorDefinition node) {
        String previousActorName = currentActorName;
        currentActorName = node.getIdent().getName();
        visitChildren(node);
        currentActorName = previousActorName;
    }
}
