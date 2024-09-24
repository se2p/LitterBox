package de.uni_passau.fim.se2.litterbox.ast.new_parser;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
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
}
