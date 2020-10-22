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
package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.NextBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static de.uni_passau.fim.se2.litterbox.cfg.Attribute.AttributeType.*;

public class AttributeTest implements JsonTest {

    @Test
    public void testSingleDefinitionAndUse() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/move.json");
        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof MoveSteps).findFirst().get();

        AttributeDefinitionVisitor visitor = new AttributeDefinitionVisitor(node.getActor());
        node.getASTNode().accept(visitor);
        Set<Attribute> definitions = visitor.getDefineables();
        assertThat(definitions).hasSize(1);
        assertThat(definitions.iterator().next().getAttributeType()).isEqualTo(POSITION);

        AttributeUseVisitor useVisitor = new AttributeUseVisitor(node.getActor());
        node.getASTNode().accept(useVisitor);
        Set<Attribute> uses = visitor.getDefineables();
        assertThat(uses).hasSize(1);
        assertThat(uses.iterator().next().getAttributeType()).isEqualTo(POSITION);
    }

    @Test
    public void testPositionDefinitions() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/positiondefinitions.json");
        Set<CFGNode> nodes = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SpriteMotionStmt).collect(Collectors.toSet());

        assertThat(nodes.size()).isEqualTo(9);
        for (CFGNode node : nodes) {
            AttributeDefinitionVisitor visitor = new AttributeDefinitionVisitor(node.getActor());
            node.getASTNode().accept(visitor);
            Set<Attribute> definitions = visitor.getDefineables();
            assertThat(definitions).hasSize(1);
            assertThat(definitions.stream().findFirst().get().getAttributeType()).isEqualTo(POSITION);
        }
    }

    @Test
    public void testRotationDefinitions() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/rotationdefinitions.json");
        Set<CFGNode> nodes = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SpriteMotionStmt).collect(Collectors.toSet());

        assertThat(nodes.size()).isEqualTo(4);
        for (CFGNode node : nodes) {
            AttributeDefinitionVisitor visitor = new AttributeDefinitionVisitor(node.getActor());
            node.getASTNode().accept(visitor);
            Set<Attribute> definitions = visitor.getDefineables();
            assertThat(definitions).hasSize(1);
            assertThat(definitions.stream().findFirst().get().getAttributeType()).isEqualTo(ROTATION);
        }
    }

    @Test
    public void testPositionUses() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/positionuses.json");
        Set<CFGNode> nodes = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SpriteMotionStmt || n.getASTNode() instanceof SpriteLookStmt).collect(Collectors.toSet());

        assertThat(nodes.size()).isEqualTo(5);
        for (CFGNode node : nodes) {
            AttributeUseVisitor visitor = new AttributeUseVisitor(node.getActor());
            node.getASTNode().accept(visitor);
            Set<Attribute> uses = visitor.getDefineables();
            assertThat(uses).hasSize(1);
            assertThat(uses.stream().findFirst().get().getAttributeType()).isEqualTo(POSITION);
        }
    }

    @Test
    public void testRotationUses() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/rotationuses.json");
        Set<CFGNode> nodes = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SpriteMotionStmt || n.getASTNode() instanceof SpriteLookStmt).collect(Collectors.toSet());

        assertThat(nodes.size()).isEqualTo(3);
        for (CFGNode node : nodes) {
            AttributeUseVisitor visitor = new AttributeUseVisitor(node.getActor());
            node.getASTNode().accept(visitor);
            Set<Attribute> uses = visitor.getDefineables();
            assertThat(uses).hasSize(1);
            assertThat(uses.stream().findFirst().get().getAttributeType()).isEqualTo(ROTATION);
        }
    }

    @Test
    public void testCostumeDefinitions() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/costumedefuses.json");

        Set<CFGNode> nodes = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof NextCostume || n.getASTNode() instanceof SwitchCostumeTo).collect(Collectors.toSet());

        assertThat(nodes.size()).isEqualTo(2);
        for (CFGNode node : nodes) {
            AttributeDefinitionVisitor visitor = new AttributeDefinitionVisitor(node.getActor());
            node.getASTNode().accept(visitor);
            Set<Attribute> definitions = visitor.getDefineables();
            assertThat(definitions).hasSize(1);
            assertThat(definitions.stream().findFirst().get().getAttributeType()).isEqualTo(COSTUME);
        }
    }

    @Test
    public void testCostumeUses() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/costumedefuses.json");

        Set<CFGNode> nodes = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof NextCostume || n.getASTNode() instanceof Say).collect(Collectors.toSet());

        assertThat(nodes.size()).isEqualTo(3);
        for (CFGNode node : nodes) {
            AttributeUseVisitor visitor = new AttributeUseVisitor(node.getActor());
            node.getASTNode().accept(visitor);
            Set<Attribute> uses = visitor.getDefineables();
            assertThat(uses).hasSize(1);
            assertThat(uses.stream().findFirst().get().getAttributeType()).isEqualTo(COSTUME);
        }
    }

    @Test
    public void testSizeDefinitions() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/sizedefuses.json");

        Set<CFGNode> nodes = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetSizeTo || n.getASTNode() instanceof ChangeSizeBy).collect(Collectors.toSet());

        assertThat(nodes.size()).isEqualTo(2);
        for (CFGNode node : nodes) {
            AttributeDefinitionVisitor visitor = new AttributeDefinitionVisitor(node.getActor());
            node.getASTNode().accept(visitor);
            Set<Attribute> definitions = visitor.getDefineables();
            assertThat(definitions).hasSize(1);
            assertThat(definitions.stream().findFirst().get().getAttributeType()).isEqualTo(SIZE);
        }
    }

    @Test
    public void testSizeUses() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/sizedefuses.json");

        Set<CFGNode> nodes = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof ChangeSizeBy || n.getASTNode() instanceof Say).collect(Collectors.toSet());

        assertThat(nodes.size()).isEqualTo(2);
        for (CFGNode node : nodes) {
            AttributeUseVisitor visitor = new AttributeUseVisitor(node.getActor());
            node.getASTNode().accept(visitor);
            Set<Attribute> uses = visitor.getDefineables();
            assertThat(uses).hasSize(1);
            assertThat(uses.stream().findFirst().get().getAttributeType()).isEqualTo(SIZE);
        }
    }

    @Test
    public void testSizeSetAndUse() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/setsizechangesize.json");

        CFGNode greenFlag = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof GreenFlag).findFirst().get();
        CFGNode setSize = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SetSizeTo).findFirst().get();
        CFGNode repeatForever = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof RepeatForeverStmt).findFirst().get();
        CFGNode changeSize = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof ChangeSizeBy).findFirst().get();

        AttributeUseVisitor useVisitor = new AttributeUseVisitor(greenFlag.getActor()); // TODO: the greenflag is shared?
        AttributeDefinitionVisitor defVisitor = new AttributeDefinitionVisitor(greenFlag.getActor()); // TODO: the greenflag is shared?
        greenFlag.getASTNode().accept(useVisitor);
        greenFlag.getASTNode().accept(defVisitor);
        assertThat(useVisitor.getDefineables()).isEmpty();
        assertThat(defVisitor.getDefineables()).isEmpty();

        useVisitor = new AttributeUseVisitor(setSize.getActor());
        defVisitor = new AttributeDefinitionVisitor(setSize.getActor());
        setSize.getASTNode().accept(useVisitor);
        setSize.getASTNode().accept(defVisitor);
        assertThat(useVisitor.getDefineables()).isEmpty();
        assertThat(defVisitor.getDefineables()).hasSize(1);

        useVisitor = new AttributeUseVisitor(repeatForever.getActor());
        defVisitor = new AttributeDefinitionVisitor(repeatForever.getActor());
        repeatForever.getASTNode().accept(useVisitor);
        repeatForever.getASTNode().accept(defVisitor);
        assertThat(useVisitor.getDefineables()).isEmpty();
        assertThat(defVisitor.getDefineables()).isEmpty();

        useVisitor = new AttributeUseVisitor(changeSize.getActor());
        defVisitor = new AttributeDefinitionVisitor(changeSize.getActor());
        changeSize.getASTNode().accept(useVisitor);
        changeSize.getASTNode().accept(defVisitor);
        assertThat(useVisitor.getDefineables()).hasSize(1);
        assertThat(defVisitor.getDefineables()).hasSize(1);
    }

    @Test
    public void testNextBackdrop() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/nextbackdroponstage.json");
        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof NextBackdrop).findFirst().get();
        // TODO: Attributes on backdrop are not yet implemented
        assertThat(getDefinedAttributes(node)).hasSize(0);
    }

    @Test
    public void testNextBackdropOnSprite() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/nextbackdroponsprite.json");
        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof NextBackdrop).findFirst().get();
        // TODO: Attributes on backdrop are not yet implemented
        assertThat(getDefinedAttributes(node)).hasSize(0);
    }

    @Test
    public void testUseOfOtherSprite() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/useattributefromothersprite.json");

        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).findFirst().get();
        assertThat(getUsedAttributes(node)).hasSize(1);
    }

    private Set<Attribute> getDefinedAttributes(CFGNode node) {
        AttributeDefinitionVisitor visitor = new AttributeDefinitionVisitor(node.getActor());
        node.getASTNode().accept(visitor);

        return visitor.getDefineables();
    }

    private Set<Attribute> getUsedAttributes(CFGNode node) {
        AttributeUseVisitor visitor = new AttributeUseVisitor(node.getActor());
        node.getASTNode().accept(visitor);
        return visitor.getDefineables();
    }
}
