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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.ast.builder.ComparisonBuilder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Base class for bug injectors that work with comparison operators (Equals, LessThan, BiggerThan).
 *
 * <p>Provides convenience methods for literal detection and matching literal creation,
 * delegating to {@link ComparisonBuilder} static utilities.
 *
 * <p>Subclasses can override the template methods {@link #injectIntoEquals}, {@link #injectIntoLessThan},
 * or {@link #injectIntoBiggerThan} to inject bugs at specific comparison node types without
 * duplicating the visit pattern.
 */
public abstract class AbstractComparisonInjector extends AbstractBugInjector {

    /**
     * Checks if an expression is a literal value, including wrapped literals.
     * Convenience wrapper around {@link ComparisonBuilder#isLiteral(ComparableExpr)}.
     *
     * @param expr The expression to check
     * @return true if the expression is a literal or wrapped literal
     */
    protected boolean isLiteral(ComparableExpr expr) {
        return ComparisonBuilder.isLiteral(expr);
    }

    /**
     * Creates a matching literal for the given reference expression.
     * Convenience wrapper around {@link ComparisonBuilder#createMatchingLiteral(ComparableExpr)}.
     *
     * @param reference The expression to create a matching literal for
     * @return A new literal matching the type and value of the reference
     */
    protected ComparableExpr createMatchingLiteral(ComparableExpr reference) {
        return ComparisonBuilder.createMatchingLiteral(reference);
    }

    // ==================== Template Methods ====================
    // Subclasses override these to inject bugs at specific comparison types.
    // Default implementations are no-ops.

    /**
     * Template method called when visiting an Equals node.
     * Override this to inject bugs into equality comparisons.
     * The default implementation does nothing.
     *
     * @param node The Equals node being visited
     */
    protected void injectIntoEquals(Equals node) {
        // Default: no-op
    }

    /**
     * Template method called when visiting a LessThan node.
     * Override this to inject bugs into less-than comparisons.
     * The default implementation does nothing.
     *
     * @param node The LessThan node being visited
     */
    protected void injectIntoLessThan(LessThan node) {
        // Default: no-op
    }

    /**
     * Template method called when visiting a BiggerThan node.
     * Override this to inject bugs into greater-than comparisons.
     * The default implementation does nothing.
     *
     * @param node The BiggerThan node being visited
     */
    protected void injectIntoBiggerThan(BiggerThan node) {
        // Default: no-op
    }

    // ==================== Visitor Implementation ====================
    // These call the template methods and then continue traversal.

    @Override
    public void visit(Equals node) {
        injectIntoEquals(node);
        super.visit(node);
    }

    @Override
    public void visit(LessThan node) {
        injectIntoLessThan(node);
        super.visit(node);
    }

    @Override
    public void visit(BiggerThan node) {
        injectIntoBiggerThan(node);
        super.visit(node);
    }
}
