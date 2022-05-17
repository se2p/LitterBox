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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class DuplicateSpriteTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new DuplicateSprite(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testDuplicateSprite() throws IOException, ParsingException {
        assertThatFinderReports(2, new DuplicateSprite(), "./src/test/fixtures/smells/duplicateSprite.json");
    }

    @Test
    public void testDuplicateSpriteMultiple() throws IOException, ParsingException {
        assertThatFinderReports(3, new DuplicateSprite(), "./src/test/fixtures/smells/duplicateSpriteMultiple.json");
    }

    @Test
    public void testDuplicate2Sprite() throws IOException, ParsingException {
        assertThatFinderReports(1, new DuplicateSprite(), "./src/test/fixtures/smells/duplicatedSprite1Script.json");
    }

    @Test
    public void testDuplicate2Sprite2Scripts() throws IOException, ParsingException {
        assertThatFinderReports(1, new DuplicateSprite(), "./src/test/fixtures/smells/duplicatedSprite2Scripts.json");
    }

    @Test
    public void testNotQuiteDuplicated() throws IOException, ParsingException {
        assertThatFinderReports(0, new DuplicateSprite(), "./src/test/fixtures/smells/duplicatedSprite1DifferentScript.json");
    }

    @Test
    public void testEmptyDuplicated() throws IOException, ParsingException {
        assertThatFinderReports(0, new DuplicateSprite(), "./src/test/fixtures/smells/doubleEmptySprite.json");
    }

    @Test
    public void testSameCodeDifferentCostumes() throws IOException, ParsingException {
        assertThatFinderReports(1, new DuplicateSprite(), "./src/test/fixtures/smells/duplicatedSpriteDifferentCostumes.json");
    }

    @Test
    public void test2Clones() throws IOException, ParsingException {
        // 1-2, 1-3 (Would be redundant: 2-3)
        assertThatFinderReports(2, new DuplicateSprite(), "./src/test/fixtures/smells/duplicated3Sprites.json");
    }

    @Test
    public void testDuplicatedSpriteWithCustomBlock() throws IOException, ParsingException {
        assertThatFinderReports(1, new DuplicateSprite(), "./src/test/fixtures/smells/duplicatedSpriteWithCustomBlock.json");
    }

    @Test
    public void testDuplicatedSpriteWithCustomBlocksWithParameters() throws IOException, ParsingException {
        assertThatFinderReports(1, new DuplicateSprite(), "./src/test/fixtures/smells/duplicatedSpriteWithCustomBlocksWithParameters.json");
    }

    @Test
    public void testDuplicatedSpriteWithLocalVariable() throws IOException, ParsingException {
        assertThatFinderReports(1, new DuplicateSprite(), "./src/test/fixtures/smells/duplicatedSpriteWithLocalVariable.json");
    }

    @Test
    public void testDuplicatedSpriteWithLocalAttributes() throws IOException, ParsingException {
        assertThatFinderReports(1, new DuplicateSprite(), "./src/test/fixtures/smells/duplicatedSpriteWithLocalAttributes.json");
    }

    @Test
    public void testDuplicatedSpriteWithSameAttributeOfOtherSprite() throws IOException, ParsingException {
        assertThatFinderReports(1, new DuplicateSprite(), "./src/test/fixtures/smells/duplicatedSpriteWithSameAttributeOfOtherSprite.json");
    }

    @Test
    public void testDuplicatedSpriteWithSameAttributeOfDifferentSprite() throws IOException, ParsingException {
        assertThatFinderReports(0, new DuplicateSprite(), "./src/test/fixtures/smells/duplicatedSpriteWithOtherAttributeOfOtherSprite.json");
    }
}
