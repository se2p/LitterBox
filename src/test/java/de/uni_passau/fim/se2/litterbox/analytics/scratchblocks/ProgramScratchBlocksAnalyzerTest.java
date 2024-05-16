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
package de.uni_passau.fim.se2.litterbox.analytics.scratchblocks;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.ProgramScratchBlocksAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ProgramScratchBlocksAnalyzerTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/emptyProject.json");
        ProgramScratchBlocksAnalyzer scratchBlocksAnalyzer = new ProgramScratchBlocksAnalyzer();
        String scratchBlocks = scratchBlocksAnalyzer.analyze(empty);
        Assertions.assertEquals("//Sprite: Stage" + System.lineSeparator() + "//Sprite: Sprite1", scratchBlocks);
    }

    @Test
    public void testNonEmptyProgram() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/allBlocks.json");
        ProgramScratchBlocksAnalyzer scratchBlocksAnalyzer = new ProgramScratchBlocksAnalyzer();
        String scratchBlocks = scratchBlocksAnalyzer.analyze(empty);
        Assertions.assertTrue(scratchBlocks.contains("when [space v] key pressed" + System.lineSeparator() + "turn right (15) degrees"));
    }
}
