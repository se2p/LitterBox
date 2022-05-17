/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysis;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysisBuilder;
import de.uni_passau.fim.se2.litterbox.dataflow.InitialDefinitionTransferFunction;
import de.uni_passau.fim.se2.litterbox.dataflow.LivenessTransferFunction;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

public class MissingInitialization extends AbstractIssueFinder {

    private static class UseIssue extends Issue {

        private Use use;

        public UseIssue(IssueFinder finder, Program program, Script script, Hint hint, Use use) {
            super(finder, IssueSeverity.HIGH, program,
                    use.getUseTarget().getActor(), script,
                    use.getUseTarget().getASTNode(),
                    use.getUseTarget().getASTNode().getMetadata(), hint);
            this.use = use;
        }

        public UseIssue(IssueFinder finder, Program program, ProcedureDefinition procedure, Hint hint, Use use) {
            super(finder, IssueSeverity.HIGH, program,
                    use.getUseTarget().getActor(), procedure,
                    use.getUseTarget().getASTNode(),
                    use.getUseTarget().getASTNode().getMetadata(), hint);
            this.use = use;
        }

        public Use getUse() {
            return use;
        }
    }


    public static final String NAME = "missing_initialization";
    public static final String NAME_CLONE = "missing_initialization_clone";

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        Set<Issue> issues = new LinkedHashSet<>();
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        program.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();

        // Initial definitions: All definitions that can be reached without a use before them
        DataflowAnalysisBuilder<Definition> defBuilder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Definition> analysis
                = defBuilder.withBackward().withMay().withTransferFunction(
                new InitialDefinitionTransferFunction()).build();
        analysis.applyAnalysis();
        Set<Definition> initialDefinitions = analysis.getDataflowFacts(cfg.getEntryNode());

        // Initial uses: All uses that can be reached without a definition before them
        DataflowAnalysisBuilder<Use> useBuilder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Use> livenessAnalysis
                = useBuilder.withBackward().withMay().withTransferFunction(new LivenessTransferFunction()).build();
        livenessAnalysis.applyAnalysis();
        Set<Use> initialUses = livenessAnalysis.getDataflowFacts(cfg.getEntryNode());

        for (Use use : initialUses) {

            if (use.getDefinable() instanceof Attribute) {
                if (((Attribute)use.getDefinable()).getAttributeType().equals(Attribute.AttributeType.VISIBILITY)) {
                    // TODO: Handle visibility properly in a way that does not produce too many false positives
                    // e.g. only report this if there is a hide statement?
                    continue;
                }
            }

            // If there are no initial definitions of the same defineable in other scripts it's an anomaly
            if (initialDefinitions.stream()
                    .filter(d -> d.getDefinable().equals(use.getDefinable()))
                    .noneMatch(d -> d.getDefinitionSource().getScriptOrProcedure()
                            != use.getUseTarget().getScriptOrProcedure())) {

                Hint hint;
                // TODO: The comment is attached to the statement, not the actual usage...
                ASTNode containingScript = use.getUseTarget().getScriptOrProcedure();
                if (containingScript instanceof Script) {
                    if (((Script) containingScript).getEvent() instanceof StartedAsClone) {
                        hint = new Hint(NAME_CLONE);
                    } else {
                        hint = new Hint(getName());
                    }
                    hint.setParameter(Hint.HINT_VARIABLE, getDefineableName(use.getDefinable()));
                    issues.add(new UseIssue(this, program,
                            (Script) containingScript, hint, use));
                } else {
                    hint = new Hint(getName());
                    hint.setParameter(Hint.HINT_VARIABLE, getDefineableName(use.getDefinable()));
                    issues.add(new UseIssue(this, program,
                            (ProcedureDefinition) containingScript, hint, use));
                }
            }
        }
        return Collections.unmodifiableSet(issues);
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            // Don't check against self
            return false;
        }

        if (first.getFinder() != other.getFinder()) {
            // Can only be a duplicate if it's the same finder
            return false;
        }

        if (first instanceof UseIssue && other instanceof UseIssue) {
            UseIssue firstUse = (UseIssue) first;
            UseIssue otherUse = (UseIssue) other;
            Use use1 = firstUse.getUse();
            Use use2 = otherUse.getUse();
            if (use1.getDefinable().equals(use2.getDefinable())) {
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
        return IssueType.BUG;
    }

    @Override
    public void setIgnoreLooseBlocks(boolean value) {
        // Irrelevant for this finder
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(NAME);
        keys.add(NAME_CLONE);
        return keys;
    }
}
