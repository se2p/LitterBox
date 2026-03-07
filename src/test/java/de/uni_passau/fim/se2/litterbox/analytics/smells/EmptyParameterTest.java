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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
        assertThat(issues.iterator().next().getHint().getKey()).isEqualTo("empty_parameter_direction");
    }

    @Test
    public void testSetPenColor() throws IOException, ParsingException {
        // set pen color to ()
        Program program = parseScratchBlocks("set pen color to ( )");
        EmptyParameter finder = new EmptyParameter();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(1);
        assertThat(issues.iterator().next().getHint().getKey()).isEqualTo("empty_parameter_color");
    }

    @Test
    public void testGoTo() throws IOException, ParsingException {
        // go to [ ]
        Program program = parseScratchBlocks("go to ( )");
        EmptyParameter finder = new EmptyParameter();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(1);
        assertThat(issues.iterator().next().getHint().getKey()).isEqualTo("empty_parameter_position");
    }

    @Test
    public void testChangeXBy() throws IOException, ParsingException {
        // change x by ()
        Program program = parseScratchBlocks("change x by ( )");
        EmptyParameter finder = new EmptyParameter();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(1);
        assertThat(issues.iterator().next().getHint().getKey()).isEqualTo("empty_parameter");
    }

    @Test
    public void testPointTowards() throws IOException, ParsingException {
        // point towards ()
        Program program = parseScratchBlocks("point towards ( )");
        EmptyParameter finder = new EmptyParameter();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(1);
        assertThat(issues.iterator().next().getHint().getKey()).isEqualTo("empty_parameter");
    }

    @Test
    public void testSpriteTouchingColor() throws IOException, ParsingException {
        // touching color ()?
        Program program = parseScratchBlocks("<touching color ( )?>");
        EmptyParameter finder = new EmptyParameter();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(1);
        assertThat(issues.iterator().next().getHint().getKey()).isEqualTo("empty_parameter_color");
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
    public void testNestedEmptyParam() throws IOException, ParsingException {
        // Construct AST manually: set x to (1 + [])
        // set x to 0 (base)
        Program program = parseScratchBlocks("set x to 0");

        // Navigate to Sprite1
        ActorDefinitionList actorList = program.getActorDefinitionList();

        ActorDefinition actor = null;
        for (ActorDefinition a : actorList.getDefinitions()) {
            if (a.getIdent().getName().equals("Sprite1")) {
                actor = a;
                break;
            }
        }
        if (actor == null && !actorList.getDefinitions().isEmpty()) {
            actor = actorList.getDefinitions().get(0);
        }

        Script script = actor.getScripts().getScriptList().get(0);

        // Clear children and add SetXTo manually
        script.getStmtList().getStmts().clear();

        NoBlockMetadata meta = new NoBlockMetadata();

        // Construct (1 + [])
        NumberLiteral num1 = new NumberLiteral(1.0);
        StringLiteral emptyStr = new StringLiteral("");
        AsNumber emptyStrAsNum = new AsNumber(emptyStr);

        Add add = new Add(num1, emptyStrAsNum, meta);
        AsNumber asNum = new AsNumber(add);

        SetXTo setStmt = new SetXTo(asNum, meta);

        // Wire parents
        setStmt.setParentNode(script.getStmtList());
        asNum.setParentNode(setStmt);
        add.setParentNode(asNum);
        num1.setParentNode(add);
        emptyStrAsNum.setParentNode(add);
        emptyStr.setParentNode(emptyStrAsNum);

        script.getStmtList().getStmts().add(setStmt);

        EmptyParameter finder = new EmptyParameter();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(1);
        assertThat(issues.iterator().next().getHint().getKey()).isEqualTo("empty_parameter");
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
}
