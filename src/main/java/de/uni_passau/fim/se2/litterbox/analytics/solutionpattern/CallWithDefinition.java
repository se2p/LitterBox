package de.uni_passau.fim.se2.litterbox.analytics.solutionpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom blocks can only have an effect if they are (still) defined. This is the solution for the "Call Without
 * Definition" bug pattern.
 */
public class CallWithDefinition extends AbstractIssueFinder {

    public static final String NAME = "call_with_definition";
    private List<String> proceduresDef;
    private List<CallStmt> calledProcedures;

    private void checkCalls() {
        for (CallStmt calledProcedure : calledProcedures) {
            if (proceduresDef.contains(calledProcedure.getIdent().getName())
                    || program.getProcedureMapping().checkIfMalformated(
                    currentActor.getIdent().getName() + calledProcedure.getIdent().getName())) {

                addIssue(calledProcedure, calledProcedure.getMetadata(), IssueSeverity.LOW);
            }
        }
    }

    @Override
    public void visit(ActorDefinition actor) {
        calledProcedures = new ArrayList<>();
        proceduresDef = new ArrayList<>();
        super.visit(actor);
        checkCalls();
    }

    @Override
    public void visit(ProcedureDefinition node) {
        proceduresDef.add(procMap.get(node.getIdent()).getName());
        super.visit(node);
    }

    @Override
    public void visit(CallStmt node) {
        calledProcedures.add(node);
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SOLUTION;
    }
}
