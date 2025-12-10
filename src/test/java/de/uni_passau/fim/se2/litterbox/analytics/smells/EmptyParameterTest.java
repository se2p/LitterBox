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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class EmptyParameterTest implements JsonTest {

    @Test
    public void testEmptyParameter() throws IOException, ParsingException {
        // move () steps
        Program program = parseScratchBlocks("move ( ) steps");
        
        EmptyParameter finder = new EmptyParameter();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(1);
    }

    @Test
    public void testPointInDirection() throws IOException, ParsingException {
        // point in direction ()
        Program program = parseScratchBlocks("point in direction ( )");
        EmptyParameter finder = new EmptyParameter();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(1);
        assertThat(getHintKey(issues.iterator().next())).isEqualTo("empty_parameter_direction");
    }

    @Test
    public void testSetPenColor() throws IOException, ParsingException {
        // set pen color to ()
        Program program = parseScratchBlocks("set pen color to ( )");
        // Note: The parser might parse "set pen color to [ ]" as SetPenColorToColorStmt with an empty StringLiteral inside AsNumber/Color wrapper
        // We rely on parseScratchBlocks to produce a structure visiting StringLiteral
        EmptyParameter finder = new EmptyParameter();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(1);
        assertThat(getHintKey(issues.iterator().next())).isEqualTo("empty_parameter_color");
    }

    @Test
    public void testGoTo() throws IOException, ParsingException {
        // go to [ ]
        Program program = parseScratchBlocks("go to ( )");
        EmptyParameter finder = new EmptyParameter();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(1);
        assertThat(getHintKey(issues.iterator().next())).isEqualTo("empty_parameter_position");
    }

    @Test
    public void testNoEmptyParameter() throws IOException, ParsingException {
        // move (10) steps
        Program program = parseScratchBlocks("move (10) steps");
        EmptyParameter finder = new EmptyParameter();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).isEmpty();
    }
    @Test
    public void testEmptyStringInSay() throws IOException, ParsingException {
        // say []
        Program program = parseScratchBlocks("say []");
        EmptyParameter finder = new EmptyParameter();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).isEmpty();
    }

    @Test
    public void testEmptyStringInThink() throws IOException, ParsingException {
        // think []
        Program program = parseScratchBlocks("think []");
        EmptyParameter finder = new EmptyParameter();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).isEmpty();
    }
    
    @Test
    public void testEmptyStringInOtherBlock() throws IOException, ParsingException {
        // ask [] and wait 
        Program program = parseScratchBlocks("ask [] and wait");
        EmptyParameter finder = new EmptyParameter();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(1);
        assertThat(getHintKey(issues.iterator().next())).isEqualTo("empty_parameter");
    }

    private String getHintKey(Issue issue) {
        try {
            Field hintField = Issue.class.getDeclaredField("hint");
            hintField.setAccessible(true);
            Hint hint = (Hint) hintField.get(issue);
            
            Field keyField = Hint.class.getDeclaredField("hintKey");
            keyField.setAccessible(true);
            return (String) keyField.get(hint);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
