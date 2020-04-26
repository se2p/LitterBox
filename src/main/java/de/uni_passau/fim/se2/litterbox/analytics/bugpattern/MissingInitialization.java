package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.dataflow.*;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

public class MissingInitialization implements IssueFinder {

    public static final String NAME = "missing_initialization";
    public static final String SHORT_NAME = "mssInit"; // TODO: Why do we need this?

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);

        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        program.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();

        DataflowAnalysisBuilder<Definition> builder = new DataflowAnalysisBuilder<>(cfg);

        // Initial definitions: All definitions that can be reached without a use before them
        DataflowAnalysis<Definition> analysis = builder.withBackward().withMay().withTransferFunction(new InitialDefinitionTransferFunction()).build();
        analysis.applyAnalysis();
        Set<Definition> initialDefinitions = analysis.getDataflowFacts(cfg.getEntryNode());

        // Initial uses: All uses that can be reached without a definition before them
        DataflowAnalysis<Use> livenessAnalysis = builder.withBackward().withMay().withTransferFunction(new LivenessTransferFunction()).build();
        livenessAnalysis.applyAnalysis();
        Set<Use> initialUses = livenessAnalysis.getDataflowFacts(cfg.getEntryNode());

        int violations = 0;
        for(Use use : initialUses) {
            // If there are no initial definitions of the same defineable in other scripts it's an anomaly
            if(initialDefinitions.stream()
                    .filter(d -> d.getDefinable().equals(use.getDefinable()))
                    .noneMatch(d -> d.getDefinitionSource().getScriptOrProcedure() != use.getUseTarget().getScriptOrProcedure())) {
                violations++;
                // TODO: Store more information here
            }
        }
        return new IssueReport(NAME, violations, Collections.emptyList(), "");
    }

    @Override
    public String getName() {
        return NAME;
    }

}
