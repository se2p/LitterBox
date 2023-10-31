/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.tokenizer;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.MaskingStrategy;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.MaskingType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;

import java.util.List;

public class StatementLevelTokenizer extends AbstractTokenizer {

    private StatementLevelTokenizer(final ProcedureDefinitionNameMapping procedureNameMapping,
                                    final boolean abstractTokens,
                                    final MaskingStrategy maskingStrategy) {
        super(procedureNameMapping, abstractTokens, maskingStrategy);
    }

    public static List<String> tokenize(final Program program,
                                        final ASTNode node,
                                        final boolean abstractTokens,
                                        final MaskingStrategy maskingStrategy) {
        return tokenize(program.getProcedureMapping(), node, abstractTokens, maskingStrategy);
    }

    private static List<String> tokenize(
            final ProcedureDefinitionNameMapping procedureNameMapping,
            final ASTNode node,
            final boolean abstractTokens,
            final MaskingStrategy maskingStrategy
    ) {
        final StatementLevelTokenizer v =
                new StatementLevelTokenizer(procedureNameMapping, abstractTokens, maskingStrategy);
        node.accept(v);
        return v.getTokens();
    }

    private String getStatementId(final ASTNode node) {
        if (node.getMetadata() instanceof DataBlockMetadata block) {
            return block.getBlockId();
        } else if (node.getMetadata() instanceof NonDataBlockMetadata block) {
            return block.getBlockId();
        }

        return null;
    }

    private void visitControlBlock(final ASTNode node, final Token opcode) {
        if (MaskingType.Statement.equals(getMaskingStrategy().getMaskingType())
                && getMaskingStrategy().getBlockId().equals(getStatementId(node))) {
            addToken(Token.MASK);
        } else {
            addToken(opcode);
            visitChildren(node);
        }
    }

    @Override
    protected void visit(final ASTNode node, final Token opcode) {
        if (MaskingType.Statement.equals(getMaskingStrategy().getMaskingType())
                && getMaskingStrategy().getBlockId().equals(getStatementId(node))) {
            addToken(Token.MASK);
        } else {
            addToken(opcode);
        }
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        visitControlBlock(node, Token.CONTROL_REPEAT);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        visitControlBlock(node, Token.CONTROL_FOREVER);
    }

    @Override
    public void visit(IfThenStmt node) {
        visitControlBlock(node, Token.CONTROL_IF);
    }

    @Override
    public void visit(IfElseStmt node) {
        visitControlBlock(node, Token.CONTROL_IF_ELSE);
    }

    @Override
    public void visit(UntilStmt node) {
        visitControlBlock(node, Token.CONTROL_REPEAT_UNTIL);
    }

    @Override
    public void visit(StopAll node) {
        visitStop(node);
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        visitStop(node);
    }

    @Override
    public void visit(StopThisScript node) {
        visitStop(node);
    }

    private void visitStop(ASTNode node) {
        visit(node, Token.CONTROL_STOP);
    }
}
