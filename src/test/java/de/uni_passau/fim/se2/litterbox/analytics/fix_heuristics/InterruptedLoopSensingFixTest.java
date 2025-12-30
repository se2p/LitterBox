package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseX;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class InterruptedLoopSensingFixTest {

    private NonDataBlockMetadata createMetadata(String blockId) {
        return new NonDataBlockMetadata("", blockId, false, new NoMutationMetadata());
    }
    
    // Subclass to expose results
    private static class TestableInterruptedLoopSensingFix extends InterruptedLoopSensingFix {
        public TestableInterruptedLoopSensingFix(String bugLocationBlockId) {
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
    public void testFixFound() {
        // Current behavior: If the block exists, it reports Fix.
        
        TestableInterruptedLoopSensingFix heuristic = new TestableInterruptedLoopSensingFix("buggy");
        NonDataBlockMetadata buggyMeta = createMetadata("buggy");
        NonDataBlockMetadata emptyMeta = createMetadata(null);

        // Create WaitSeconds with buggy ID
        WaitSeconds wait = new WaitSeconds(new MouseX(emptyMeta), buggyMeta);
        
        StmtList stmts = new StmtList(wait);
        wait.setParentNode(stmts);
        
        Script script = createScript(stmts);
        
        heuristic.visit(script);

        Assertions.assertEquals(1, heuristic.getFoundIssues().size(), "Should report fix if block exists (Current Behavior)");
    }
    
    @Test
    public void testSilentOnDeletion() {
        // If block with ID does not exist (e.g. removed), no check passes, so no issue (Silent).
        // Since Fix means "Issue Added", no issue means "No Fix".
        // But for deletion case, usually we want "Silent"?
        // Or if deletion IS the fix?
        // If deletion is the fix, then finding NOTHING means Heuristic fails to report fix.
        // So this heuristic FAILS on deletion.
        
        TestableInterruptedLoopSensingFix heuristic = new TestableInterruptedLoopSensingFix("buggy");
        NonDataBlockMetadata buggyMeta = createMetadata("buggy");
        NonDataBlockMetadata otherMeta = createMetadata("other");
        NonDataBlockMetadata emptyMeta = createMetadata(null);

        // Create different block
        WaitSeconds wait = new WaitSeconds(new MouseX(emptyMeta), otherMeta);
        
        StmtList stmts = new StmtList(wait);
        wait.setParentNode(stmts);
        
        Script script = createScript(stmts);
        
        heuristic.visit(script);

        Assertions.assertTrue(heuristic.getFoundIssues().isEmpty(), "Should be silent if block ID mismatch/missing");
    }
}
