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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.quantum.*;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.util.StructuralEquality;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeFilteringVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class QuantumExtensionParserTest implements JsonTest {

    Program program;
    ActorDefinition sprite;

    @BeforeEach
    void setUp() throws ParsingException {
        program = getAST("src/test/fixtures/quantum.json");
        sprite = program.getActorDefinitionList().getDefinitions().getLast();
        assertThat(sprite.getIdent().getName()).isEqualTo("Sprite1");
    }

    @Test
    void testClone() {
        final CloneVisitor cv = new CloneVisitor();
        final Program program2 = (Program) program.accept(cv);
        assertThat(StructuralEquality.areStructurallyEqual(program, program2)).isTrue();
    }

    @Test
    void testEvents() {
        final List<Script> scripts = sprite.getScripts().getScriptList();
        assertThat(scripts).hasSize(3);
        assertAll(scripts.stream().map(s -> () -> assertThat(s.getEvent()).isNotInstanceOf(Never.class)));
    }

    @Test
    void testMeasure() {
        final Measure measure = NodeFilteringVisitor.getBlock(program, Measure.class).orElseThrow();
        assertThat(measure.getMetadata()).isNotNull();
    }

    @Test
    void testQuantumSuperpositionPossibilities() {
        final QuantumSuperpositionPossibilities node = NodeFilteringVisitor.getBlock(program, QuantumSuperpositionPossibilities.class).orElseThrow();
        assertThat(node.getMetadata()).isNotNull();
        assertThat(node.getAttribute().getType()).isEqualTo(FixedQuantumAttribute.FixedQuantumAttributeType.POSITION);
        assertThat(node.getPossibilities()).isEqualTo(new NumberLiteral(1.0));
    }

    @Test
    void testQuantumSuperpositionPossibilitiesList() {
        final QuantumSuperpositionPossibilitiesList node = NodeFilteringVisitor.getBlock(program, QuantumSuperpositionPossibilitiesList.class).orElseThrow();
        assertThat(node.getMetadata()).isNotNull();
        assertThat(node.getList()).isEqualTo(new StringLiteral("list"));
        assertThat(node.getAttribute().getType()).isEqualTo(FixedQuantumAttribute.FixedQuantumAttributeType.DIRECTION);
        assertThat(node.getPossibilities()).isEqualTo(new NumberLiteral(1.0));
    }

    @Test
    void testQuantumSuperpositionList() {
        final QuantumSuperpositionList node = NodeFilteringVisitor.getBlock(program, QuantumSuperpositionList.class).orElseThrow();
        assertThat(node.getMetadata()).isNotNull();
        assertThat(node.getList()).isEqualTo(new StringLiteral("list"));
        assertThat(node.getAttribute().getType()).isEqualTo(FixedQuantumAttribute.FixedQuantumAttributeType.SIZE);
    }

    @Test
    void testQuantumEntanglementPossibilities() {
        final QuantumEntanglementPossibilities node = NodeFilteringVisitor.getBlock(program, QuantumEntanglementPossibilities.class).orElseThrow();
        assertThat(node.getMetadata()).isNotNull();
        assertThat(node.getAttribute().getType()).isEqualTo(FixedQuantumAttribute.FixedQuantumAttributeType.COLOR);
        assertThat(node.getPossibilities()).isEqualTo(new NumberLiteral(1.0));
    }

    @Test
    void testQuantumEntanglementPossibilitiesList() {
        final QuantumEntanglementPossibilitiesList node = NodeFilteringVisitor.getBlock(program, QuantumEntanglementPossibilitiesList.class).orElseThrow();
        assertThat(node.getMetadata()).isNotNull();
        assertThat(node.getList()).isEqualTo(new StringLiteral("list"));
        assertThat(node.getAttribute().getType()).isEqualTo(FixedQuantumAttribute.FixedQuantumAttributeType.SIZE);
        assertThat(node.getPossibilities()).isEqualTo(new NumberLiteral(1.0));
    }

    @Test
    void testQuantumEntanglementList() {
        final QuantumEntanglementList node = NodeFilteringVisitor.getBlock(program, QuantumEntanglementList.class).orElseThrow();
        assertThat(node.getMetadata()).isNotNull();
        assertThat(node.getList()).isEqualTo(new StringLiteral("list"));
        assertThat(node.getAttribute().getType()).isEqualTo(FixedQuantumAttribute.FixedQuantumAttributeType.COSTUME);
    }
}
