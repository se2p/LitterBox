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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.util.StringContextAnalyzer;

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
        // Check string context unless subclass opts out (e.g., ParameterAsLiteralInjector)
        if (shouldCheckStringContext() && !isInStringContext(node)) {
            return;
        }

        // Skip nodes inside Message (broadcast messages) - finder doesn't descend into those
        if (isInsideMessage(node)) {
            return;
        }

        if (injectedNodes.contains(node)) {
            return;
        }
        injectedNodes.add(node);

        Optional<StringLiteral> replacement = checkAndCreateReplacementGeneric(node);
        if (replacement.isPresent()) {
            String literalName = replacement.get().getText();

            // Skip if the bug already exists for this name (precondition)
            if (existingIssueNames.contains(literalName)) {
                return;
            }

            replaceNodeImmutably(node, n -> new StringLiteral(literalName));
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

    protected boolean isInStringContext(ASTNode node) {
        return StringContextAnalyzer.isInStringContext(node);
    }

    /**
     * Checks if the node is inside a Message node (used in broadcast blocks).
     * The VariableAsLiteral finder has visit(Message) as a no-op, so it won't detect
     * StringLiterals inside Message nodes.
     */
    private boolean isInsideMessage(ASTNode node) {
        ASTNode current = node.getParentNode();
        while (current != null) {
            if (current instanceof Message) {
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
