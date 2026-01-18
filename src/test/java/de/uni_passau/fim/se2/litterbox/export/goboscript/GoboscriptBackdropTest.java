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
package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class GoboscriptBackdropTest {

    @Test
    public void testBackdropSwitchSpecial() {
        List<Stmt> stmtList = new ArrayList<>();
        NoBlockMetadata meta = new NoBlockMetadata();

        // switch backdrop to (previous backdrop)
        stmtList.add(new SwitchBackdrop(
                new Prev(meta),
                meta
        ));

        // switch backdrop to (next backdrop)
        stmtList.add(new SwitchBackdrop(
                new Next(meta),
                meta
        ));

        // switch backdrop to (random backdrop)
        stmtList.add(new SwitchBackdrop(
                new Random(meta),
                meta
        ));

        StmtList stmts = new StmtList(stmtList);
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(stmts);
        String result = visitor.getOutput();

        // This expects the CORRECT Goboscript syntax
        assertThat(result).contains("previous_backdrop;");
        assertThat(result).contains("next_backdrop;");
        assertThat(result).contains("random_backdrop;");
    }
}
