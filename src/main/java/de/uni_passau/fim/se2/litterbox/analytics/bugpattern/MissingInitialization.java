package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.dataflow.*;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;
import java.util.stream.Collectors;

public class MissingInitialization implements IssueFinder {

    public static final String NAME = "missing_initialization";
    public static final String SHORT_NAME = "mssInit"; // TODO: Why do we need this?

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);

        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        program.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();

        DataflowAnalysisBuilder<Use> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Use> analysis = builder.withBackward().withMay().withTransferFunction(new LivenessTransferFunction()).build();
        analysis.applyAnalysis();

        // Missing initialization is only a problem if the program is started with the green flag
        Set<CFGNode> greenFlags = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof GreenFlag).collect(Collectors.toSet());
        int violations = 0;
        for(CFGNode greenFlag : greenFlags) {
            Set<Use> undefinedUses = analysis.getDataflowFacts(greenFlag);
            violations += undefinedUses.size();
        }

        // TODO: Add positions
        return new IssueReport(NAME, violations, Collections.emptyList(), "");
    }

    @Override
    public String getName() {
        return NAME;
    }
}
