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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.Optional;

/**
 * Abstract base class for injectors that replace identifier references (Variable or Parameter)
 * with string literals of their names.
 */
public abstract class AbstractAsLiteralInjector extends AbstractBugInjector {

    /**
     * Functional interface for creating replacement statements.
     */
    @FunctionalInterface
    protected interface NodeReplacer {
        Stmt replace(Stmt clonedStmt, StringLiteral newLiteral);
    }

    @Override
    public void visit(Say node) {
        Optional<StringLiteral> replacement = checkAndCreateReplacement(node.getString());
        replacement.ifPresent(stringLiteral -> injectMutation(node, (clonedStmt, lit) -> {
            Say say = (Say) clonedStmt;
            return new Say(lit, say.getMetadata());
        }, stringLiteral.getText()));
        super.visit(node);
    }

    @Override
    public void visit(SayForSecs node) {
        Optional<StringLiteral> replacement = checkAndCreateReplacement(node.getString());
        replacement.ifPresent(stringLiteral -> injectMutation(node, (clonedStmt, lit) -> {
            SayForSecs sayForSecs = (SayForSecs) clonedStmt;
            return new SayForSecs(lit, sayForSecs.getSecs(), sayForSecs.getMetadata());
        }, stringLiteral.getText()));
        super.visit(node);
    }

    @Override
    public void visit(Think node) {
        Optional<StringLiteral> replacement = checkAndCreateReplacement(node.getThought());
        replacement.ifPresent(stringLiteral -> injectMutation(node, (clonedStmt, lit) -> {
            Think think = (Think) clonedStmt;
            return new Think(lit, think.getMetadata());
        }, stringLiteral.getText()));
        super.visit(node);
    }

    @Override
    public void visit(ThinkForSecs node) {
        Optional<StringLiteral> replacement = checkAndCreateReplacement(node.getThought());
        replacement.ifPresent(stringLiteral -> injectMutation(node, (clonedStmt, lit) -> {
            ThinkForSecs thinkForSecs = (ThinkForSecs) clonedStmt;
            return new ThinkForSecs(lit, thinkForSecs.getSecs(), thinkForSecs.getMetadata());
        }, stringLiteral.getText()));
        super.visit(node);
    }

    @Override
    public void visit(AskAndWait node) {
        Optional<StringLiteral> replacement = checkAndCreateReplacement(node.getQuestion());
        replacement.ifPresent(stringLiteral -> injectMutation(node, (clonedStmt, lit) -> {
            AskAndWait ask = (AskAndWait) clonedStmt;
            return new AskAndWait(lit, ask.getMetadata());
        }, stringLiteral.getText()));
        super.visit(node);
    }

    @Override
    public void visit(SetVariableTo node) {
        Optional<StringLiteral> replacement = checkAndCreateReplacementGeneric(node.getExpr());
        replacement.ifPresent(stringLiteral -> injectMutation(node, (clonedStmt, lit) -> {
            SetVariableTo set = (SetVariableTo) clonedStmt;
            return new SetVariableTo(set.getIdentifier(), lit, set.getMetadata());
        }, stringLiteral.getText()));
        super.visit(node);
    }

    /**
     * Checks if the string expression contains the target identifier type (e.g., Variable or Parameter)
     * wrapped in AsString, and creates a replacement StringLiteral if found.
     *
     * @param expr The string expression to check
     * @return Optional containing the replacement literal, or empty if no match
     */
    protected Optional<StringLiteral> checkAndCreateReplacement(StringExpr expr) {
        if (expr instanceof AsString asString) {
            return extractIdentifierName(asString.getOperand1());
        }
        return Optional.empty();
    }

    /**
     * Checks if the expression contains the target identifier type directly or wrapped.
     *
     * @param expr The expression to check
     * @return Optional containing the replacement literal, or empty if no match
     */
    protected Optional<StringLiteral> checkAndCreateReplacementGeneric(Expression expr) {
        Optional<StringLiteral> direct = extractIdentifierName(expr);
        if (direct.isPresent()) {
            return direct;
        }
        if (expr instanceof StringExpr stringExpr) {
            return checkAndCreateReplacement(stringExpr);
        }
        return Optional.empty();
    }

    /**
     * Extract the identifier name from an expression if it matches the target type.
     * Subclasses implement this to check for Variable or Parameter.
     *
     * @param expr The expression to check
     * @return Optional containing a StringLiteral with the identifier name, or empty if no match
     */
    protected abstract Optional<StringLiteral> extractIdentifierName(Expression expr);

    /**
     * Helper method to inject a mutation that replaces a statement with a new one containing a string literal.
     */
    protected void injectMutation(Stmt originalNode, NodeReplacer replacer, String literalValue) {
        mutateClone(originalNode, clonedNode -> {
            StringLiteral literal = new StringLiteral(literalValue);

            Stmt newStmt = replacer.replace(clonedNode, literal);

            ASTNode parent = (clonedNode).getParentNode();
            if (parent instanceof StmtList stmtList) {
                int index = stmtList.getStmts().indexOf(clonedNode);
                if (index != -1) {
                    newStmt.setParentNode(stmtList);
                    literal.setParentNode(newStmt);
                    stmtList.getStmts().set(index, newStmt);
                }
            }
        });
    }
}
