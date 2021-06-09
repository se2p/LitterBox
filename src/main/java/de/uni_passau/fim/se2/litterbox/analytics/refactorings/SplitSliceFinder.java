package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.dependency.ProgramDependenceGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SplitSlice;

import java.util.List;
import java.util.Set;

public class SplitSliceFinder extends AbstractDependencyRefactoringFinder {

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            // Unconnected blocks
            return;
        }

        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor(currentActor);
        script.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);

        List<Stmt> statements = script.getStmtList().getStmts();

        // Go backwards, create slice from last statement
        if (statements.size() > 2) {
            Set<Stmt> slice = pdg.backwardSlice(statements.get(statements.size() - 1));
            if (slice.size() < statements.size()) {
                refactorings.add(new SplitSlice(script, slice));
            }
        }
    }

    @Override
    public String getName() {
        return SplitSlice.NAME;
    }
}
