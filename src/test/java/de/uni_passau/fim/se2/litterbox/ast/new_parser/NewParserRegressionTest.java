package de.uni_passau.fim.se2.litterbox.ast.new_parser;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeFilteringVisitor;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class NewParserRegressionTest implements JsonTest {

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
}
