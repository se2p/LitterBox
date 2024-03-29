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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PathGeneratorTest implements JsonTest {

    final String[] expectedLeafs = {"loudness", "10", "Hello!", "left-right", "pitch", "100", "draggable",
            "color", "0", "1", "forward", "front", "number", "Size", "1", "2", "log", "year"};

    @Test
    void testGeneratePaths() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        PathGenerator generator = new PathGenerator(program, 8, false, false);
        List<ProgramFeatures> pathContextsPerSprite = generator.generatePaths();
        assertEquals(2, pathContextsPerSprite.size());
        int positionCat = 0;
        int positionAbby = 0;
        if (pathContextsPerSprite.get(0).getName().equals("cat")
                && pathContextsPerSprite.get(1).getName().equals("abby")) {
            positionAbby = 1;
        } else if (pathContextsPerSprite.get(1).getName().equals("cat")
                && pathContextsPerSprite.get(0).getName().equals("abby")) {
            positionCat = 1;
        } else {
            fail();
        }

        // Sprite cat
        ProgramFeatures cat = pathContextsPerSprite.get(positionCat);
        assertEquals("cat", cat.getName());
        assertEquals(3, cat.getFeatures().size());
        assertEquals("39,625791294,Hi!",
                cat.getFeatures().get(0).toString());
        assertEquals("39,1493538624,Show",
                cat.getFeatures().get(1).toString());
        assertEquals("Hi!,-547448667,Show",
                cat.getFeatures().get(2).toString());

        // Sprite abby
        ProgramFeatures abby = pathContextsPerSprite.get(positionAbby);
        assertEquals("abby", abby.getName());
        assertEquals(1, abby.getFeatures().size());
        assertEquals("GreenFlag,-2069003229,Hello!",
                abby.getFeatures().get(0).toString());
    }

    @Test
    void testGeneratePathsWithDifferentTokens() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/allChangeableTokens.json");
        PathGenerator generator = new PathGenerator(program, 8, false, false);
        List<String> tokens = generator.getAllLeafs();
        assertArrayEquals(expectedLeafs, tokens.toArray());
    }

    @ParameterizedTest(name = "{displayName} [{index}] includeStage={0}")
    @ValueSource(booleans = {true, false})
    void testGeneratePathsWholeProgram(boolean includeStage) throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        PathGenerator generator = new PathGenerator(program, 8, includeStage, true);

        List<ProgramFeatures> pathContexts = generator.generatePaths();
        assertEquals(1, pathContexts.size());
        assertEquals("program", pathContexts.get(0).getName());

        int expectedPathCount;
        List<String> expectedPaths = new ArrayList<>(List.of(
                "39,625791294,Hi!",
                "39,1493538624,Show",
                "Hi!,-547448667,Show",
                "GreenFlag,-2069003229,Hello!"
        ));
        if (includeStage) {
            expectedPathCount = 6;
            expectedPaths.add("GreenFlag,272321927,GreenFlag");
            expectedPaths.add("GreenFlag,1809747443,10");
        } else {
            expectedPathCount = 4;
        }

        List<String> actualPaths = pathContexts.get(0).getFeatures()
                .stream().map(ProgramRelation::toString).collect(Collectors.toList());
        assertEquals(expectedPathCount, actualPaths.size());

        for (String expectedPath : expectedPaths) {
            assertTrue(actualPaths.contains(expectedPath), expectedPath);
        }
    }

    @ParameterizedTest(name = "{displayName} [{index}] includeStage={0}, wholeProgram={1}")
    @MethodSource("code2vecOptions")
    void testGeneratePathsEmptyProgram(boolean includeStage, boolean wholeProgram) throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");

        PathGenerator generator = new PathGenerator(program, 8, includeStage, wholeProgram);
        List<ProgramFeatures> features = generator.generatePaths();
        assertTrue(features.isEmpty());
    }

    private static Stream<Arguments> code2vecOptions() {
        return Stream.of(
                Arguments.arguments(true, true),
                Arguments.arguments(true, false),
                Arguments.arguments(false, true),
                Arguments.arguments(false, false)
        );
    }
}
