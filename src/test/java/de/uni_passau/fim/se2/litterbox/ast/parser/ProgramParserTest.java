/*
 * Copyright (C) 2020 LitterBox contributors
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

import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetAttributeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ProgramParserTest implements JsonTest {

    @Test
    public void testEmptyProgramStructure() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        Truth.assertThat(program.getIdent().getName()).isEqualTo("emptyProject");
        Truth.assertThat(program.getChildren().size()).isEqualTo(3);
        Truth.assertThat(program.getChildren().get(1)).isInstanceOf(ActorDefinitionList.class);

        ActorDefinitionList list = program.getActorDefinitionList();
        Truth.assertThat(list.getDefinitions().size()).isEqualTo(2);
    }

    @Test
    public void testSetStmts() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ActorDefinition stage = program.getActorDefinitionList().getDefinitions().get(0);
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
    }

    @Test
    public void testVariable() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        ActorDefinition stage = program.getActorDefinitionList().getDefinitions().get(0);
        List<DeclarationStmt> decls = stage.getDecls().getDeclarationStmtList();
        Assertions.assertTrue(((DeclarationIdentAsTypeStmt) decls.get(0)).getIdent() instanceof Variable);
        Truth.assertThat(((DeclarationIdentAsTypeStmt) decls.get(0)).getIdent()
                .getName().getName()).isEqualTo("my variable");

        SetVariableTo setStmt = (SetVariableTo) stage.getSetStmtList().getStmts().stream()
                .filter(t -> t instanceof SetVariableTo)
                .findFirst().get();
        Assertions.assertTrue(((Qualified) setStmt.getIdentifier()).getSecond() instanceof Variable);
        Truth.assertThat(((Qualified) setStmt.getIdentifier()).getSecond().getName().getName()).isEqualTo("my " +
                "variable");
        Truth.assertThat(((NumberLiteral) setStmt.getExpr()).getValue()).isEqualTo(0);

        ActorDefinition sprite = program.getActorDefinitionList().getDefinitions().get(1);
        List<SetStmt> spriteSetStmts = sprite.getSetStmtList().getStmts().stream()
                .filter(t -> t instanceof SetVariableTo).collect(
                        Collectors.toList());

        SetVariableTo setList = (SetVariableTo) spriteSetStmts.get(0);
        Qualified variable = (Qualified) setList.getIdentifier();
        Assertions.assertTrue(variable.getSecond() instanceof ScratchList);
        Truth.assertThat(variable.getSecond().getName().getName()).isEqualTo("SpriteLocalList");
        ExpressionList exprListPlain = (ExpressionList) setList.getExpr();
        Truth.assertThat(((StringLiteral) exprListPlain.getExpressions().get(0)).getText()).isEqualTo("Elem1");
        Truth.assertThat(((StringLiteral) exprListPlain.getExpressions().get(1)).getText()).isEqualTo("Elem2");
        Truth.assertThat(((StringLiteral) exprListPlain.getExpressions().get(2)).getText()).isEqualTo("1");
        Truth.assertThat(((StringLiteral) exprListPlain.getExpressions().get(3)).getText()).isEqualTo("2");
    }
}
