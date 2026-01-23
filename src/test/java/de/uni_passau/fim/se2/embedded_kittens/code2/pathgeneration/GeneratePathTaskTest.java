/*
 * Copyright (C) 2021-2024 EmbeddedKittens contributors
 *
 * This file is part of EmbeddedKittens.
 *
 * EmbeddedKittens is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * EmbeddedKittens is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EmbeddedKittens. If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-FileCopyrightText: 2021-2024 EmbeddedKittens contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration;

import static com.google.common.truth.Truth.assertThat;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.uni_passau.fim.se2.embedded_kittens.JsonTest;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.program_relation.ProgramRelation;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.program_relation.ProgramRelationFactory;
import de.uni_passau.fim.se2.embedded_kittens.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.ProcedureCount;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.ScriptCount;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

class GeneratePathTaskTest implements JsonTest {

    private final ProgramRelationFactory programRelationFactory = ProgramRelationFactory.withHashCodeFactory();
    static final String CAT_PATHS = "cat 39,625791294,hi_! 39,1493538624,Show hi_!,-547448667,Show";
    static final String ABBY_PATHS = "abby GreenFlag,-2069003229,hello_!";
    static final String STAGE_PATHS = "stage GreenFlag,1809747443,10";

    /**
     * the only sprite that has a script contains more than one leaf: Cat
     */
    static final List<String> CAT_SCRIPT_PATHS = List.of(
        "39,625791294,hi_!",
        "39,1493538624,Show",
        "hi_!,-547448667,Show",
        "GreenFlag,-2069003229,hello_!"
    );

    @Test
    void testCreateContextEmptyProgram() throws ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        PathGenerator pathGenerator = getPathGenerator(program, true);
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);
        List<ProgramFeatures> pathContextForCode2Vec = generatePathTask.createContext();
        assertThat(pathContextForCode2Vec).isEmpty();
    }

    @ParameterizedTest(name = "{displayName} [{index}] includeStage={0}")
    @ValueSource(booleans = { true, false })
    void testCreateContextForCode2Vec(boolean includeStage) throws ParsingException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        PathGenerator pathGenerator = getPathGenerator(program, includeStage);
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);
        List<ProgramFeatures> pathContextsForCode2Vec = generatePathTask.createContext();

        if (includeStage) {
            assertThat(pathContextsForCode2Vec).hasSize(3);
        }
        else {
            assertThat(pathContextsForCode2Vec).hasSize(2);
        }

        List<String> pathContexts = pathContextsForCode2Vec
            .stream()
            .map(ProgramFeatures::toString)
            .toList();

        assertThat(pathContexts).contains(CAT_PATHS);
        assertThat(pathContexts).contains(ABBY_PATHS);

        if (includeStage) {
            assertThat(pathContexts).contains(STAGE_PATHS);
        }
        else {
            assertThat(pathContexts).doesNotContain(STAGE_PATHS);
        }
    }

    @ParameterizedTest(name = "{displayName} [{index}] includeStage={0}")
    @ValueSource(booleans = { true, false })
    void testCreateContextCustomProcedures(boolean includeStage) throws ParsingException {
        Program program = getAST("src/test/fixtures/ml_preprocessing/shared/custom_blocks_simple.json");
        PathGenerator pathGenerator = getPathGenerator(program, includeStage);
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);

        List<ProgramFeatures> pathContextsForCode2Vec = generatePathTask.createContext();
        assertThat(pathContextsForCode2Vec).hasSize(1);
        List<ProgramRelation> programRelations = pathContextsForCode2Vec.getFirst().getFeatures();

        assertThat(programRelations).hasSize(11);

        List<String> relations = programRelations.stream().map(ProgramRelation::toString).toList();
        assertThat(relations).containsAtLeast(
            "testblock_b,-826963864,input_param",
            "testblock_b,-1701860098,BooleanType",
            "input_param,2024539361,BooleanType"
        );
    }

    @ParameterizedTest(name = "{displayName} [{index}] includeStage={0}")
    @ValueSource(booleans = { true, false })
    void testCreateContextForCode2VecPerScripts(boolean includeStage) throws ParsingException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        PathGenerator pathGenerator = getPathGenerator(program, includeStage);
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);
        List<ProgramFeatures> pathContextsForCode2Vec = generatePathTask.createContext();

        List<String> pathContexts = pathContextsForCode2Vec
            .stream()
            .flatMap(features -> features.getFeatures().stream())
            .map(ProgramRelation::toString)
            .toList();

        if (includeStage) {
            assertThat(pathContexts).hasSize(5);
            assertThat(pathContexts).contains("GreenFlag,1809747443,10");
        }
        else {
            assertThat(pathContexts).hasSize(4);
        }

        assertThat(pathContexts).containsAtLeastElementsIn(CAT_SCRIPT_PATHS);
    }

    @ParameterizedTest(name = "{displayName} [{index}] includeStage={0}")
    @ValueSource(booleans = { true, false })
    void testCreateContextForCode2VecPerScriptsCountForProgramWithOnlyValidScripts(boolean includeStage)
        throws ParsingException {
        Program program = getAST("src/test/fixtures/bugsPerScripts/random_project.json");
        PathGenerator pathGenerator = getScriptPathGenerator(program, includeStage);
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);
        List<ProgramFeatures> pathContextsForCode2Vec = generatePathTask.createContext();
        List<String> pathContexts = pathContextsForCode2Vec
            .stream()
            .map(ProgramFeatures::toStringWithoutNodeName)
            .toList();

        ScriptCount<ASTNode> scriptCount = new ScriptCount<>();
        int scriptCountPerProgram = (int) scriptCount.calculateMetric(program);

        ProcedureCount<ASTNode> procedureCount = new ProcedureCount<>();
        int procedureCountPerProgram = (int) procedureCount.calculateMetric(program);

        assertThat(pathContexts).hasSize(scriptCountPerProgram + procedureCountPerProgram);
    }

    private PathGenerator getPathGenerator(final Program program, final boolean includeStage) {
        return PathGeneratorFactory.createPathGenerator(
            PathType.SPRITE, 8, includeStage,
            program, false, programRelationFactory, ActorNameNormalizer.getDefault()
        );
    }

    private PathGenerator getScriptPathGenerator(final Program program, final boolean includeStage) {
        return PathGeneratorFactory.createPathGenerator(
            PathType.SCRIPT, 8, includeStage,
            program, false, programRelationFactory, ActorNameNormalizer.getDefault()
        );
    }
}
