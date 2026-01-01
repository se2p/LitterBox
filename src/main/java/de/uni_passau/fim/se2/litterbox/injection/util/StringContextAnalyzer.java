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
package de.uni_passau.fim.se2.litterbox.injection.util;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
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

import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Utility class to determine if an AST node is in a context where a String value is valid.
 *
 * <p>
 * This class uses a table-driven approach with four categories:
 * <ul>
 *   <li>{@link #ACCEPTS_STRING_ANY_POSITION} - Contexts that accept strings in any child position</li>
 *   <li>{@link #ACCEPTS_STRING_SPECIFIC_POSITION} - Contexts that accept strings only in specific positions</li>
 *   <li>{@link #EXCLUDED_CONTEXTS} - Type-strict contexts that never accept strings</li>
 *   <li>{@link #PASS_THROUGH_WRAPPERS} - Wrapper types to skip when traversing</li>
 * </ul>
 */
public class StringContextAnalyzer {

    private StringContextAnalyzer() {
        // Utility class
    }

    // ==================== String-Accepting Contexts ====================

    /**
     * Contexts that accept strings in ANY child position.
     * These include sprite look blocks, string operators, and comparisons.
     */
    private static final Set<Class<? extends ASTNode>> ACCEPTS_STRING_ANY_POSITION = Set.of(
            // Sprite look (say, think, ask)
            Say.class, SayForSecs.class, Think.class, ThinkForSecs.class, AskAndWait.class,
            // String operators
            Join.class, LetterOf.class, LengthOfString.class, StringContains.class,
            // Comparisons accept any type
            Equals.class, BiggerThan.class, LessThan.class
    );

    /**
     * Contexts that accept strings only in a SPECIFIC child position.
     * The BiPredicate checks if the child is in the string-accepting position.
     */
    private static final Map<Class<? extends ASTNode>, BiPredicate<ASTNode, ASTNode>>
            ACCEPTS_STRING_SPECIFIC_POSITION = Map.of(
            AddTo.class, (parent, child) -> ((AddTo) parent).getString() == child,
            InsertAt.class, (parent, child) -> ((InsertAt) parent).getString() == child,
            ReplaceItem.class, (parent, child) -> ((ReplaceItem) parent).getString() == child,
            SetVariableTo.class, (parent, child) -> ((SetVariableTo) parent).getExpr() == child
    );

    // ==================== Excluded Contexts ====================

    /**
     * Contexts that NEVER accept strings (type-strict contexts).
     * If we encounter one of these while traversing up, return false immediately.
     */
    private static final Set<Class<? extends ASTNode>> EXCLUDED_CONTEXTS = Set.of(
            AttributeFromVariable.class,
            Qualified.class,
            DeclarationIdentAsTypeStmt.class
    );

    /**
     * Pass-through wrappers - if parent is one of these, continue checking grandparent.
     * These are type conversion nodes that don't impose type constraints.
     */
    private static final Set<Class<? extends ASTNode>> PASS_THROUGH_WRAPPERS = Set.of(
            AsString.class
    );

    // ==================== Main Logic ====================

    /**
     * Checks if the node is in a context that accepts a String value.
     * This traverses up the AST to check parent nodes.
     *
     * @param node The node to check context for
     * @return true if the node is in a string-accepting context
     */
    public static boolean isInStringContext(ASTNode node) {
        ASTNode current = node;
        while (current != null) {
            ASTNode parent = current.getParentNode();
            if (parent == null) {
                return false;
            }

            Class<? extends ASTNode> parentClass = parent.getClass();

            // Pass-through: continue checking grandparent
            if (PASS_THROUGH_WRAPPERS.contains(parentClass)) {
                current = parent;
                continue;
            }

            // Excluded: early exit
            if (EXCLUDED_CONTEXTS.contains(parentClass)) {
                return false;
            }

            // Accepts string in any position
            if (ACCEPTS_STRING_ANY_POSITION.contains(parentClass)) {
                return true;
            }

            // Accepts string in specific position
            BiPredicate<ASTNode, ASTNode> positionChecker =
                    ACCEPTS_STRING_SPECIFIC_POSITION.get(parentClass);
            if (positionChecker != null && positionChecker.test(parent, current)) {
                return true;
            }

            current = parent;
        }
        return false;
    }
}
