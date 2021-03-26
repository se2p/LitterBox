package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AvgVariableLengthCount<T extends ASTNode> implements ScratchVisitor, MetricExtractor<T> {
    public static final String NAME = "avg_variable_length_count";
    private boolean insideScript = false;
    private boolean insideProcedure = false;
    private List<String> variables;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        double count = 0;
        this.variables = new ArrayList<>();
        insideProcedure = false;
        insideScript = false;
        node.accept(this);
        count = getAvgVariableLengthCount();
        return count;
    }

    private double getAvgVariableLengthCount() {
        List<String> allVariables = getVariables();
        Set<String> variables = new HashSet<String>(allVariables);
        int wordLength = 0;
        for (String var : variables) { // loop through the list of strings
            wordLength = wordLength + var.length(); // store the individual length of each string
        }
        double varLen = 0;
        if (variables.size() > 0) {
            varLen = (float) wordLength / variables.size();
        } else {
            varLen = 0;
        }

        return varLen;
    }
    @Override
    public void visit(Variable node) {
        if (insideScript || insideProcedure) {
            this.variables.add(node.getName().getName());
        }
    }

    @Override
    public void visit(ScratchList node) {
        if (insideScript || insideProcedure) {
            this.variables.add(node.getName().getName());
        }
    }
    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        insideScript = false;
        visitChildren(node);
        insideProcedure = false;
    }

    @Override
    public void visit(Script node) {
        insideScript = true;
        insideProcedure = false;
        visitChildren(node);
        insideScript = false;
    }

    @Override
    public void visit(Stmt node) {
        if (!(insideProcedure || insideScript)) {
            return;
        }
        visitChildren(node);
    }

    public List<String> getVariables() {
        return this.variables;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
