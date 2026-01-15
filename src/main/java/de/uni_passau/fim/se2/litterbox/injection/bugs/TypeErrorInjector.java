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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.TypeError;
import de.uni_passau.fim.se2.litterbox.ast.builder.ComparisonBuilder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.SetVideoTransparency;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.WhenVideoMotionGreaterThan;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.VideoSensingExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Injects the {@link TypeError} bug using two strategies:
 *
 * <p>
 * <b>Strategy 1: Comparison Type Error</b><br>
 * Replaces one operand in comparisons (Equals, LessThan, BiggerThan) with a boolean.
 *
 * <p>
 * <b>Strategy 2: Number-to-Boolean Injection</b><br>
 * Replaces NUMBER parameters (in MoveSteps, SetXTo, WaitSeconds, etc.) with a boolean expression.
 *
 * <p>
 * For realistic bugs, this injector collects boolean expressions from the program
 * (like {@code Touching}, {@code IsMouseDown}, etc.) and uses them as replacement values.
 *
 */
public class TypeErrorInjector extends AbstractComparisonInjector implements VideoSensingExtensionVisitor {

    /**
     * Fallback boolean expression to use when no boolean expressions are found in the program.
     */
    private static final NumExpr FALLBACK_BOOL_AS_NUM = new AsNumber(
            new IsMouseDown(NonDataBlockMetadata.emptyNonBlockMetadata())
    );

    private final List<BoolExpr> collectedBoolExprs = new ArrayList<>();
    private final Set<ASTNode> buggyNodes = new HashSet<>();
    private NumExpr boolAsNumReplacement;

    @Override
    public List<InjectionResult> inject(Program program) {
        // First pass: collect boolean expressions from the program
        collectExpressions(program);

        // Pre-run finder to identify existing buggy nodes
        buggyNodes.clear();
        for (Issue issue : getIssueFinder().check(program)) {
            ASTNode node = issue.getCodeLocation();
            if (node != null) {
                buggyNodes.add(node);
            }
        }

        // Choose replacement expression (lazy initialization of fallback)
        if (!collectedBoolExprs.isEmpty()) {
            BoolExpr cloned = cloner.apply(collectedBoolExprs.getFirst());
            boolAsNumReplacement = new AsNumber(cloned);
        } else {
            boolAsNumReplacement = FALLBACK_BOOL_AS_NUM;
        }

        // Second pass: inject bugs
        return super.inject(program);
    }

    private final IssueFinder issueFinder = new TypeError();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    // ==================== Strategy 1: Comparison Type Errors ====================

    @Override
    public void visit(Equals node) {
        injectIntoComparison(node, node.getOperand1(),
                replacement -> new ComparisonBuilder()
                        .from(node)
                        .withOperand2(replacement)
                        .buildEquals());
        super.visit(node);
    }

    @Override
    public void visit(LessThan node) {
        injectIntoComparison(node, node.getOperand1(),
                replacement -> new ComparisonBuilder()
                        .from(node)
                        .withOperand2(replacement)
                        .buildLessThan());
        super.visit(node);
    }

    @Override
    public void visit(BiggerThan node) {
        injectIntoComparison(node, node.getOperand1(),
                replacement -> new ComparisonBuilder()
                        .from(node)
                        .withOperand2(replacement)
                        .buildBiggerThan());
        super.visit(node);
    }

    // ==================== Strategy 2: Number-to-Boolean Injection ====================

    @Override
    public void visit(MoveSteps node) {
        injectIntoNumericParam(node, n -> new MoveSteps(getBoolAsNumReplacement(), n.getMetadata()));
        super.visit(node);
    }

    @Override
    public void visit(SetXTo node) {
        injectIntoNumericParam(node, n -> new SetXTo(getBoolAsNumReplacement(), n.getMetadata()));
        super.visit(node);
    }

    @Override
    public void visit(SetYTo node) {
        injectIntoNumericParam(node, n -> new SetYTo(getBoolAsNumReplacement(), n.getMetadata()));
        super.visit(node);
    }

    @Override
    public void visit(WaitSeconds node) {
        injectIntoNumericParam(node, n -> new WaitSeconds(getBoolAsNumReplacement(), n.getMetadata()));
        super.visit(node);
    }

    @Override
    public void visit(SetVideoTransparency node) {
        injectIntoNumericParam(node, n -> new SetVideoTransparency(getBoolAsNumReplacement(), n.getMetadata()));
        visitChildren(node);
    }

    @Override
    public void visit(WhenVideoMotionGreaterThan node) {
        injectIntoNumericParam(node, n -> new WhenVideoMotionGreaterThan(getBoolAsNumReplacement(), n.getMetadata()));
        visitChildren(node);
    }

    // ==================== Helper Methods ====================

    /**
     * Injects a type error into a comparison node if it's a valid candidate.
     * A node is a candidate if it's not already buggy and has a literal operand.
     *
     * @param node        The comparison node
     * @param operand1    The first operand to check for literal type
     * @param constructor Function to construct the replacement
     * @param <T>         The node type
     */
    private <T extends ASTNode> void injectIntoComparison(
            T node,
            ASTNode operand1,
            Function<NumExpr, T> constructor) {

        if (!isBuggy(node) && (operand1 instanceof NumberLiteral || operand1 instanceof StringLiteral)) {
            replaceNodeImmutably(node, n -> constructor.apply(getBoolAsNumReplacement()));
        }
    }

    /**
     * Injects a type error into blocks with numeric parameters.
     *
     * @param node        The node to potentially replace
     * @param constructor Function to construct the replacement
     * @param <T>         The node type
     */
    private <T extends ASTNode> void injectIntoNumericParam(T node, Function<T, ASTNode> constructor) {
        if (!isBuggy(node)) {
            replaceNodeImmutably(node, constructor);
        }
    }

    private boolean isBuggy(ASTNode node) {
        if (buggyNodes.contains(node)) {
            return true;
        }
        for (ASTNode child : node.getChildren()) {
            if (isBuggy(child)) {
                return true;
            }
        }
        return false;
    }

    private void collectExpressions(Program program) {
        collectedBoolExprs.clear();
        program.accept(new ExpressionCollector());
    }

    private NumExpr getBoolAsNumReplacement() {
        return cloner.apply(boolAsNumReplacement);
    }

    /**
     * Visitor that collects boolean expressions for realistic injection.
     */
    private class ExpressionCollector implements ScratchVisitor {
        @Override
        public void visit(Touching node) {
            collectedBoolExprs.add(node);
        }

        @Override
        public void visit(IsMouseDown node) {
            collectedBoolExprs.add(node);
        }

        @Override
        public void visit(IsKeyPressed node) {
            collectedBoolExprs.add(node);
        }

        @Override
        public void visit(SpriteTouchingColor node) {
            collectedBoolExprs.add(node);
        }
    }
}
