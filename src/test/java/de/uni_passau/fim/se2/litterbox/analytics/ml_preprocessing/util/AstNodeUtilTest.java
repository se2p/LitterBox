
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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AstNodeUtilTest implements JsonTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGetActors(boolean includeStage) throws Exception {
        final Program program = getAST("src/test/fixtures/multipleSprites.json");
        final List<ActorDefinition> actors = AstNodeUtil.getActors(program, includeStage).toList();

        int expectedActorCount = includeStage ? 3 : 2;
        assertThat(actors).hasSize(expectedActorCount);

        if (!includeStage) {
            assertAll(actors.stream().map(actor -> () -> assertThat(actor.isStage()).isFalse()));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGetActorsWithoutDefault(boolean includeStage) throws Exception {
        final Program program = getAST("src/test/fixtures/similarButNotSame.json");
        final List<ActorDefinition> allActors = AstNodeUtil.getActors(program, includeStage).toList();
        final List<ActorDefinition> actors = AstNodeUtil.getActorsWithoutDefaultSprites(program, includeStage).toList();

        int expectedActorCount = includeStage ? 2 : 1;
        assertThat(actors).hasSize(expectedActorCount);
        assertThat(allActors).hasSize(expectedActorCount + 1);

        if (!includeStage) {
            assertAll(actors.stream().map(actor -> () -> assertThat(actor.isStage()).isFalse()));
        }
    }

    @Test
    void testFindActorForActorDef() throws Exception {
        final Program program = getAST("src/test/fixtures/multipleSprites.json");

        assertAll(
                program
                        .getActorDefinitionList()
                        .getDefinitions()
                        .stream()
                        .map(actor -> () -> assertSame(actor, AstNodeUtil.findActor(actor).orElseThrow()))
        );
    }

    @Test
    void testFindActorForProgram() throws Exception {
        final Program program = getAST("src/test/fixtures/multipleSprites.json");
        assertEquals(Optional.empty(), AstNodeUtil.findActor(program));
    }

    @Test
    void testFindActorForProgramMetadata() throws Exception {
        final ProgramMetadata metadata = getAST("src/test/fixtures/multipleSprites.json").getProgramMetadata();
        assertEquals(Optional.empty(), AstNodeUtil.findActor(metadata));
    }

    @Test
    void testFindActorForNode() throws Exception {
        final Program program = getAST("src/test/fixtures/multipleSprites.json");

        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            for (Script script : actor.getScripts().getScriptList()) {
                assertEquals(actor, AstNodeUtil.findActor(script).orElseThrow());
                assertEquals(actor, AstNodeUtil.findActor(script.getEvent()).orElseThrow());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t ", "repl"})
    void testGetProcedureName(String replacement) throws Exception {
        final Program program = getAST("src/test/fixtures/customBlocks.json");
        final List<String> procedureNames = program.getProcedureMapping()
                .getProcedures()
                .values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(ProcedureInfo::getName)
                .map(name -> AstNodeUtil.replaceProcedureParams(name, replacement))
                .collect(Collectors.toList());

        final String nameWithReplacements = String.format("BlockWithInputs %s %s", replacement, replacement).trim();
        assertThat(procedureNames).containsExactly("BlockNoInputs", nameWithReplacements);
    }
}
