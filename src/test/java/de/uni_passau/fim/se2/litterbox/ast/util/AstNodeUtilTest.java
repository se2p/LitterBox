
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
package de.uni_passau.fim.se2.litterbox.ast.util;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

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
                .toList();

        final String nameWithReplacements = String.format("BlockWithInputs %s %s", replacement, replacement).trim();
        assertThat(procedureNames).containsExactly("BlockNoInputs", nameWithReplacements);
    }

    @Test
    void testGetProcedureBlockId() throws Exception {
        final Program program = getAST("src/test/fixtures/customBlocks.json");
        final Stream<ProcedureDefinition> procedures = program.getActorDefinitionList().getDefinitions().stream()
                .map(ActorDefinition::getProcedureDefinitionList)
                .map(ProcedureDefinitionList::getList)
                .flatMap(Collection::stream);

        assertAll(procedures.map(proc -> () -> assertThat(AstNodeUtil.getBlockId(proc)).isNotNull()));
    }

    @Test
    void testGetProcedureBlockIdOfDefinition() throws Exception {
        final Program program = getAST("src/test/fixtures/customBlocks.json");
        final ProcedureDefinition procedure = program.getActorDefinitionList().getDefinitions().stream()
                .map(ActorDefinition::getProcedureDefinitionList)
                .map(ProcedureDefinitionList::getList)
                .flatMap(Collection::stream)
                .filter(procedureDefinition -> "V-ChQTp}ZbGaPT;Mu5ok".equals(procedureDefinition.getIdent().getName()))
                .findFirst()
                .orElseThrow();

        assertThat(AstNodeUtil.getBlockId(procedure)).isEqualTo("V-ChQTp}ZbGaPT;Mu5ok");
    }

    @Test
    void testGetBlockMap() throws Exception {
        final Program program = getAST("src/test/fixtures/inputs.json");
        final Map<String, ASTNode> blockMap = AstNodeUtil.getBlockMap(program);
        final Consumer<String> isExpectedClass = (String blockId) -> {
            final Class<?> clazz = switch (blockId) {
                case "operator_equals" -> Equals.class;
                case "control_if" -> IfStmt.class;
                case "sensing_of" -> AttributeOf.class;
                case "motion_movesteps" -> MoveSteps.class;
                case "sensing_of_object_menu" -> WithExpr.class;
                default -> throw new IllegalArgumentException(blockId);
            };

            assertThat(clazz.isAssignableFrom(blockMap.get(blockId).getClass())).isTrue();
        };

        assertAll(blockMap.keySet().stream().map(blockId -> () -> isExpectedClass.accept(blockId)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"operator_equals", "motion_movesteps", "sensing_of"})
    void testIsInputOfKindBlocks(final String blockId) throws Exception {
        final Program program = getAST("src/test/fixtures/inputs.json");
        final ASTNode block = AstNodeUtil.getBlockMap(program).get(blockId);
        final String inputKey = switch (blockId) {
            case "operator_equals" -> CONDITION_KEY;
            case "sensing_of" -> OPERAND1_KEY;
            case "motion_movesteps" -> SUBSTACK_KEY;
            default -> throw new IllegalArgumentException();
        };

        assertThat(AstNodeUtil.isInputOfKind(block, inputKey)).isTrue();

        final Stream<String> otherKeys = Stream.of(CONDITION_KEY, OPERAND1_KEY, SUBSTACK_KEY, OPERAND2_KEY, STEPS_KEY)
                .filter(key -> !key.equals(inputKey));

        assertAll(otherKeys.map(key -> () -> assertThat(AstNodeUtil.isInputOfKind(block, key)).isFalse()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"operator_equals", "motion_movesteps"})
    void testIsInputOfKindPrimitives(final String parentId) throws Exception {
        final Program program = getAST("src/test/fixtures/inputs.json");
        final ASTNode block = AstNodeUtil.getBlockMap(program).get(parentId);
        final ASTNode input;
        final String inputKind;

        switch (parentId) {
            case "operator_equals":
                input = ((Equals) block).getOperand2();
                inputKind = OPERAND2_KEY;
                break;
            case "motion_movesteps":
                input = ((MoveSteps) block).getSteps();
                inputKind = STEPS_KEY;
                break;
            default:
                throw new IllegalArgumentException(parentId);
        }

        assertThat(AstNodeUtil.isInputOfKind(input, inputKind)).isTrue();

        final Stream<String> otherKeys = Stream.of(CONDITION_KEY, OPERAND1_KEY, SUBSTACK_KEY, OPERAND2_KEY, STEPS_KEY)
                .filter(key -> !key.equals(inputKind));

        assertAll(otherKeys.map(key -> () -> assertThat(AstNodeUtil.isInputOfKind(input, key)).isFalse()));
    }
}
