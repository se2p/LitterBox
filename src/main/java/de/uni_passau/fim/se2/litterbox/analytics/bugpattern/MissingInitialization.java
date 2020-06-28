/*
 * Copyright (C) 2020 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.Definition;
import de.uni_passau.fim.se2.litterbox.cfg.Use;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysis;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysisBuilder;
import de.uni_passau.fim.se2.litterbox.dataflow.InitialDefinitionTransferFunction;
import de.uni_passau.fim.se2.litterbox.dataflow.LivenessTransferFunction;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import java.util.LinkedHashSet;
import java.util.Set;

public class MissingInitialization implements IssueFinder {

    public static final String NAME = "missing_initialization";
    public static final String SHORT_NAME = "mssInit"; // TODO: Why do we need this?
    private Set<Issue> issues = new LinkedHashSet<>();

    @Override
    public Set<Issue> check(Program program) {
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
                // TODO: Fix cast!
                issues.add(new Issue(this, use.getUseTarget().getActor(), (AbstractNode) use.getUseTarget().getASTNode()));
            }
        }
        return issues;
    }

    @Override
    public String getName() {
        return NAME;
    }

}
