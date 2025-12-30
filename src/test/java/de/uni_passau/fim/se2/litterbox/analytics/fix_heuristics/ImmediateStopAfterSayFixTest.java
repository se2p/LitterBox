package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseX;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class ImmediateStopAfterSayFixTest {

    private NonDataBlockMetadata createMetadata(String blockId) {
        return new NonDataBlockMetadata("", blockId, false, new NoMutationMetadata());
    }
    
    // Subclass to expose results
    private static class TestableImmediateStopAfterSayFix extends ImmediateStopAfterSayFix {
        public TestableImmediateStopAfterSayFix(String bugLocationBlockId) {
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
    public void testFixFoundNoNextStmt() {
        // Fix: Say is last statement (No subsequent StopAll)
        
        TestableImmediateStopAfterSayFix heuristic = new TestableImmediateStopAfterSayFix("buggy");
        NonDataBlockMetadata buggyMeta = createMetadata("buggy");
        
        Say say = new Say(new StringLiteral("Hello"), buggyMeta);
        StmtList stmts = new StmtList(say);
        say.setParentNode(stmts);
        
        Script script = createScript(stmts);
        
        // Traverse script to find Say and check environment
        heuristic.visit(script);

        Assertions.assertEquals(1, heuristic.getFoundIssues().size(), "Should find fix if Say is last statement");
    }

    @Test
    public void testFixFoundNextStmtNotStopAll() {
        // Fix: Next statement is MoveSteps (NOT StopAll)
        
        TestableImmediateStopAfterSayFix heuristic = new TestableImmediateStopAfterSayFix("buggy");
        NonDataBlockMetadata buggyMeta = createMetadata("buggy");
        NonDataBlockMetadata emptyMeta = createMetadata(null);
        
        Say say = new Say(new StringLiteral("Hello"), buggyMeta);
        MoveSteps move = new MoveSteps(new MouseX(emptyMeta), emptyMeta);
        
        StmtList stmts = new StmtList(say, move);
        say.setParentNode(stmts);
        move.setParentNode(stmts);
        
        Script script = createScript(stmts);
        
        heuristic.visit(script);

        Assertions.assertEquals(1, heuristic.getFoundIssues().size(), "Should find fix if next statement is not StopAll");
    }

    @Test
    public void testNoFixSayFollowedByStopAll() {
        // No Fix: Next statement *IS* StopAll
        
        TestableImmediateStopAfterSayFix heuristic = new TestableImmediateStopAfterSayFix("buggy");
        NonDataBlockMetadata buggyMeta = createMetadata("buggy");
        NonDataBlockMetadata emptyMeta = createMetadata(null);
        
        Say say = new Say(new StringLiteral("Hello"), buggyMeta);
        StopAll stop = new StopAll(emptyMeta);
        
        StmtList stmts = new StmtList(say, stop);
        say.setParentNode(stmts);
        stop.setParentNode(stmts);
        
        Script script = createScript(stmts);
        
        heuristic.visit(script);

        Assertions.assertTrue(heuristic.getFoundIssues().isEmpty(), "Should NOT find fix if next statement is StopAll");
    }

    @Test
    public void testFixFoundThinkNoNextStmt() {
        // Fix: Think is last statement
        
        TestableImmediateStopAfterSayFix heuristic = new TestableImmediateStopAfterSayFix("buggy");
        NonDataBlockMetadata buggyMeta = createMetadata("buggy");
        
        Think think = new Think(new StringLiteral("Hmm"), buggyMeta);
        StmtList stmts = new StmtList(think);
        think.setParentNode(stmts);
        
        Script script = createScript(stmts);
        
        heuristic.visit(script);

        Assertions.assertEquals(1, heuristic.getFoundIssues().size(), "Should find fix if Think is last statement");
    }
}
