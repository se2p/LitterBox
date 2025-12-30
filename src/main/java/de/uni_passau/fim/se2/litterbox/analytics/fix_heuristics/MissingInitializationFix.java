/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.dataflow.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;

import java.util.Optional;
import java.util.Set;

/**
 * Fix heuristic for the MissingInitialization bug.
 *
 * <p>The bug is reported when a variable is used before it is initialized.
 *
 * <p>The bug is fixed if the variable use is now properly initialized before use.
 */
public class MissingInitializationFix extends AbstractFixHeuristic {
    public static final String NAME = "missing_initialization_fix";

    public MissingInitializationFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    public Set<Issue> check(Program program) {
        this.program = program;

        // Build CFG and perform dataflow analysis
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        program.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();

        // Initial definitions analysis
        DataflowAnalysisBuilder<Definition> defBuilder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Definition> defAnalysis = defBuilder
                .withBackward()
                .withMay()
                .withTransferFunction(new InitialDefinitionTransferFunction())
                .build();
        defAnalysis.applyAnalysis();
        Set<Definition> initialDefinitions = defAnalysis.getDataflowFacts(cfg.getEntryNode());

        // Initial uses (liveness) analysis
        DataflowAnalysisBuilder<Use> useBuilder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Use> livenessAnalysis = useBuilder
                .withBackward()
                .withMay()
                .withTransferFunction(new LivenessTransferFunction())
                .build();
        livenessAnalysis.applyAnalysis();
        Set<Use> initialUses = livenessAnalysis.getDataflowFacts(cfg.getEntryNode());

        // Find the target node
        Optional<BlockIdNodeFinder.NodeContext> contextOpt =
                BlockIdNodeFinder.find(program, bugLocationBlockId);

        contextOpt.ifPresent(context -> {
            // Check if this node corresponds to an initial use (still buggy)
            boolean matchesBug = false;

            for (Use use : initialUses) {
                if (use.getUseTarget().getASTNode() == context.node()) {
                    // Potential bug. Apply Bug Finder filters.
                    if (use.getDefinable() instanceof Attribute attribute) {
                        if (attribute.getAttributeType() == Attribute.AttributeType.VISIBILITY) {
                            continue; // Skip visibility
                        }
                    }

                    // Check if there's a definition in another script
                    boolean hasDefInOtherScript = initialDefinitions.stream()
                            .filter(d -> d.getDefinable().equals(use.getDefinable()))
                            .noneMatch(d -> d.getDefinitionSource().getScriptOrProcedure()
                                    != use.getUseTarget().getScriptOrProcedure());

                    if (hasDefInOtherScript) {
                        matchesBug = true;
                        break;
                    }
                }
            }

            if (!matchesBug) {
                // Fixed! The use is now properly initialized.
                IssueBuilder builder = new IssueBuilder()
                        .withFinder(this)
                        .withCurrentNode(context.node())
                        .withActor(context.actor())
                        .withProgram(program)
                        .withMetadata(context.node().getMetadata())
                        .withHint(Hint.fromKey(getName()));

                if (context.script() != null) {
                    builder.withScript(context.script());
                }
                if (context.procedure() != null) {
                    builder.withProcedure(context.procedure());
                }

                issues.add(builder.build());
            }
        });

        return issues;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
