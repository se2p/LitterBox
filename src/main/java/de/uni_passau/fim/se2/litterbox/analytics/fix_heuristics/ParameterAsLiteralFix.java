/*
 * Copyright (C) 2019-2026 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ParameterAsLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

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

    private final ParameterAsLiteral relatedBugFinder = new ParameterAsLiteral();

    public ParameterAsLiteralFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        parametersInScope = null;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public Set<FixResult> check(Program program) {
        Set<FixResult> results = super.check(program);
        if (!results.isEmpty()) {
            // Fix found by visitation or deletion check in super
            return results;
        }

        // If the block exists (so not empty) but was not visited (because type changed from StringLiteral),
        // we consider it fixed.
        if (getBugLocationBlockId() != null) {
            BlockIdNodeFinder.find(program, getBugLocationBlockId()).ifPresent(ctx -> {
                if (!(ctx.node() instanceof StringLiteral)) {
                    addFixWithLooseComment();
                }
            });
        }

        return Collections.unmodifiableSet(fixes);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        parametersInScope = node.getParameterDefinitionList().getParameterDefinitions().stream()
                .map(p -> p.getIdent().getName())
                .collect(Collectors.toSet());

        visitChildren(node);

        parametersInScope = null;
    }

    @Override
    public void visit(StringLiteral node) {
        if (matchesBugLocation(node)) {
            // Check if parent is StrId (identifier context), ignore if so
            if (node.getParentNode() instanceof StrId) {
                return;
            }
            // Only report fix if inside a procedure AND text no longer matches parameter
            // If not in procedure scope, don't report (the code was moved/deleted, not fixed)
            if (parametersInScope != null) {
                if (!parametersInScope.contains(node.getText())) {
                    addFix(node, node.getMetadata());
                }
            }
        }
    }
}
