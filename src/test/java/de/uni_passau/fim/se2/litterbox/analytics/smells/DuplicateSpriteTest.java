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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class DuplicateSpriteTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/emptyProject.json");
        DuplicateSprite parameterName = new DuplicateSprite();
        Set<Issue> reports = parameterName.check(program);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testDuplicateSprite() throws IOException, ParsingException {
        Program duplicateSprite = getAST("./src/test/fixtures/smells/duplicateSprite.json");
        DuplicateSprite finder = new DuplicateSprite();
        Set<Issue> reports = finder.check(duplicateSprite);
        Assertions.assertEquals(3, reports.size());
    }

    @Test
    public void testDuplicate2Sprite() throws IOException, ParsingException {
        Program duplicate2Sprite = getAST("./src/test/fixtures/smells/duplicatedSprite1Script.json");
        DuplicateSprite finder = new DuplicateSprite();
        Set<Issue> reports = finder.check(duplicate2Sprite);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testDuplicate2Sprite2Scripts() throws IOException, ParsingException {
        Program duplicate2Sprite2Scripts = getAST("./src/test/fixtures/smells/duplicatedSprite2Scripts.json");
        DuplicateSprite finder = new DuplicateSprite();
        Set<Issue> reports = finder.check(duplicate2Sprite2Scripts);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testNotQuiteDuplicated() throws IOException, ParsingException {
        Program duplicate2SpriteDifferentScript = getAST("./src/test/fixtures/smells/duplicatedSprite1DifferentScript.json");
        DuplicateSprite finder = new DuplicateSprite();
        Set<Issue> reports = finder.check(duplicate2SpriteDifferentScript);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testSameCodeDifferentCostumes() throws IOException, ParsingException {
        Program duplicate2SpriteDifferentCostumes = getAST("./src/test/fixtures/smells/duplicatedSpriteDifferentCostumes.json");
        DuplicateSprite finder = new DuplicateSprite();
        Set<Issue> reports = finder.check(duplicate2SpriteDifferentCostumes);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void test2Clones() throws IOException, ParsingException {
        Program duplicated3Sprites = getAST("./src/test/fixtures/smells/duplicated3Sprites.json");
        DuplicateSprite finder = new DuplicateSprite();
        Set<Issue> reports = finder.check(duplicated3Sprites);
        // 1-2, 1-3, 2-3
        Assertions.assertEquals(3, reports.size());
    }

    @Test
    public void testDuplicatedSpriteWithCustomBlock() throws IOException, ParsingException {
        Program duplicatedSpriteWithCustomBlock = getAST("./src/test/fixtures/smells/duplicatedSpriteWithCustomBlock.json");
        DuplicateSprite finder = new DuplicateSprite();
        Set<Issue> reports = finder.check(duplicatedSpriteWithCustomBlock);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testDuplicatedSpriteWithCustomBlocksWithParameters() throws IOException, ParsingException {
        Program duplicatedSpriteWithCustomBlocksWithParameters = getAST("./src/test/fixtures/smells/duplicatedSpriteWithCustomBlocksWithParameters.json");
        DuplicateSprite finder = new DuplicateSprite();
        Set<Issue> reports = finder.check(duplicatedSpriteWithCustomBlocksWithParameters);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testDuplicatedSpriteWithLocalVariable() throws IOException, ParsingException {
        Program duplicatedSpriteWithLocalVariable = getAST("./src/test/fixtures/smells/duplicatedSpriteWithLocalVariable.json");
        DuplicateSprite finder = new DuplicateSprite();
        Set<Issue> reports = finder.check(duplicatedSpriteWithLocalVariable);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testDuplicatedSpriteWithLocalAttributes() throws IOException, ParsingException {
        Program duplicatedSpriteWithLocalVariable = getAST("./src/test/fixtures/smells/duplicatedSpriteWithLocalAttributes.json");
        DuplicateSprite finder = new DuplicateSprite();
        Set<Issue> reports = finder.check(duplicatedSpriteWithLocalVariable);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testDuplicatedSpriteWithSameAttributeOfOtherSprite() throws IOException, ParsingException {
        Program duplicatedSpriteWithLocalVariable = getAST("./src/test/fixtures/smells/duplicatedSpriteWithSameAttributeOfOtherSprite.json");
        DuplicateSprite finder = new DuplicateSprite();
        Set<Issue> reports = finder.check(duplicatedSpriteWithLocalVariable);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testDuplicatedSpriteWithSameAttributeOfDifferentSprite() throws IOException, ParsingException {
        Program duplicatedSpriteWithLocalVariable = getAST("./src/test/fixtures/smells/duplicatedSpriteWithOtherAttributeOfOtherSprite.json");
        DuplicateSprite finder = new DuplicateSprite();
        Set<Issue> reports = finder.check(duplicatedSpriteWithLocalVariable);
        Assertions.assertEquals(0, reports.size());
    }

}
