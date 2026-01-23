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
package de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

class PathGenerationVisitorTest implements JsonTest {

    @ParameterizedTest
    @ValueSource(
        strings = {
            "src/test/fixtures/allBlocks.json",
            "src/test/fixtures/customBlocks.json",
            "src/test/fixtures/customBlocksWithParams.json",
            "src/test/fixtures/customBlockNumTextParameters.json"
        }
    )
    void testAllBlocksVisitable(final String programPath) throws ParsingException {
        final Program program = getAST(programPath);

        assertAll(traverse(program).map(node -> () -> {
            final PathRepresentationVisitor visitor = new PathRepresentationVisitor();
            node.accept(visitor);
            assertTrue(visitor.getRepresentation() > 0);
        }));
    }

    private Stream<ASTNode> traverse(final ASTNode node) {
        return Stream.concat(
            Stream.of(node),
            node.getChildren().stream().flatMap(this::traverse)
        );
    }
}
