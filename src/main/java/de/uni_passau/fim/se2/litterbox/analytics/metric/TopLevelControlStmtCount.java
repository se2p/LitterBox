package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class TopLevelControlStmtCount implements MetricExtractor, ScratchVisitor {
    public static final String NAME = "top_level_control_stmt_count";

    private int count = 0;

    @Override
    public double calculateMetric(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        program.accept(this);
        return count;
    }

    @Override
    public void visit(ControlStmt node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (node.getParentNode().getParentNode() instanceof Script || node.getParentNode().getParentNode() instanceof ProcedureDefinition) {
            count++;
            visitChildren(node);
        }
    }

    @Override
    public void visit(WaitUntil node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (node.getParentNode().getParentNode() instanceof Script || node.getParentNode().getParentNode() instanceof ProcedureDefinition) {
            count++;
        }
    }

    @Override
    public void visit(WaitSeconds node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (node.getParentNode().getParentNode() instanceof Script || node.getParentNode().getParentNode() instanceof ProcedureDefinition) {
            count++;
        }
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (node.getParentNode().getParentNode() instanceof Script || node.getParentNode().getParentNode() instanceof ProcedureDefinition) {
            count++;
        }
    }

    @Override
    public void visit(TerminationStmt node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (node.getParentNode().getParentNode() instanceof Script || node.getParentNode().getParentNode() instanceof ProcedureDefinition) {
            count++;
        }
    }

    @Override
    public void visit(CreateCloneOf node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (node.getParentNode().getParentNode() instanceof Script || node.getParentNode().getParentNode() instanceof ProcedureDefinition) {
            count++;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
