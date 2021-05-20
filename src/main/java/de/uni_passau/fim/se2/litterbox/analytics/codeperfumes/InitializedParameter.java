package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;

import java.util.ArrayList;
import java.util.List;


/**
 * When custom blocks are created the user can define parameters, which can then be used in the body of the custom
 * block. The program only works as intended as long as the parameter is initialized. This is the solution pattern for
 * the bug pattern "Orphaned Parameter".
 */
public class InitializedParameter extends AbstractIssueFinder {
    public static final String NAME = "initialized_parameter";
    private List<ParameterDefinition> currentParameterDefinitions;
    private boolean insideProcedure;
    private List<String> checkedList;


    @Override
    public void visit(ActorDefinition actor) {
        currentParameterDefinitions = new ArrayList<>();
        checkedList = new ArrayList<>();
        super.visit(actor);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentParameterDefinitions = node.getParameterDefinitionList().getParameterDefinitions();
        checkedList = new ArrayList<>();
        super.visit(node);
        insideProcedure = false;
    }

    @Override
    public void visit(Parameter node) {
        if (insideProcedure && !checked(node.getName().getName())) {
            checkParameterNames(node.getName().getName(), node);
            checkedList.add(node.getName().getName());
        }
        visitChildren(node);
    }

    private void checkParameterNames(String name, Parameter node) {
        for (int i = 0; i < currentParameterDefinitions.size(); i++) {
            if (name.equals(currentParameterDefinitions.get(i).getIdent().getName())) {
                addIssue(node, node.getMetadata(), IssueSeverity.LOW);
                break;
            }
        }
    }

    private boolean checked(String name) {
        for (String parameter : checkedList) {
            if (parameter.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
