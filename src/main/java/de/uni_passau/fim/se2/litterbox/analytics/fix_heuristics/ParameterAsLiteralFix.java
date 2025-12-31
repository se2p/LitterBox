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

import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ParameterAsLiteral;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fix heuristic for the ParameterAsLiteral bug.
 *
 * <p>The bug is reported when a string literal matches a parameter name.
 * The bug is fixed if the literal no longer matches any parameter name.
 */
public class ParameterAsLiteralFix extends AbstractFixHeuristic {

    private Set<String> parametersInScope;

    private final IssueFinder relatedBugFinder = new ParameterAsLiteral();

    public ParameterAsLiteralFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public Set<Issue> check(Program program) {
        // If the original buggy block (StringLiteral) is gone, we consider it fixed.
        java.util.Optional<BlockIdNodeFinder.NodeContext> foundOpt = BlockIdNodeFinder.find(program, bugLocationBlockId);
        
        if (bugLocationBlockId != null) {
            if (foundOpt.isEmpty()) {
                addIssueWithLooseComment();
                return Collections.unmodifiableSet(issues);
            }
            
            // Also fixed if the node exists but is no longer a StringLiteral (e.g. reverted to Parameter)
            if (!(foundOpt.get().node() instanceof StringLiteral)) {
                 addIssueWithLooseComment();
                 return Collections.unmodifiableSet(issues);
            }
        }
        
        return super.check(program);
    }


    @Override
    public void visit(ProcedureDefinition node) {
        parametersInScope = node.getParameterDefinitionList().getParameterDefinitions().stream()
                .map(p -> p.getIdent().getName())
                .collect(Collectors.toSet());

        super.visit(node);

        parametersInScope = null;
    }

    @Override
    public void visit(StringLiteral node) {
        if (matchesBugLocation(node)) {
            // Check if parent is StrId (identifier context), ignore if so
            if (node.getParentNode() instanceof de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId) {
                return;
            }
            // Only report fix if inside a procedure AND text no longer matches parameter
            // If not in procedure scope, don't report (the code was moved/deleted, not fixed)
            if (parametersInScope != null) {
                if (!parametersInScope.contains(node.getText())) {
                    addIssue(node, node.getMetadata());
                }
            }
        } else {
             // System.out.println("Node mismatch: " + node + " ID: " + node.getMetadata().getBlockId() + " Expected: " + bugLocationBlockId);
        }
    }
}
