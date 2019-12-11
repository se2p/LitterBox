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
package scratch.ast.visitor;

import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.event.Event;
import scratch.ast.model.event.StartedAsClone;
import scratch.ast.model.expression.bool.BiggerThan;
import scratch.ast.model.expression.bool.Equals;
import scratch.ast.model.expression.bool.LessThan;
import scratch.ast.model.procedure.ProcedureDefinition;
import scratch.ast.model.statement.common.CreateCloneOf;
import scratch.ast.model.statement.common.SetStmt;
import scratch.ast.model.statement.pen.PenClearStmt;
import scratch.ast.model.statement.pen.PenDownStmt;
import scratch.ast.model.statement.pen.PenUpStmt;
import scratch.ast.model.variable.StrId;

public interface ScratchVisitor {

    /**
     * Default implementation of visit method for ASTNode.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ASTNode of which the children will be iterated
     */
    default void visit(ASTNode node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    /**
     * Default implementation of visit method for ActorDefinition.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ActorDefinition of which the children will be iterated
     */
    default void visit(ActorDefinition node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for PenDownStmt.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenDownStmt of which the children will be iterated
     */
    default void visit(PenDownStmt node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for PenUpStmt.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenUpStmt of which the children will be iterated
     */
    default void visit(PenUpStmt node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for PenUpStmt.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenUpStmt of which the children will be iterated
     */
    default void visit(PenClearStmt node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for SetStmt.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetStmt of which the children will be iterated
     */
    default void visit(SetStmt node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link Equals}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Equals Node of which the children will be iterated
     */
    default void visit(Equals node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link LessThan}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LessThan Node of which the children will be iterated
     */
    default void visit(LessThan node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link BiggerThan}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BiggerThan Node of which the children will be iterated
     */
    default void visit(BiggerThan node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ProcedureDefinition}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ProcedureDefinition Node of which the children will be iterated
     */
    default void visit(ProcedureDefinition node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link StrId}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StrId of which the children will be iterated
     */
    default void visit(StrId node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link Event}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Event of which the children will be iterated
     */
    default void visit(Event node){visit((ASTNode) node);}

    /**
     * Default implementation of visit method for {@link CreateCloneOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node CreateCloneOf Node of which the children will be iterated
     */
    default void visit(CreateCloneOf node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link StartedAsClone}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StartedAsClone Node of which the children will be iterated
     */
    default void visit(StartedAsClone node) {
        visit((ASTNode) node);
    }
}
