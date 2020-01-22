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
package ast.parser;

import static junit.framework.TestCase.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ast.Constants;
import ast.ParsingException;
import ast.model.ActorDefinition;
import ast.model.ActorDefinitionList;
import ast.model.Program;
import ast.model.SetStmtList;
import ast.model.expression.list.ExpressionListPlain;
import ast.model.literals.NumberLiteral;
import ast.model.literals.StringLiteral;
import ast.model.resource.ImageResource;
import ast.model.resource.SoundResource;
import ast.model.statement.common.SetAttributeTo;
import ast.model.statement.common.SetStmt;
import ast.model.statement.common.SetVariableTo;
import ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import ast.model.statement.declaration.DeclarationStmt;
import ast.model.variable.Qualified;

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
            Truth.assertThat(((DeclarationIdentAsTypeStmt) decls.get(0)).getIdent()
                    .getName()).isEqualTo(Constants.VARIABLE_ABBREVIATION + "my variable");

            SetVariableTo setStmt = (SetVariableTo) stage.getSetStmtList().getStmts().stream()
                    .filter(t -> t instanceof SetVariableTo)
                    .findFirst().get();
            Truth.assertThat(((Qualified) setStmt.getVariable()).getSecond().getName()).isEqualTo(Constants.VARIABLE_ABBREVIATION + "my variable");
            Truth.assertThat(((NumberLiteral) setStmt.getExpr()).getValue()).isEqualTo(0);

            ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
            List<SetStmt> spriteSetStmts = sprite.getSetStmtList().getStmts().stream()
                    .filter(t -> t instanceof SetVariableTo).collect(
                            Collectors.toList());

            SetVariableTo setList = (SetVariableTo) spriteSetStmts.get(0);
            Qualified variable = (Qualified) setList.getVariable();
            Truth.assertThat(variable.getSecond().getName()).isEqualTo(Constants.LIST_ABBREVIATION + "SpriteLocalList");
            ExpressionListPlain exprListPlain = (ExpressionListPlain) setList.getExpr();
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