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
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GeneratePathTaskTest implements JsonTest {

    final static String CAT_PATHS = "cat 39,625791294,Hi! 39,1493538624,Show Hi!,-547448667,Show";
    final static String ABBY_PATHS = "abby GreenFlag,-2069003229,Hello!";
    final static String STAGE_PATHS = "stage GreenFlag,1809747443,10";

    @Test
    void testCreateContextEmptyProgram() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        GeneratePathTask generatePathTask = new GeneratePathTask(program, 8, true, true);
        String pathContextForCode2Vec = generatePathTask.createContextForCode2Vec();
        assertEquals("", pathContextForCode2Vec);
    }

    @ParameterizedTest(name = "{displayName} [{index}] includeStage={0}")
    @ValueSource(booleans = {true, false})
    void testCreateContextForCode2Vec(boolean includeStage) throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        GeneratePathTask generatePathTask = new GeneratePathTask(program, 8, includeStage, false);

        String pathContextForCode2Vec = generatePathTask.createContextForCode2Vec();
        String[] outputLines = pathContextForCode2Vec.split("\n");

        if (includeStage) {
            assertEquals(3, outputLines.length);
        } else {
            assertEquals(2, outputLines.length);
        }

        assertTrue(pathContextForCode2Vec.contains(CAT_PATHS));
        assertTrue(pathContextForCode2Vec.contains(ABBY_PATHS));

        if (includeStage) {
            assertTrue(pathContextForCode2Vec.contains(STAGE_PATHS));
        } else {
            assertFalse(pathContextForCode2Vec.contains(STAGE_PATHS));
        }
    }
}
