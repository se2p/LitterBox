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
package de.uni_passau.fim.se2.litterbox.ast;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScratchNodeFactoryTest {

    private ScratchNodeFactory nodeFactory;

    @BeforeEach
    public void setUp() {
        nodeFactory = new ScratchNodeFactory();
    }

    @Test
    public void testCreateIfThenStmt() {
        IfStmt stmt = nodeFactory.createIf()
                .withCondition(nodeFactory.unspecifiedBool())
                .build();

        assertNotNull(stmt);
        assertTrue(stmt.getBoolExpr() instanceof UnspecifiedBoolExpr);
        assertNotNull(stmt.getThenStmts());
        assertTrue(stmt.getThenStmts().getStmts().isEmpty());
    }

    @Test
    public void testCreateUntilStmt() {
        UntilStmt stmt = nodeFactory.createUntil()
                .withCondition(nodeFactory.unspecifiedBool())
                .build();

        assertNotNull(stmt);
        assertTrue(stmt.getBoolExpr() instanceof UnspecifiedBoolExpr);
        assertNotNull(stmt.getStmtList());
        assertTrue(stmt.getStmtList().getStmts().isEmpty());
    }
}
