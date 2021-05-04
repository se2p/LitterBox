package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.CallWithoutDefinition;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingLoopSensing;
import de.uni_passau.fim.se2.litterbox.analytics.smells.UnnecessaryIfAfterUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;

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
    public boolean areOpposite(Issue first, Issue other) {
        if (first.getFinder() != this) {
            return super.areOpposite(first, other);
        }
        return (other.getFinder() instanceof CallWithoutDefinition);
    }

//    @Override
//    public boolean areCoupled(Issue first, Issue other) {
//        if (first.getFinder() != this) {
//            return super.areCoupled(first, other);
//        }
//
//        if (other.getFinder() instanceof UnnecessaryIfAfterUntil) {
//            return other.getFinder().areCoupled(other, first);
//        }
//        return false;
//    }
//
//    @Override
//    public boolean areCoupled(Issue first, Issue other) {
//        if (first.getFinder() != this) {
//            return super.areCoupled(first, other);
//        }
//
//        if (other.getFinder() instanceof MissingLoopSensing) {
//            if (other.getActor().equals(first.getActor()) && other.getScriptOrProcedureDefinition().equals(first.getScriptOrProcedureDefinition())) {
//                ASTNode otherLoc = other.getCodeLocation();
//                while (!(otherLoc instanceof IfElseStmt) && !(otherLoc instanceof IfThenStmt)) {
//                    otherLoc = otherLoc.getParentNode();
//                }
//                return otherLoc.equals(first.getCodeLocation());
//            }
//        }
//        return false;
//    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
