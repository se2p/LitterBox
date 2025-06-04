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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeFilteringVisitor;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ParserRegressionTest implements JsonTest {

    @Test
    void failParseHackedColourString() throws ParsingException, IOException {
        final Program p = getAST("src/test/fixtures/parserRegressions/hackedColourString.json");

        final var setPenColourBlock = NodeFilteringVisitor.getBlocks(p, SetPenColorToColorStmt.class).get(0);
        assertInstanceOf(FromNumber.class, setPenColourBlock.getColorExpr());

        assertThat(NodeFilteringVisitor.getBlocks(p, ColorLiteral.class)).isEmpty();
    }

    @Test
    void implicitlyDefinedListShouldBeListNotVariable() throws ParsingException, IOException {
        final Program p = getAST("src/test/fixtures/parserRegressions/listParsedAsVariable.json");

        final var speakBlock = NodeFilteringVisitor.getBlocks(p, Speak.class).get(0);
        final Qualified speakText = (Qualified) speakBlock.getText().getChildren().get(0);

        assertInstanceOf(ScratchList.class, speakText.getSecond());
    }

    @Test
    void acceptVariableNoShadowInput() throws ParsingException, IOException {
        final Program p = getAST("src/test/fixtures/parserRegressions/acceptVariableInputsHackedShadow.json");

        final var equalsBlock = NodeFilteringVisitor.getBlocks(p, Equals.class).get(0);
        final Qualified leftOperand = (Qualified) equalsBlock.getOperand1();

        assertInstanceOf(Variable.class, leftOperand.getSecond());
    }

    @Test
    void backdropNamesStartingWithRandom() throws ParsingException, IOException {
        final Program p = getAST("src/test/fixtures/parserRegressions/backdropNameStartsWithRandom.json");

        final var switchBackdropBlock = NodeFilteringVisitor.getBlocks(p, SwitchBackdropAndWait.class).get(0);
        assertInstanceOf(WithExpr.class, switchBackdropBlock.getElementChoice());
        assertInstanceOf(StrId.class, ((WithExpr) switchBackdropBlock.getElementChoice()).getExpression());

        final StrId switchToName = (StrId) ((WithExpr) switchBackdropBlock.getElementChoice()).getExpression();
        assertThat(switchToName.getName()).isEqualTo("random backdrop1");

        assertThat(NodeFilteringVisitor.getBlocks(p, Random.class)).isEmpty();
    }
}
