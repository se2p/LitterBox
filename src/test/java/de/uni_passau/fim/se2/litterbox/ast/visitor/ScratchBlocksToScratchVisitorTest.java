package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ScratchblocksLexer;
import de.uni_passau.fim.se2.litterbox.ScratchblocksParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ScratchBlocksToScratchVisitorTest {

    @Test
    public void test() throws IOException {
        ScratchblocksLexer lexer = new ScratchblocksLexer(CharStreams.fromString("move (10) steps"));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ScratchblocksParser parser = new ScratchblocksParser(tokens);
        ParseTree tree = parser.actor();

        ScratchBlocksToScratchVisitor vis = new ScratchBlocksToScratchVisitor();
        vis.visit(tree);
    }
}
