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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A CloneVisitor that can apply transformations to specific nodes during cloning.
 *
 * <p>
 * Unlike using setter methods (which break immutability), this visitor creates
 * new node instances with transformed children, maintaining the immutability
 * of the original AST.
 *
 * <p>
 * Usage:
 * <pre>
 * TransformingCloneVisitor visitor = new TransformingCloneVisitor();
 * visitor.register(originalMoveSteps, node ->
 *     new MoveSteps(newStepsExpr, node.getMetadata()));
 * Program mutated = visitor.apply(originalProgram);
 * </pre>
 */
public class TransformingCloneVisitor extends CloneVisitor {

    private final Map<ASTNode, Function<ASTNode, ASTNode>> transformations = new IdentityHashMap<>();
    private final Map<ASTNode, ASTNode> replacements = new IdentityHashMap<>();
    private final Map<ASTNode, ASTNode> mapping = new IdentityHashMap<>();

    public TransformingCloneVisitor() {
        super(true);
    }

    /**
     * Registers a transformation for a specific node.
     * When this node is visited during cloning, the transformation function
     * will be applied instead of the standard cloning.
     *
     * @param target         The original node to transform
     * @param transformation Function that takes the original node and returns the replacement
     * @param <T>            Type of the node
     */
    public <T extends ASTNode> void register(T target, Function<T, ASTNode> transformation) {
        @SuppressWarnings("unchecked")
        Function<ASTNode, ASTNode> cast = node -> transformation.apply((T) node);
        transformations.put(target, cast);
    }

    /**
     * Returns the replacement node that was created for the given original node.
     * Returns null if the node was not transformed.
     *
     * @param original The original node that was registered for transformation
     * @return The replacement node, or null if not transformed
     */
    public ASTNode getReplacement(ASTNode original) {
        return replacements.get(original);
    }

    /**
     * Returns the mapping from original AST nodes to their cloned counterparts.
     *
     * @return The original-to-cloned node mapping
     */
    public Map<ASTNode, ASTNode> getMapping() {
        return mapping;
    }

    /**
     * Checks if a node has a registered transformation.
     */
    protected boolean hasTransformation(ASTNode node) {
        return transformations.containsKey(node);
    }

    /**
     * Applies the registered transformation to a node and records the replacement.
     */
    protected <T extends ASTNode> ASTNode applyTransformation(T node) {
        ASTNode replacement = transformations.get(node).apply(node);
        replacements.put(node, replacement);
        return replacement;
    }

    /**
     * Override apply to handle transformations that change the node type.
     * When a transformation is registered, skip the type cast since the
     * replacement may be a different type than the original.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ASTNode> T apply(T node) {
        if (node == null) {
            return null;
        }

        T cloned;
        // If this node has a transformation, apply it and return the result
        // without the type check (since the type may change)
        if (hasTransformation(node)) {
            cloned = (T) applyTransformation(node);
        } else {
            // Otherwise, use the default behavior
            cloned = super.apply(node);
        }

        if (cloned != null) {
            mapping.put(node, cloned);
        }

        return cloned;
    }

    // --- Motion Blocks ---

    @Override
    public ASTNode visit(MoveSteps node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetXTo node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetYTo node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PointInDirection node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    // --- Timing and Variable Blocks ---

    @Override
    public ASTNode visit(WaitSeconds node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetVariableTo node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ChangeVariableBy node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    // --- Comparison Expressions ---

    @Override
    public ASTNode visit(Equals node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(LessThan node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(BiggerThan node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    // --- Arithmetic Expressions ---

    @Override
    public ASTNode visit(Add node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Minus node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Mult node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Div node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Mod node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PickRandom node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Round node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Variable node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Parameter node) {
        if (hasTransformation(node)) {
            return applyTransformation(node);
        }
        return super.visit(node);
    }
}

