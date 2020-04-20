package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.dataflow.*;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

// TODO: Only implemented/tested for variables but could be applied to anything defineable
public class MissingVariableInitialization implements IssueFinder {

    public static final String NAME = "missing_variable_initialization";
    public static final String SHORT_NAME = "mssVarInit";

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);

        Map<String, VariableInfo> variableInfoMap = program.getSymbolTable().getVariables();
        ArrayList<VariableInfo> varInfos = new ArrayList<>(variableInfoMap.values());

        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        program.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();

        DataflowAnalysisBuilder<Use> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Use> analysis = builder.withBackward().withMay().withTransferFunction(new LivenessTransferFunction()).build();
        analysis.applyAnalysis();

        Set<Use> undefinedUses = analysis.getDataflowFacts(cfg.getEntryNode());
        int violations = undefinedUses.size();

        // TODO: Add positions
        return new IssueReport(NAME, violations, Collections.emptyList(), "");
    }

    @Override
    public String getName() {
        return NAME;
    }
}
