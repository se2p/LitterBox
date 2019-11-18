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
package scratch.ast.parser;

import static junit.framework.TestCase.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.ActorDefinitionList;
import scratch.ast.model.Program;
import scratch.ast.model.SetStmtList;
import scratch.ast.model.expression.list.ExpressionListPlain;
import scratch.ast.model.expression.num.Number;
import scratch.ast.model.expression.string.Str;
import scratch.ast.model.resource.ImageResource;
import scratch.ast.model.resource.SoundResource;
import scratch.ast.model.statement.common.SetAttributeTo;
import scratch.ast.model.statement.common.SetStmt;
import scratch.ast.model.statement.common.SetVariableTo;
import scratch.ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import scratch.ast.model.statement.declaration.DeclarationStmt;
import scratch.ast.model.variable.Qualified;

public class ProgramParserTest {

    private JsonNode project;

    @Before
    public void setup() {
        String path = "src/test/java/scratch/fixtures/emptyProject.json";
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
            Truth.assertThat(program.getIdent().getValue()).isEqualTo("Empty");
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
            Truth.assertThat(((Str) setAttr.getStringExpr()).getStr()).isEqualTo("volume");
            Truth.assertThat(((Number) setAttr.getExpr()).getValue()).isEqualTo(100);

            setAttr = (SetAttributeTo) stmts.get(1);
            Truth.assertThat(((Str) setAttr.getStringExpr()).getStr()).isEqualTo("layerOrder");
            Truth.assertThat(((Number) setAttr.getExpr()).getValue()).isEqualTo(0);

            setAttr = (SetAttributeTo) stmts.get(2);
            Truth.assertThat(((Str) setAttr.getStringExpr()).getStr()).isEqualTo("tempo");
            Truth.assertThat(((Number) setAttr.getExpr()).getValue()).isEqualTo(60);

            setAttr = (SetAttributeTo) stmts.get(3);
            Truth.assertThat(((Str) setAttr.getStringExpr()).getStr()).isEqualTo("videoTransparency");
            Truth.assertThat(((Number) setAttr.getExpr()).getValue()).isEqualTo(50);

            setAttr = (SetAttributeTo) stmts.get(4);
            Truth.assertThat(((Str) setAttr.getStringExpr()).getStr()).isEqualTo("videoState");
            Truth.assertThat(((Str) setAttr.getExpr()).getStr()).isEqualTo("on");

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
                .getValue()).isEqualTo("my variable");

            SetVariableTo setStmt = (SetVariableTo) stage.getSetStmtList().getStmts().stream()
                .filter(t -> t instanceof SetVariableTo)
                .findFirst().get();
            Truth.assertThat(((Qualified) setStmt.getVariable()).getSecond().getValue()).isEqualTo("my variable");
            Truth.assertThat(((Number) setStmt.getExpr()).getValue()).isEqualTo(0);

            ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
            List<SetStmt> spriteSetStmts = sprite.getSetStmtList().getStmts().stream()
                .filter(t -> t instanceof SetVariableTo).collect(
                    Collectors.toList());

            SetVariableTo setList = (SetVariableTo) spriteSetStmts.get(0);
            Qualified variable = (Qualified) setList.getVariable();
            Truth.assertThat(variable.getSecond().getValue()).isEqualTo("SpriteLocalList");
            ExpressionListPlain exprListPlain = (ExpressionListPlain) setList.getExpr();
            Truth.assertThat(((Str) exprListPlain.getExpressions().get(0)).getStr()).isEqualTo("Elem1");
            Truth.assertThat(((Str) exprListPlain.getExpressions().get(1)).getStr()).isEqualTo("Elem2");
            Truth.assertThat(((Str) exprListPlain.getExpressions().get(2)).getStr()).isEqualTo("1");
            Truth.assertThat(((Str) exprListPlain.getExpressions().get(3)).getStr()).isEqualTo("2");

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
            Truth.assertThat(soundResource.getIdent().getValue()).isEqualTo("pop");
            ImageResource imageResource = (ImageResource) stage.getResources().getResourceList().get(1);
            Truth.assertThat(imageResource.getIdent().getValue()).isEqualTo("backdrop1");

            ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
            soundResource = (SoundResource) sprite.getResources().getResourceList().get(0);
            Truth.assertThat(soundResource.getIdent().getValue()).isEqualTo("Meow");
            imageResource = (ImageResource) sprite.getResources().getResourceList().get(1);
            Truth.assertThat(imageResource.getIdent().getValue()).isEqualTo("costume1");
            imageResource = (ImageResource) sprite.getResources().getResourceList().get(2);
            Truth.assertThat(imageResource.getIdent().getValue()).isEqualTo("costume2");

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }
}