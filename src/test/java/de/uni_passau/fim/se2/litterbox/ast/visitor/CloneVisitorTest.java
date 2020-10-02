/*
 * Copyright (C) 2020 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class CloneVisitorTest implements JsonTest {

    @ParameterizedTest
    @ValueSource(strings = {"src/test/fixtures/scratchblocks/motionblocks.json",
            "src/test/fixtures/scratchblocks/touchingedgeblock.json",
            "src/test/fixtures/scratchblocks/multicustomblocks.json",
            "src/test/fixtures/scratchblocks/lookblocks.json",
            "src/test/fixtures/scratchblocks/soundblocks.json",
            "src/test/fixtures/scratchblocks/soundblocks2.json",
            "src/test/fixtures/scratchblocks/sensingblocks.json",
            "src/test/fixtures/scratchblocks/variableblocks.json",
            "src/test/fixtures/scratchblocks/controlblocks.json",
            "src/test/fixtures/scratchblocks/messageblocks.json",
            "src/test/fixtures/scratchblocks/cloneblocks.json",
            "src/test/fixtures/scratchblocks/backdropblocks.json",
            "src/test/fixtures/scratchblocks/timerblocks.json",
            "src/test/fixtures/scratchblocks/mathExprInTimerBlock.json",
            "src/test/fixtures/scratchblocks/arithmeticblocks.json",
            "src/test/fixtures/scratchblocks/booleanblocks.json",
            "src/test/fixtures/scratchblocks/stringblocks.json",
            "src/test/fixtures/scratchblocks/attributeblocks.json",
            "src/test/fixtures/scratchblocks/sensingconditionblocks.json",
            "src/test/fixtures/scratchblocks/listattributeblocks.json",
            "src/test/fixtures/scratchblocks/customblock1.json",
            "src/test/fixtures/scratchblocks/customblock2.json",
            "src/test/fixtures/scratchblocks/customblock3.json",
            "src/test/fixtures/scratchblocks/customblock4.json",
            "src/test/fixtures/scratchblocks/customblock5.json",
            "src/test/fixtures/scratchblocks/penblocks.json",
            "src/test/fixtures/scratchblocks/unconnectedblocks.json",
            "src/test/fixtures/scratchblocks/multipleunconnectedblocks.json",
            "src/test/fixtures/scratchblocks/stopscriptblocks.json",
            "src/test/fixtures/scratchblocks/spriteclickedblocks.json",
            "src/test/fixtures/scratchblocks/variablesinchoiceblocks.json"})
    public void testEquality(String fileName) throws IOException, ParsingException {
        Program program = getAST(fileName);
        CloneVisitor cloneVisitor = new CloneVisitor();
        Program programCopy = cloneVisitor.apply(program);
        assertEqualsButNotSame(program, programCopy);
    }

    private void assertEqualsButNotSame(ASTNode node1, ASTNode node2) {
        assertNotSame(node1, node2);

        List<? extends ASTNode> children1 = node1.getChildren();
        List<? extends ASTNode> children2 = node2.getChildren();
        assertEquals(children1.size(), children2.size());

        for (int i = 0; i < node2.getChildren().size(); i++) {
            assertEqualsButNotSame(children1.get(i), children2.get(i));
        }

        assertEquals(node1, node2, "Found difference for nodes of type "+node1.getClass());
    }
}
