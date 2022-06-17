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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

import java.util.ArrayList;

public class EmptyCloneVisitor extends CloneVisitor {

    /**
     * Default implementation of visit method for {@link Equals}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Equals Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Equals node) {
        return new Equals(new StringLiteral(""), new StringLiteral(""), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LessThan}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LessThan Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LessThan node) {
        return new LessThan(new StringLiteral(""), new StringLiteral(""), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link BiggerThan}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node BiggerThan Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(BiggerThan node) {
        return new BiggerThan(new StringLiteral(""), new StringLiteral(""), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IfElseStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node IfElseStmt Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(IfElseStmt node) {
        return new IfElseStmt(new UnspecifiedBoolExpr(), new StmtList(new ArrayList<>()), new StmtList(new ArrayList<>()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IfThenStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node IfThenStmt Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(IfThenStmt node) {
        return new IfThenStmt(new UnspecifiedBoolExpr(), new StmtList(new ArrayList<>()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link WaitUntil}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node WaitUntil Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(WaitUntil node) {
        return new WaitUntil(new UnspecifiedBoolExpr(), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link UntilStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node UntilStmt Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(UntilStmt node) {
        return new UntilStmt(new UnspecifiedBoolExpr(), new StmtList(new ArrayList<>()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Not}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Not Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Not node) {
        return new Not(new UnspecifiedBoolExpr(), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link And}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node And Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(And node) {
        return new And(new UnspecifiedBoolExpr(), new UnspecifiedBoolExpr(), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Or}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node And Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Or node) {
        return new Or(new UnspecifiedBoolExpr(), new UnspecifiedBoolExpr(), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RepeatForeverStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RepeatForeverStmt Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RepeatForeverStmt node) {
        return new RepeatForeverStmt(new StmtList(new ArrayList<>()), apply(node.getMetadata()));
    }
}
