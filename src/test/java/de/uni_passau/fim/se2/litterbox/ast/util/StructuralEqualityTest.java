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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Div;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Minus;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.IfOnEdgeBounce;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

class StructuralEqualityTest implements JsonTest {

    @Test
    void expressionEqualIgnoringLiterals() {
        final NumberLiteral n1 = new NumberLiteral(12);
        final NumberLiteral n2 = new NumberLiteral(123);
        final NumberLiteral n3 = new NumberLiteral(3);

        final Add add1 = new Add(n1, n2, new NoBlockMetadata());
        final Add add2 = new Add(n1, n3, new NoBlockMetadata());

        assertThat(StructuralEquality.areStructurallyEqualIgnoringLiterals(add1, add2)).isTrue();
    }

    @Test
    void expressionEqual() {
        final NumberLiteral n1 = new NumberLiteral(12);
        final NumberLiteral n2 = new NumberLiteral(123);
        final NumberLiteral n3 = new NumberLiteral(3);

        final Add add1 = new Add(n1, n2, new NoBlockMetadata());
        final Add add2 = new Add(n1, n3, new NoBlockMetadata());

        assertThat(StructuralEquality.areStructurallyEqual(add1, add2)).isFalse();
    }

    @Test
    void expressionDifferentTypesNotEqual() {
        final NumberLiteral n1 = new NumberLiteral(12);
        final NumberLiteral n2 = new NumberLiteral(123);

        final Add add = new Add(n1, n2, new NoBlockMetadata());
        final Minus sub = new Minus(n1, n2, new NoBlockMetadata());

        assertThat(StructuralEquality.areStructurallyEqual(add, sub)).isFalse();
        assertThat(StructuralEquality.areStructurallyEqualIgnoringLiterals(add, sub)).isFalse();
    }

    @Test
    void stmtListsNotEqualIfChildCountDifferent() {
        final IfOnEdgeBounce s1 = new IfOnEdgeBounce(new NoBlockMetadata());

        final StmtList list1 = new StmtList(s1);
        final StmtList list2 = new StmtList(s1, s1);

        assertThat(StructuralEquality.areStructurallyEqual(list1, list2)).isFalse();
        assertThat(StructuralEquality.areStructurallyEqualIgnoringLiterals(list1, list2)).isFalse();
    }

    @Test
    void leafNonEqualToOtherNode() {
        final NumberLiteral n1 = new NumberLiteral(12);
        final NumberLiteral n2 = new NumberLiteral(123);

        final Div div = new Div(n1, n2, new NoBlockMetadata());

        assertThat(StructuralEquality.areStructurallyEqual(div, n1)).isFalse();
        assertThat(StructuralEquality.areStructurallyEqual(n1, div)).isFalse();
        assertThat(StructuralEquality.areStructurallyEqualIgnoringLiterals(div, n1)).isFalse();
        assertThat(StructuralEquality.areStructurallyEqualIgnoringLiterals(n1, div)).isFalse();
    }

    @Test
    void colorLiteralsEqual() {
        final ColorLiteral c1 = new ColorLiteral(12, 35, 67);
        final ColorLiteral c2 = new ColorLiteral(23, 56, 34);

        assertThat(StructuralEquality.areStructurallyEqual(c1, c2)).isFalse();
        assertThat(StructuralEquality.areStructurallyEqualIgnoringLiterals(c1, c2)).isTrue();
    }

    @Test
    void programShouldBeStructurallyEqualToItself() throws Exception {
        final Program program = getAST("src/test/fixtures/allBlocks.json");
        assertEqualToSelf(program);
    }

    @Test
    void programWithExtensionsBlocksShouldBeStructurallyEqualToItself() throws Exception {
        final Program program = getAST("src/test/fixtures/allExtensionBlocks.json");
        assertEqualToSelf(program);
    }

    @Test
    void recreatedProgramsWithDifferentStatesShouldBeStructurallyEqual() throws Exception {
        final Program program1 = getFixture("recreatedProgram_v1.json");
        final Program program2 = getFixture("recreatedProgram_v2.json");

        assertThat(StructuralEquality.areStructurallyEqual(program1, program2)).isTrue();
        assertThat(StructuralEquality.areStructurallyEqualIgnoringLiterals(program1, program2)).isTrue();
    }

    @Test
    void programsWithSameSpritesInDifferentOrderStructurallyEqual() throws Exception {
        final Program p1 = getFixture("sameSpritesDifferentOrder_v1.json");
        final Program p2 = getFixture("sameSpritesDifferentOrder_v2.json");

        assertThat(StructuralEquality.areStructurallyEqual(p1, p2)).isTrue();
        assertThat(StructuralEquality.areStructurallyEqual(p2, p1)).isTrue();

        assertThat(StructuralEquality.areStructurallyEqualIgnoringLiterals(p1, p2)).isTrue();
        assertThat(StructuralEquality.areStructurallyEqualIgnoringLiterals(p2, p1)).isTrue();
    }

    @Test
    void sameScriptsAndProceduresInDifferentOrderStructurallyEqual() throws Exception {
        final Program p1 = getFixture("sameScriptsDifferentOrder_v1.json");
        final Program p2 = getFixture("sameScriptsDifferentOrder_v2.json");

        assertThat(StructuralEquality.areStructurallyEqual(p1, p2)).isTrue();
        assertThat(StructuralEquality.areStructurallyEqual(p2, p1)).isTrue();

        assertThat(StructuralEquality.areStructurallyEqualIgnoringLiterals(p1, p2)).isTrue();
        assertThat(StructuralEquality.areStructurallyEqualIgnoringLiterals(p2, p1)).isTrue();
    }

    @Test
    void differentParameterTypesNonEqual() throws Exception {
        final Program p1 = getFixture("proceduresDifferentParamTypes_v1.json");
        final Program p2 = getFixture("proceduresDifferentParamTypes_v2.json");

        assertThat(StructuralEquality.areStructurallyEqual(p1, p2)).isFalse();
        assertThat(StructuralEquality.areStructurallyEqualIgnoringLiterals(p1, p2)).isFalse();
    }

    @Test
    void differentParameterCountsNonEqual() throws Exception {
        final Program p1 = getFixture("proceduresDifferentParamTypes_v2.json");
        final Program p2 = getFixture("proceduresDifferentParamTypes_v3.json");

        assertThat(StructuralEquality.areStructurallyEqual(p1, p2)).isFalse();
        assertThat(StructuralEquality.areStructurallyEqualIgnoringLiterals(p1, p2)).isFalse();
    }

    @Disabled("only for initial debugging which leaf types are missing")
    @ParameterizedTest
    @MethodSource("allFixtures")
    void allProgramsStructurallyIdenticalToSelf(final Program program) {
        assertEqualToSelf(program);
    }

    private void assertEqualToSelf(final Program program) {
        assertThat(StructuralEquality.areStructurallyEqual(program, program)).isTrue();
        assertThat(StructuralEquality.areStructurallyEqualIgnoringLiterals(program, program)).isTrue();
    }

    static Stream<Arguments> allFixtures() throws Exception {
        return Files.walk(Path.of("src/test/fixtures"))
                .filter(path -> path.getFileName().toString().endsWith(".json"))
                .filter(path -> !path.toString().contains("mblock"))
                .flatMap(path -> {
                    try {
                        return Stream.of(JsonTest.parseProgram(path.toString()));
                    } catch (Exception e) {
                        return Stream.empty();
                    }
                })
                .map(Arguments::of);
    }

    private Program getFixture(final String filename) throws Exception {
        return getAST("src/test/fixtures/structuralEquality/" + filename);
    }
}
