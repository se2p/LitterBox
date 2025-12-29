/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for the {@link GoboscriptVisitor} export functionality.
 */
public class GoboscriptVisitorTest implements JsonTest {

    @Test
    public void testMotionBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        // Verify structure
        assertThat(result).contains("sprite");
        assertThat(result).contains("onflag {");
        assertThat(result).contains("move 10;");
        assertThat(result).contains("turn_right 15;");
        assertThat(result).contains("turn_left 15;");
        assertThat(result).contains("goto");
        assertThat(result).contains("point_in_direction 90;");
    }

    @Test
    public void testControlBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/controlblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains("onflag {");
        assertThat(result).contains("wait 1;");
        assertThat(result).contains("repeat 10 {");
        assertThat(result).contains("if");
        assertThat(result).contains("forever {");
        assertThat(result).contains("stop_all;");
    }

    @Test
    public void testLooksBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/lookblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains("say \"Hello!\"");
        assertThat(result).contains("think \"Hmm...\"");
        assertThat(result).contains("switch_costume");
        assertThat(result).contains("next_costume;");
        assertThat(result).contains("change_size");
        assertThat(result).contains("set_size");
        assertThat(result).contains("show;");
        assertThat(result).contains("hide;");
    }

    @Test
    public void testSoundBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/soundblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains("play_sound_until_done");
        assertThat(result).contains("start_sound");
        assertThat(result).contains("stop_all_sounds;");
        assertThat(result).contains("change_volume");
        assertThat(result).contains("set_volume");
    }

    @Test
    public void testVariableBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/variableblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        // Variable set and change
        assertThat(result).contains("= 0;");
        assertThat(result).contains("+= 1;");

        // List operations
        assertThat(result).contains("add");
        assertThat(result).contains("delete");
    }

    @Test
    public void testArithmeticExpressions() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/arithmeticblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        // Math operators
        assertThat(result).contains("+");
        assertThat(result).contains("-");
        assertThat(result).contains("*");
        assertThat(result).contains("/");
        assertThat(result).contains("round");
        assertThat(result).contains("pick_random");
    }

    @Test
    public void testBooleanExpressions() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/booleanblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        // Comparison and logic
        assertThat(result).contains(">");
        assertThat(result).contains("<");
        assertThat(result).contains("==");
        assertThat(result).contains("not");
        assertThat(result).contains("and");
        assertThat(result).contains("or");
    }

    @Test
    public void testCloneBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/cloneblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains("onclone {");
        assertThat(result).contains("create_clone myself;");
        assertThat(result).contains("delete_this_clone;");
    }

    @Test
    public void testMessageBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/messageblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains("on \"message1\"");
        assertThat(result).contains("broadcast");
    }

    @Test
    public void testCustomBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/multicustomblocks.json");
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        program.accept(visitor);
        String result = visitor.getOutput();

        assertThat(result).contains("proc");
        assertThat(result).contains("block1");
        assertThat(result).contains("block2");
    }

    @Test
    public void testStaticOf() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");
        String result = GoboscriptVisitor.of(program);

        assertThat(result).isNotEmpty();
        assertThat(result).contains("onflag");
        assertThat(result).contains("move");
    }
}
