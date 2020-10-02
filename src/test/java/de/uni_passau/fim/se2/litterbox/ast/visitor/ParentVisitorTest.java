package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ParentVisitorTest implements JsonTest {
    @Test
    public void testDeletion() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/scratchblocks/motionblocks.json");
        assertNull(program.getParentNode()); // Root node has no parent
        assertParentRelation(program);
    }

    private void assertParentRelation(ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            assertSame(node, child.getParentNode());
            assertParentRelation(child);
        }
    }
}
