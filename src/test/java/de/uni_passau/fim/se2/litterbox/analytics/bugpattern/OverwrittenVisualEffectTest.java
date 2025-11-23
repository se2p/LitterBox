/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class OverwrittenVisualEffectTest implements JsonTest {

    private final ScratchBlocksParser parser = new ScratchBlocksParser();

    @Test
    public void testOverwrittenCostume() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            switch costume to (costume1 v)
            switch costume to (costume2 v)
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).hasSize(1);
    }

    @Test
    public void testOverwrittenCostumeWithWait() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            switch costume to (costume1 v)
            wait (1) seconds
            switch costume to (costume2 v)
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).isEmpty();
    }
    
    @Test
    public void testOverwrittenEffect() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            set [color v] effect to (10)
            set [color v] effect to (20)
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).hasSize(1);
    }

    @Test
    public void testDifferentEffects() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            set [color v] effect to (10)
            set [ghost v] effect to (50)
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).isEmpty();
    }

    @Test
    public void testClearEffects() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            set [color v] effect to (10)
            clear graphic effects
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).hasSize(1);
    }
    
    @Test
    public void testClearEffectsOverwritten() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            clear graphic effects
            clear graphic effects
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).hasSize(1);
    }

    @Test
    public void testSayOverwritten() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            say [Hello]
            say [World]
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).hasSize(1);
    }

    @Test
    public void testSayForSecsHalts() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            say [Hello]
            say [World] for (2) seconds
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).isEmpty();
    }

    @Test
    public void testSetEffectThenClear() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            set [brightness v] effect to (50)
            clear graphic effects
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).hasSize(1);
    }

    @Test
    public void testConditionalOverwrite() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            switch costume to (costume1 v)
            if <key (space v) pressed?> then
                switch costume to (costume2 v)
            end
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).isEmpty();
    }

    @Test
    public void testConditionalHalting() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            switch costume to (costume1 v)
            if <key (space v) pressed?> then
                wait (1) seconds
            end
            switch costume to (costume2 v)
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).isEmpty();
    }
}
