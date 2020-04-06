/*
 * Copyright (C) 2019 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.resource.ImageResource;
import de.uni_passau.fim.se2.litterbox.ast.model.resource.SoundResource;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetAttributeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.fail;

public class ProgramParserTest {

    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/emptyProject.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testEmptyProgramStructure() {
        try {
            Program program = ProgramParser.parseProgram("Empty", project);
            Truth.assertThat(program.getIdent().getName()).isEqualTo("Empty");
            Truth.assertThat(program.getChildren().size()).isEqualTo(2);
            Truth.assertThat(program.getChildren().get(1)).isInstanceOf(ActorDefinitionList.class);

            ActorDefinitionList list = program.getActorDefinitionList();
            Truth.assertThat(list.getDefintions().size()).isEqualTo(2);
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testSetStmts() {
        try {
            Program program = ProgramParser.parseProgram("Empty", project);
            ActorDefinition stage = program.getActorDefinitionList().getDefintions().get(0);
            SetStmtList setStmtList = stage.getSetStmtList();
            List<SetStmt> stmts = setStmtList.getStmts();
            SetAttributeTo setAttr = (SetAttributeTo) stmts.get(0);
            Truth.assertThat(((StringLiteral) setAttr.getStringExpr()).getText()).isEqualTo("volume");
            Truth.assertThat(((NumberLiteral) setAttr.getExpr()).getValue()).isEqualTo(100);

            setAttr = (SetAttributeTo) stmts.get(1);
            Truth.assertThat(((StringLiteral) setAttr.getStringExpr()).getText()).isEqualTo("layerOrder");
            Truth.assertThat(((NumberLiteral) setAttr.getExpr()).getValue()).isEqualTo(0);

            setAttr = (SetAttributeTo) stmts.get(2);
            Truth.assertThat(((StringLiteral) setAttr.getStringExpr()).getText()).isEqualTo("tempo");
            Truth.assertThat(((NumberLiteral) setAttr.getExpr()).getValue()).isEqualTo(60);

            setAttr = (SetAttributeTo) stmts.get(3);
            Truth.assertThat(((StringLiteral) setAttr.getStringExpr()).getText()).isEqualTo("videoTransparency");
            Truth.assertThat(((NumberLiteral) setAttr.getExpr()).getValue()).isEqualTo(50);

            setAttr = (SetAttributeTo) stmts.get(4);
            Truth.assertThat(((StringLiteral) setAttr.getStringExpr()).getText()).isEqualTo("videoState");
            Truth.assertThat(((StringLiteral) setAttr.getExpr()).getText()).isEqualTo("on");
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testVariable() {
        try {
            Program program = ProgramParser.parseProgram("Empty", project);
            ActorDefinition stage = program.getActorDefinitionList().getDefintions().get(0);
            List<DeclarationStmt> decls = stage.getDecls().getDeclarationStmtList();
            Assertions.assertTrue(((DeclarationIdentAsTypeStmt) decls.get(0)).getIdent() instanceof Variable);
            Truth.assertThat(((DeclarationIdentAsTypeStmt) decls.get(0)).getIdent()
                    .getName().getName()).isEqualTo( "my variable");

            SetVariableTo setStmt = (SetVariableTo) stage.getSetStmtList().getStmts().stream()
                    .filter(t -> t instanceof SetVariableTo)
                    .findFirst().get();
            Assertions.assertTrue(((Qualified) setStmt.getIdentifier()).getSecond() instanceof Variable);
            Truth.assertThat(((Qualified) setStmt.getIdentifier()).getSecond().getName().getName()).isEqualTo( "my variable");
            Truth.assertThat(((NumberLiteral) setStmt.getExpr()).getValue()).isEqualTo(0);

            ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
            List<SetStmt> spriteSetStmts = sprite.getSetStmtList().getStmts().stream()
                    .filter(t -> t instanceof SetVariableTo).collect(
                            Collectors.toList());

            SetVariableTo setList = (SetVariableTo) spriteSetStmts.get(0);
            Qualified variable = (Qualified) setList.getIdentifier();
            Assertions.assertTrue(variable.getSecond() instanceof ScratchList);
            Truth.assertThat(variable.getSecond().getName().getName()).isEqualTo( "SpriteLocalList");
            ExpressionList exprListPlain = (ExpressionList) setList.getExpr();
            Truth.assertThat(((StringLiteral) exprListPlain.getExpressions().get(0)).getText()).isEqualTo("Elem1");
            Truth.assertThat(((StringLiteral) exprListPlain.getExpressions().get(1)).getText()).isEqualTo("Elem2");
            Truth.assertThat(((StringLiteral) exprListPlain.getExpressions().get(2)).getText()).isEqualTo("1");
            Truth.assertThat(((StringLiteral) exprListPlain.getExpressions().get(3)).getText()).isEqualTo("2");
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testResources() {
        Program program = null;
        try {
            program = ProgramParser.parseProgram("Empty", project);
            ActorDefinition stage = program.getActorDefinitionList().getDefintions().get(0);
            SoundResource soundResource = (SoundResource) stage.getResources().getResourceList().get(0);
            Truth.assertThat(soundResource.getIdent().getName()).isEqualTo("pop");
            ImageResource imageResource = (ImageResource) stage.getResources().getResourceList().get(1);
            Truth.assertThat(imageResource.getIdent().getName()).isEqualTo("backdrop1");

            ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
            soundResource = (SoundResource) sprite.getResources().getResourceList().get(0);
            Truth.assertThat(soundResource.getIdent().getName()).isEqualTo("Meow");
            imageResource = (ImageResource) sprite.getResources().getResourceList().get(1);
            Truth.assertThat(imageResource.getIdent().getName()).isEqualTo("costume1");
            imageResource = (ImageResource) sprite.getResources().getResourceList().get(2);
            Truth.assertThat(imageResource.getIdent().getName()).isEqualTo("costume2");
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }
}