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

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysis;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysisBuilder;
import de.uni_passau.fim.se2.litterbox.dataflow.InitialDefinitionTransferFunction;
import de.uni_passau.fim.se2.litterbox.dataflow.LivenessTransferFunction;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

public class MissingInitialization implements IssueFinder {

    public static final String NAME = "missing_initialization";

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
            // If there are no initial definitions of the same defineable in other scripts it's an anomaly
            if (initialDefinitions.stream()
                    .filter(d -> d.getDefinable().equals(use.getDefinable()))
                    .noneMatch(d -> d.getDefinitionSource().getScriptOrProcedure()
                            != use.getUseTarget().getScriptOrProcedure())) {

                Hint hint = new Hint(getName());
                hint.setParameter(Hint.HINT_VARIABLE, getDefineableName(use.getDefinable()));
                // TODO: The comment is attached to the statement, not the actual usage...
                ASTNode containingScript = use.getUseTarget().getScriptOrProcedure();
                if (containingScript instanceof Script) {
                    issues.add(new Issue(this, IssueSeverity.HIGH, program, use.getUseTarget().getActor(),
                            (Script) containingScript,
                            use.getUseTarget().getASTNode(),
                            null,  // TODO: Where is the relevant metadata?
                            hint));
                } else {
                    issues.add(new Issue(this, IssueSeverity.HIGH, program, use.getUseTarget().getActor(),
                            (ProcedureDefinition) containingScript,
                            use.getUseTarget().getASTNode(),
                            null, // TODO: Where is the relevant metadata
                            hint));
                }
            }
        }
        return Collections.unmodifiableSet(issues);
    }

    // TODO: Clean this up
    public String getDefineableName(Defineable def) {
        String result = "";
        if (def instanceof Variable) {
            result = IssueTranslator.getInstance().getInfo(IssueTranslator.VARIABLE);
            result += " \"";
            Variable var = (Variable)def;
            if (var.getIdentifier() instanceof LocalIdentifier) {
                result += ((LocalIdentifier)var.getIdentifier()).getName();
            } else {
                result += ((Qualified)var.getIdentifier()).getSecond().getName().getName();
            }
            result += "\"";

        } else if (def instanceof ListVariable) {
            result = IssueTranslator.getInstance().getInfo(IssueTranslator.LIST);
            result += " \"";
            ListVariable var = (ListVariable)def;
            if (var.getIdentifier() instanceof LocalIdentifier) {
                result += ((LocalIdentifier)var.getIdentifier()).getName();
            } else {
                result += ((Qualified)var.getIdentifier()).getSecond().getName().getName();
            }
            result += "\"";

        } else if (def instanceof Attribute) {
            result = IssueTranslator.getInstance().getInfo(IssueTranslator.ATTRIBUTE);
            result += " \"";
            Attribute attr = (Attribute)def;
            switch (attr.getAttributeType()) {
                case SIZE:
                    result += IssueTranslator.getInstance().getInfo(IssueTranslator.SIZE);
                    break;
                case COSTUME:
                    result += IssueTranslator.getInstance().getInfo(IssueTranslator.COSTUME);
                    break;
                case POSITION:
                    result += IssueTranslator.getInstance().getInfo(IssueTranslator.POSITION);
                    break;
                case ROTATION:
                    result += IssueTranslator.getInstance().getInfo(IssueTranslator.ROTATION);
                    break;
            }
            result += "\"";

        }
        return result;
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
    public boolean isDuplicateOf(Issue first, Issue other) {
        // TODO: Implement
        return false;
    }

    @Override
    public boolean isSubsumedBy(Issue first, Issue other) {
        // TODO: Implement
        return false;
    }

    @Override
    public Collection<String> getHintKeys() {
        // Default: Only one key with the name of the finder
        return Arrays.asList(getName());
    }
}
