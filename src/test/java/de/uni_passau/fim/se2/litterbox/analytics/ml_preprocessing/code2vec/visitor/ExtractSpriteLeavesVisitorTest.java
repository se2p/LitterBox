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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec.visitor;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExtractSpriteLeavesVisitorTest implements JsonTest {

    @Test
    void testVisit() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        ExtractSpriteLeavesVisitor spriteVisitor = new ExtractSpriteLeavesVisitor(
                program.getProcedureMapping(), false
        );
        program.accept(spriteVisitor);
        Map<ActorDefinition, List<ASTNode>> leavesMap = spriteVisitor.getLeaves();

        assertEquals(2, leavesMap.keySet().size());

        ActorDefinition[] sprites = getSpriteArrayFromLeavesMap(leavesMap);

        //check sprite abby
        ActorDefinition firstSprite = sprites[0];
        assertEquals("abby", (firstSprite).getIdent().getName());
        assertEquals(2, leavesMap.get(firstSprite).size());
        assertEquals("GreenFlag", leavesMap.get(firstSprite).get(0).getUniqueName());
        assertEquals("StringLiteral", leavesMap.get(firstSprite).get(1).getUniqueName());

        //check sprite cat
        ActorDefinition secondSprite = sprites[1];
        assertEquals("cat", (secondSprite).getIdent().getName());
        assertEquals(3, leavesMap.get(secondSprite).size());
        assertEquals("NumberLiteral", leavesMap.get(secondSprite).get(0).getUniqueName());
        assertEquals("StringLiteral", leavesMap.get(secondSprite).get(1).getUniqueName());
        assertEquals("Show", leavesMap.get(secondSprite).get(2).getUniqueName());
    }

    @Test
    void testVisitIncludeStage() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        ExtractSpriteLeavesVisitor spriteVisitor = new ExtractSpriteLeavesVisitor(
                program.getProcedureMapping(), true
        );
        program.accept(spriteVisitor);

        Map<ActorDefinition, List<ASTNode>> leavesMap = spriteVisitor.getLeaves();
        assertEquals(3, leavesMap.keySet().size());

        Optional<ActorDefinition> stage = leavesMap.keySet().stream().filter(ActorDefinition::isStage).findFirst();
        assertTrue(stage.isPresent());
    }

    private ActorDefinition[] getSpriteArrayFromLeavesMap(Map<ActorDefinition, List<ASTNode>> leavesMap) {
        ActorDefinition[] sprites = new ActorDefinition[2];
        for (ActorDefinition sprite : leavesMap.keySet()) {
            if (sprite.getIdent().getName().equals("abby")) {
                sprites[0] = sprite;
            } else if (sprite.getIdent().getName().equals("cat")){
                sprites[1] = sprite;
            } else {
                fail("Expected were 'abby' or 'cat' but was " + sprite.getIdent().getName());
            }
        }
        return sprites;
    }
}
