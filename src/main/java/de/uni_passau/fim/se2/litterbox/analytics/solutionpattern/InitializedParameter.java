package de.uni_passau.fim.se2.litterbox.analytics.solutionpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;

import java.util.List;


/**
 * When custom blocks are created the user can define parameters, which can then be used in the body of the custom
 * block. The program only works as intended as long as the parameter is initialized. This is the solution pattern for
 *  * the bug pattern "Orphaned Parameter".
 */
public class InitializedParameter extends AbstractIssueFinder {
    public static final String NAME = "initialized_parameter";
    private List<ParameterDefinition> currentParameterDefinitions;
    private boolean insideProcedure;

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentParameterDefinitions = node.getParameterDefinitionList().getParameterDefinitions();
        super.visit(node);
        insideProcedure = false;
    }

    @Override
    public void visit(Parameter node) {
        if (insideProcedure) {
            checkParameterNames(node.getName().getName(), node);
        }
        visitChildren(node);
    }

    private void checkParameterNames(String name, Parameter node) {
        for (int i = 0; i < currentParameterDefinitions.size(); i++) {
            if (name.equals(currentParameterDefinitions.get(i).getIdent().getName())) {
                addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
                break;
            }
        }
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
