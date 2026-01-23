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
package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests that sound names are correctly resolved to file paths in PyStage export.
 * Regression test for KeyError when playing sounds by name vs. registered path.
 */
class PyStageSoundPathResolutionTest implements JsonTest {

    @Test
    void testSoundNameResolvedToFilePath() throws IOException, ParsingException {
        // Uses fixture with: play_sound_until_done and start_sound for "Meow"
        // Sound metadata: name "Meow", md5ext "83c36d806dc92327b9e7049a565c6bff.wav"
        Program program = getAST("src/test/fixtures/stmtParser/actorSoundStmts.json");
        String result = PyStageVisitor.of(program);
        
        // The sound should be referenced by its file path, not just its name
        // Sound "Meow" should be resolved to "sounds/Meow.wav"
        assertThat(result).contains("start_sound(\"sounds/Meow.wav\")");
        assertThat(result).contains("play_sound_until_done(\"sounds/Meow.wav\")");
        
        // Ensure string literal sounds do NOT use _sound_at helper
        assertThat(result).doesNotContain("_sound_at(self, \"Meow\"");
    }
}
