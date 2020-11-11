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
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ColorParserTest implements JsonTest {

    @Test
    public void testFromNumberInColorTouches() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/colorBlocks.json");
        ActorDefinitionList list = program.getActorDefinitionList();
        Truth.assertThat(list.getDefinitions().size()).isEqualTo(2);

        final ActorDefinition first = list.getDefinitions().get(1);
        final Script script = first.getScripts().getScriptList().get(0);
        final ExpressionStmt expressionStmt = (ExpressionStmt) script.getStmtList().getStmts()
                .get(0);
        Truth.assertThat(expressionStmt.getExpression()).isInstanceOf(ColorTouchingColor.class);
        ColorTouchingColor expression = (ColorTouchingColor) expressionStmt.getExpression();

        Truth.assertThat(expression.getOperand1()).isInstanceOf(FromNumber.class);
        Truth.assertThat(expression.getOperand2()).isInstanceOf(FromNumber.class);
    }

    @Test
    public void testColorLiteral() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/colorBlocks.json");
        ActorDefinitionList list = program.getActorDefinitionList();
        Truth.assertThat(list.getDefinitions().size()).isEqualTo(2);

        final ActorDefinition first = list.getDefinitions().get(1);
        final Script script = first.getScripts().getScriptList().get(1);
        final ExpressionStmt expressionStmt = (ExpressionStmt) script.getStmtList().getStmts()
                .get(0);
        Truth.assertThat(expressionStmt.getExpression()).isInstanceOf(SpriteTouchingColor.class);
        SpriteTouchingColor expression = (SpriteTouchingColor) expressionStmt.getExpression();

        Truth.assertThat(expression.getColor()).isInstanceOf(ColorLiteral.class);
    }
}
