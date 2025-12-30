package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class InvalidNumberStringFixTest {

    private NonDataBlockMetadata createMetadata(String blockId) {
        return new NonDataBlockMetadata("", blockId, false, new NoMutationMetadata());
    }
    
    // Subclass to expose results
    private static class TestableInvalidNumberStringFix extends InvalidNumberStringFix {
        public TestableInvalidNumberStringFix(String bugLocationBlockId) {
            super(bugLocationBlockId);
        }

        public Set<Issue> getFoundIssues() {
            return this.issues;
        }
    }

    private Script createScript(StmtList stmts) {
        NonDataBlockMetadata emptyMeta = createMetadata(null);
        StartedAsClone event = new StartedAsClone(emptyMeta);
        Script script = new Script(event, stmts);
        event.setParentNode(script);
        stmts.setParentNode(script);
        return script;
    }

    @Test
    public void testFixFoundValidNumber() {
        // Fix: String is a valid number ("123.45")
        
        TestableInvalidNumberStringFix heuristic = new TestableInvalidNumberStringFix("buggy");
        NonDataBlockMetadata buggyMeta = createMetadata("buggy");
        
        StringLiteral str = new StringLiteral("123.45");
        // Use anonymous subclass to inject metadata
        AsNumber asNumber = new AsNumber(str) {
            @Override
            public BlockMetadata getMetadata() {
                return buggyMeta;
            }
        };
        str.setParentNode(asNumber);
        
        // Wrap in a statement to be part of a script (MoveSteps takes NumExpr)
        MoveSteps move = new MoveSteps(asNumber, createMetadata(null));
        asNumber.setParentNode(move);
        
        StmtList stmts = new StmtList(move);
        move.setParentNode(stmts);
        
        Script script = createScript(stmts);
        
        heuristic.visit(script);

        Assertions.assertEquals(1, heuristic.getFoundIssues().size(), "Should find fix if string is a valid number");
    }

    @Test
    public void testFixFoundNotAllowedChars() {
        // Fix: String contains characters NOT allowed in numbers ("abc"), so it's clearly not a number format error, just not a number.
        // Heuristic logic: if (ALLOWED_CHARS matches AND !VALID_NUMBER matches) -> fixed=false (Bug).
        // If !ALLOWED_CHARS matches -> fixed=true.
        
        TestableInvalidNumberStringFix heuristic = new TestableInvalidNumberStringFix("buggy");
        NonDataBlockMetadata buggyMeta = createMetadata("buggy");
        
        StringLiteral str = new StringLiteral("abc"); // "abc" does not match ALLOWED_CHARS (0-9eE.+-).
        AsNumber asNumber = new AsNumber(str) {
            @Override
            public BlockMetadata getMetadata() {
                return buggyMeta;
            }
        };
        str.setParentNode(asNumber);
        
        MoveSteps move = new MoveSteps(asNumber, createMetadata(null));
        asNumber.setParentNode(move);
        
        StmtList stmts = new StmtList(move);
        move.setParentNode(stmts);
        
        Script script = createScript(stmts);
        
        heuristic.visit(script);

        Assertions.assertEquals(1, heuristic.getFoundIssues().size(), "Should find fix if string does not look like a number (not allowed chars)");
    }

    @Test
    public void testNoFixInvalidNumberString() {
        // No Fix: String matches ALLOWED_CHARS but is NOT VALID_NUMBER ("12..34")
        
        TestableInvalidNumberStringFix heuristic = new TestableInvalidNumberStringFix("buggy");
        NonDataBlockMetadata buggyMeta = createMetadata("buggy");
        
        StringLiteral str = new StringLiteral("12..34"); // Matches ALLOWED, but invalid number
        AsNumber asNumber = new AsNumber(str) {
            @Override
            public BlockMetadata getMetadata() {
                return buggyMeta;
            }
        };
        str.setParentNode(asNumber);
        
        MoveSteps move = new MoveSteps(asNumber, createMetadata(null));
        asNumber.setParentNode(move);
        
        StmtList stmts = new StmtList(move);
        move.setParentNode(stmts);
        
        Script script = createScript(stmts);
        
        heuristic.visit(script);

        Assertions.assertTrue(heuristic.getFoundIssues().isEmpty(), "Should NOT find fix if string is invalid number format (12..34)");
    }
}
