package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks if there are any unused parameters in one of the custom blocks.
 */
public class UnusedParameter extends AbstractIssueFinder {
    public static final String NAME = "unused_parameter";
    private boolean insideProcedure;
    private List<String> usedParameterNames;

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        usedParameterNames = new ArrayList<>();
        super.visit(node);
        List<ParameterDefinition> parameterDefinitions = node.getParameterDefinitionList().getParameterDefinitions();
        for (ParameterDefinition def : parameterDefinitions) {
            if (!usedParameterNames.contains(def.getIdent().getName())) {
                addIssue(def, def.getMetadata());
            }
        }
        insideProcedure = false;
    }

    @Override
    public void visit(Parameter node) {
        if (insideProcedure) {
            usedParameterNames.add(node.getName().getName());
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
