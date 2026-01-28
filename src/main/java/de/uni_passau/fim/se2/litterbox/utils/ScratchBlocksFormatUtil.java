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
package de.uni_passau.fim.se2.litterbox.utils;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

/**
 * Utility methods for formatting AST nodes as scratchblocks notation.
 * These methods wrap ScratchBlocksVisitor output with appropriate tags for display.
 */
public final class ScratchBlocksFormatUtil {

    private ScratchBlocksFormatUtil() {
        // Utility class
    }

    /**
     * Wraps a Script in scratchblocks tags.
     *
     * @param node the script
     * @return formatted scratchblocks string
     */
    public static String wrappedScratchBlocks(Script node) {
        return "[scratchblocks]\n" + ScratchBlocksVisitor.of(node) + "[/scratchblocks]";
    }

    /**
     * Wraps a ProcedureDefinition in scratchblocks tags.
     *
     * @param node the procedure
     * @return formatted scratchblocks string
     */
    public static String wrappedScratchBlocks(ProcedureDefinition node) {
        return "[scratchblocks]\n" + ScratchBlocksVisitor.of(node) + "[/scratchblocks]";
    }

    /**
     * Wraps a StmtList in scratchblocks tags.
     *
     * @param node the statement list
     * @return formatted scratchblocks string
     */
    public static String wrappedScratchBlocks(StmtList node) {
        return "[scratchblocks]\n" + ScratchBlocksVisitor.of(node) + "[/scratchblocks]";
    }

    /**
     * Wraps a statement in inline scratchblocks tags.
     *
     * @param node the statement
     * @return formatted scratchblocks string
     */
    public static String wrappedScratchBlocks(Stmt node) {
        return "[sbi]" + ScratchBlocksVisitor.of(node) + "[/sbi]";
    }

    /**
     * Wraps an expression in inline scratchblocks tags.
     *
     * @param node the expression
     * @return formatted scratchblocks string
     */
    public static String wrappedScratchBlocks(Expression node) {
        return "[sbi]" + ScratchBlocksVisitor.of(node) + "[/sbi]";
    }

    /**
     * Wraps a variable in variable tags.
     *
     * @param node the variable
     * @return formatted variable string
     */
    public static String wrappedScratchBlocks(Variable node) {
        return "[var]" + ScratchBlocksVisitor.of(node) + "[/var]";
    }

    /**
     * Wraps a number literal in inline scratchblocks tags with grey ring.
     *
     * @param node the number literal
     * @return formatted scratchblocks string
     */
    public static String wrappedScratchBlocks(NumberLiteral node) {
        return "[sbi]<" + ScratchBlocksVisitor.of(node) + " :: grey ring>[/sbi]";
    }

    /**
     * Wraps a string literal in inline scratchblocks tags with context-appropriate styling.
     *
     * @param node the string literal
     * @param inLookStmt whether inside a look statement
     * @param inSoundStmt whether inside a sound statement
     * @param inBroadcastStmt whether inside a broadcast statement
     * @return formatted scratchblocks string
     */
    public static String wrappedScratchBlocks(StringLiteral node, boolean inLookStmt,
                                               boolean inSoundStmt, boolean inBroadcastStmt) {
        if (inLookStmt) {
            return "[sbi]<" + ScratchBlocksVisitor.of(node) + " :: looks ring>[/sbi]";
        } else if (inSoundStmt) {
            return "[sbi]<" + ScratchBlocksVisitor.of(node) + " :: sound ring>[/sbi]";
        } else if (inBroadcastStmt) {
            return "[sbi]<" + ScratchBlocksVisitor.of(node) + " :: control ring>[/sbi]";
        } else {
            return "[sbi]<" + ScratchBlocksVisitor.of(node) + " :: grey ring>[/sbi]";
        }
    }

    /**
     * Wraps a color literal in inline scratchblocks tags with grey ring.
     *
     * @param node the color literal
     * @return formatted scratchblocks string
     */
    public static String wrappedScratchBlocks(ColorLiteral node) {
        return "[sbi]<" + ScratchBlocksVisitor.of(node) + " :: grey ring>[/sbi]";
    }

    /**
     * Creates a simplified version of a control statement (without body).
     * Useful for displaying just the control structure header.
     *
     * @param stmt the statement
     * @return simplified statement with empty body
     */
    public static Stmt getSingleStmt(Stmt stmt) {
        return switch (stmt) {
            case IfThenStmt ifThenStmt ->
                    new IfThenStmt(ifThenStmt.getBoolExpr(), new StmtList(), ifThenStmt.getMetadata());
            case IfElseStmt ifElseStmt ->
                    new IfElseStmt(ifElseStmt.getBoolExpr(), new StmtList(), new StmtList(), ifElseStmt.getMetadata());
            case RepeatTimesStmt repeatTimesStmt ->
                    new RepeatTimesStmt(repeatTimesStmt.getTimes(), new StmtList(), repeatTimesStmt.getMetadata());
            case RepeatForeverStmt repeatForeverStmt ->
                    new RepeatForeverStmt(new StmtList(), repeatForeverStmt.getMetadata());
            case UntilStmt untilStmt ->
                    new UntilStmt(untilStmt.getBoolExpr(), new StmtList(), untilStmt.getMetadata());
            default -> stmt;
        };
    }
}
