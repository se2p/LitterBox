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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

class GeneratePathTaskTest implements JsonTest {

    final static String CAT_PATHS = "cat 39,625791294,Hi! 39,1493538624,Show Hi!,-547448667,Show";
    final static String ABBY_PATHS = "abby GreenFlag,-2069003229,Hello!";
    final static String STAGE_PATHS = "stage GreenFlag,1809747443,10";

    /**
     * the only sprite that has a script contains more than one leaf: Cat
     */
    final static String CAT_SCRIPT_PATH = "Hi!,-547448667,Show";

    @Test
    void testCreateContextEmptyProgram() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        PathGenerator pathGenerator = PathGeneratorFactory.createPathGenerator(true, false, 8, true, program);
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);
        String pathContextForCode2Vec = generatePathTask.createContextForCode2Vec().collect(Collectors.joining());
        assertThat(pathContextForCode2Vec).isEmpty();
    }

    @ParameterizedTest(name = "{displayName} [{index}] includeStage={0}")
    @ValueSource(booleans = {true, false})
    void testCreateContextForCode2Vec(boolean includeStage) throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        PathGenerator pathGenerator = PathGeneratorFactory.createPathGenerator(false, false, 8, includeStage, program);
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);
        List<String> pathContextsForCode2Vec = generatePathTask.createContextForCode2Vec().collect(Collectors.toList());

        if (includeStage) {
            assertThat(pathContextsForCode2Vec).hasSize(3);
        } else {
            assertThat(pathContextsForCode2Vec).hasSize(2);
        }

        assertThat(pathContextsForCode2Vec).contains(CAT_PATHS);
        assertThat(pathContextsForCode2Vec).contains(ABBY_PATHS);

        if (includeStage) {
            assertThat(pathContextsForCode2Vec).contains(STAGE_PATHS);
        } else {
            assertThat(pathContextsForCode2Vec).doesNotContain(STAGE_PATHS);
        }
    }

    @ParameterizedTest(name = "{displayName} [{index}] includeStage={0}")
    @ValueSource(booleans = {true, false})
    void testCreateContextForCode2VecPerScripts(boolean includeStage) throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        PathGenerator pathGenerator = PathGeneratorFactory.createPathGenerator(false, true, 8, includeStage, program);
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);
        List<String> pathContextsForCode2Vec = generatePathTask.createContextForCode2Vec().collect(Collectors.toList());

        // temp sol to remove the Script name TODO clean that up when changing createContextForCode2Vec return type
        List<String> pathContextsForCode2Vec2 = new ArrayList<>();
        for (String token : pathContextsForCode2Vec) {
            int x= token.indexOf(" ");
            pathContextsForCode2Vec2.add(token.substring(x+1));
        }


        if (includeStage) {
            assertThat(pathContextsForCode2Vec2).hasSize(1);
        } else {
            assertThat(pathContextsForCode2Vec2).hasSize(1);
        }

        assertThat(pathContextsForCode2Vec2).contains(CAT_SCRIPT_PATH);

    }
}
