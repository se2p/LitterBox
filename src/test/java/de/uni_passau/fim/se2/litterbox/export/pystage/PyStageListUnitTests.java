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
package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.DeleteOf;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class PyStageListUnitTests {

    @Test
    public void testDeleteAllOfList_withAsNumberWrapper() {
        // Construct: delete (AsNumber("all")) of "myList"
        DeleteOf deleteNode = new DeleteOf(
            new AsNumber(new StringLiteral("all")),
            new StrId("myList"),
            new NoBlockMetadata()
        );

        String result = PyStageVisitor.of(deleteNode);

        // Expect: self.set_variable("myList", [])
        assertThat(result.trim()).isEqualTo("self.set_variable(\"myList\", [])");
    }
}
