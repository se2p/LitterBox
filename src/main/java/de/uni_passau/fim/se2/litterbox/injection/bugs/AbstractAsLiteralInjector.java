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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.StringContains;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.LengthOfString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Join;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.LetterOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.AddTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.InsertAt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.ReplaceItem;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Abstract base class for injectors that replace identifier references (Variable or Parameter)
 * with string literals of their names.
 */
public abstract class AbstractAsLiteralInjector extends AbstractBugInjector {

    private Set<Expression> injectedNodes;
    private Set<String> existingIssueNames;
    private boolean ignoreExistingIssues = false;

    public void setIgnoreExistingIssues(boolean ignore) {
        this.ignoreExistingIssues = ignore;
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        injectedNodes = new HashSet<>();
        // Collect names that already trigger issues - skip injecting for these
        if (!ignoreExistingIssues) {
            existingIssueNames = collectExistingIssueNames(program);
        } else {
            existingIssueNames = new HashSet<>();
        }
        return super.inject(program);
    }

    /**
     * Replaces specific visit methods with generic checkAndInject.
     * Subclasses should call this method in their visit(Variable) or visit(Parameter) implementations.
     */
    protected void checkAndInject(Expression node) {
        debugFoundCandidate();

        // Check string context unless subclass opts out (e.g., ParameterAsLiteralInjector)
        if (shouldCheckStringContext() && !isInStringContext(node)) {
            debugPreconditionFailed();
            return;
        }

        // Skip nodes inside Message (broadcast messages) - finder doesn't descend into those
        if (isInsideMessage(node)) {
            debugPreconditionFailed();
            return;
        }

        if (injectedNodes.contains(node)) {
            debugPreconditionFailed();
            return;
        }
        injectedNodes.add(node);

        Optional<StringLiteral> replacement = checkAndCreateReplacementGeneric(node);
        if (replacement.isPresent()) {
            String literalName = replacement.get().getText();

            // Skip if the bug already exists for this name (precondition)
            if (existingIssueNames.contains(literalName)) {
                debugPreconditionFailed();
                return;
            }

            debugMutationAttempted();
            replaceWith(node, n -> new StringLiteral(literalName));
        } else {
            debugPreconditionFailed();
        }
    }

    /**
     * Returns true if this injector should check for string context before injecting.
     * Subclasses can override to return false if they want to inject in all contexts.
     *
     * @return true by default (check string context), false to skip the check
     */
    protected boolean shouldCheckStringContext() {
        return true;
    }

    /**
     * Collects the names (literal values) that already trigger issues in the program.
     * We skip injecting for these names since the bug already exists.
     */
    private Set<String> collectExistingIssueNames(Program program) {
        Set<Issue> existingIssues = getIssueFinder().check(program);
        Set<String> names = new HashSet<>();
        for (Issue issue : existingIssues) {
            // The issue's code location contains a StringLiteral - find it and extract text
            ASTNode node = issue.getCodeLocation();
            if (node != null) {
                findStringLiteralNames(node, names);
            }
        }
        return names;
    }

    private void findStringLiteralNames(ASTNode node, Set<String> names) {
        if (node instanceof StringLiteral literal) {
            names.add(literal.getText());
        }
        for (ASTNode child : node.getChildren()) {
            findStringLiteralNames(child, names);
        }
    }

    private boolean isInStringContext(ASTNode node) {
        ASTNode current = node;
        while (current != null) {
            ASTNode parent = current.getParentNode();
            if (parent == null) {
                return false;
            }

            // Pass-through: AsString is a wrapper, continue checking its parent
            if (parent instanceof AsString) {
                current = parent;
                continue;
            }

            // Check for excluded (strict type) contexts first
            if (isExcludedContext(parent)) {
                return false;
            }

            // Check various string-accepting contexts
            if (isSpriteLookContext(parent)
                    || isStringOperatorContext(parent)
                    || isComparisonContext(parent)
                    || isListOperationContext(parent, current)
                    || isVariableAssignmentContext(parent, current)) {
                return true;
            }

            current = parent;
        }
        return false;
    }

    /**
     * Checks if parent is a SpriteLook statement that accepts strings.
     */
    private boolean isSpriteLookContext(ASTNode parent) {
        return parent instanceof Say
                || parent instanceof SayForSecs
                || parent instanceof Think
                || parent instanceof ThinkForSecs
                || parent instanceof AskAndWait;
    }

    /**
     * Checks if parent is a string operator or string function.
     */
    private boolean isStringOperatorContext(ASTNode parent) {
        return parent instanceof Join
                || parent instanceof LetterOf
                || parent instanceof LengthOfString
                || parent instanceof StringContains;
    }

    /**
     * Checks if parent is a comparison operator (which accepts strings).
     */
    private boolean isComparisonContext(ASTNode parent) {
        return parent instanceof Equals
                || parent instanceof BiggerThan
                || parent instanceof LessThan;
    }

    /**
     * Checks if parent is a list operation with the node in a string-accepting position.
     */
    private boolean isListOperationContext(ASTNode parent, ASTNode current) {
        if (parent instanceof AddTo addTo) {
            return addTo.getString() == current;
        }
        if (parent instanceof InsertAt insertAt) {
            return insertAt.getString() == current;
        }
        if (parent instanceof ReplaceItem replaceItem) {
            return replaceItem.getString() == current;
        }
        return false;
    }

    /**
     * Checks if parent is a variable assignment with the node in the value position.
     */
    private boolean isVariableAssignmentContext(ASTNode parent, ASTNode current) {
        if (parent instanceof SetVariableTo setStmt) {
            return setStmt.getExpr() == current;
        }
        return false;
    }

    /**
     * Checks if parent requires a strict type (Variable/DataExpr) and would fail with StringLiteral.
     */
    private boolean isExcludedContext(ASTNode parent) {
        return parent instanceof AttributeFromVariable
                || parent instanceof Qualified
                || parent instanceof DeclarationIdentAsTypeStmt;
    }


    /**
     * Checks if the node is inside a Message node (used in broadcast blocks).
     * The VariableAsLiteral finder has visit(Message) as a no-op, so it won't detect
     * StringLiterals inside Message nodes.
     */
    private boolean isInsideMessage(ASTNode node) {
        ASTNode current = node.getParentNode();
        while (current != null) {
            if (current instanceof de.uni_passau.fim.se2.litterbox.ast.model.Message) {
                return true;
            }
            current = current.getParentNode();
        }
        return false;
    }

    /**
     * Checks if the expression contains the target identifier type directly or wrapped.
     */
    protected Optional<StringLiteral> checkAndCreateReplacementGeneric(Expression expr) {
        Optional<StringLiteral> direct = extractIdentifierName(expr);
        if (direct.isPresent()) {
            return direct;
        }
        if (expr instanceof AsString stringExpr) {
            return checkAndCreateReplacementGeneric(stringExpr.getOperand1());
        }
        return Optional.empty();
    }

    /**
     * Extract the identifier name from an expression if it matches the target type.
     * Subclasses implement this to check for Variable or Parameter.
     */
    protected abstract Optional<StringLiteral> extractIdentifierName(Expression expr);
}
