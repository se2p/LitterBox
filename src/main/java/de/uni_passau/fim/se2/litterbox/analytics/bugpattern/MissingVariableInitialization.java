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

        DataflowAnalysisBuilder<Definition> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Definition> analysis = builder.withForward().withMay().withTransferFunction(new ReachingDefinitionsTransferFunction()).build();
        analysis.applyAnalysis();

        int violations = 0;
        for(CFGNode node : cfg.getNodes()) {
            Set<Use> usedVariables = node.getUses();
            Set<Definition> facts = analysis.getDataflowFacts(node);

            for(Use use : usedVariables) {
                // TODO: This is overly strict. We could instead check if undef can reach a use
                if(facts.stream().noneMatch(f -> f.getDefinable().equals(use.getDefinable()))) {
                    // If no definitions are available
                    violations++;
                }
            }
        }

        // TODO: Add positions
        return new IssueReport(NAME, violations, Collections.emptyList(), "");
    }

    @Override
    public String getName() {
        return NAME;
    }
}
