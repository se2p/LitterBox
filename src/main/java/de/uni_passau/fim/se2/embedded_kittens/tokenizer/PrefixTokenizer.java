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
package de.uni_passau.fim.se2.embedded_kittens.tokenizer;

import de.uni_passau.fim.se2.embedded_kittens.util.MaskingStrategy;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;

import java.util.List;

/**
 * Like the {@link Tokenizer}, but for binary expressions the prefix order is used.
 *
 * <p>
 * For example, the addition is tokenised as {@code 3 + 4} by the regular {@link Tokenizer}, but tokenised as
 * {@code + 3 4} here. This prefix notation may be helpful for next-token prediction models that use short sequences,
 * e.g. n-gram models.
 */
public class PrefixTokenizer extends Tokenizer {

    protected PrefixTokenizer(
            ProcedureDefinitionNameMapping procedureNameMapping,
            boolean abstractTokens, boolean abstractFixedNodeOptions, boolean parenthesiseAtoms,
            MaskingStrategy maskingStrategy
    ) {
        super(procedureNameMapping, abstractTokens, abstractFixedNodeOptions, parenthesiseAtoms, maskingStrategy);
    }

    public static List<String> tokenize(
            final Program program, final ASTNode node, final boolean abstractTokens,
            final boolean abstractFixedNodeOptions, final MaskingStrategy maskingStrategy
    ) {
        return tokenize(
            program.getProcedureMapping(), node, abstractTokens, abstractFixedNodeOptions, true, maskingStrategy
        );
    }

    /**
     * Does not add parentheses around literals and variable names that occur inside other expressions.
     *
     * <p>
     * For example, in {@code move_steps (2)} the parentheses remain since it is the outermost expression of the
     * argument to the {@code move_steps} block. However, {@code move steps (3 + my_variable)} will be tokenized like
     * {@code move_steps (add 3 5)} without the additional parentheses around {@code 3} and {@code my_variable}.
     *
     * @param program                  Some Scratch program.
     * @param node                     The node inside the program that should be tokenized.
     * @param abstractTokens           True, if literals and names should be replaced by fixed abstract tokens.
     * @param abstractFixedNodeOptions True, if fixed node options should be replaced by a fixed abstract token.
     * @param maskingStrategy          The masking strategy that should be applied during tokenization.
     * @return A sequence of tokens obtained when traversing the AST of {@code node}.
     */
    public static List<String> tokenizeWithReducedParentheses(
            final Program program, final ASTNode node, final boolean abstractTokens,
            final boolean abstractFixedNodeOptions, final MaskingStrategy maskingStrategy
    ) {
        return tokenize(
            program.getProcedureMapping(), node, abstractTokens, abstractFixedNodeOptions, false, maskingStrategy
        );
    }

    private static List<String> tokenize(
            final ProcedureDefinitionNameMapping procedureNameMapping,
            final ASTNode node,
            final boolean abstractTokens,
            final boolean abstractFixedNodeOptions,
            final boolean parenthesiseAtoms,
            final MaskingStrategy maskingStrategy
    ) {
        final PrefixTokenizer v = new PrefixTokenizer(
                procedureNameMapping, abstractTokens, abstractFixedNodeOptions, parenthesiseAtoms, maskingStrategy
        );
        node.accept(v);
        return v.getTokens();
    }

    @Override
    protected void visitExpression(final Expression expr) {
        if (parenthesiseExpressions) {
            super.visitExpression(expr);
        } else {
            expr.accept(this);
        }
    }

    @Override
    protected void visitBinaryOperator(BinaryExpression<?, ?> node, Token opcode) {
        if (shouldBeMasked(node)) {
            addToken(Token.MASK);
        } else {
            addToken(opcode);
            visitChild(node.getOperand1());
            visitChild(node.getOperand2());
        }
    }
}
