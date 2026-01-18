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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.VariableAsLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fix heuristic for the VariableAsLiteral bug.
 *
 * <p>The bug is reported when a string literal matches a variable name.
 * The bug is fixed if the literal no longer matches any variable name in scope.
 */
public class VariableAsLiteralFix extends AbstractFixHeuristic {

    private Map<String, VariableInfo> variableInfoMap;
    private Map<String, ExpressionListInfo> listInfoMap;
    private Set<String> variablesInScope;

    private final VariableAsLiteral relatedBugFinder = new VariableAsLiteral();

    public VariableAsLiteralFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        variablesInScope = null;
        variableInfoMap = null;
        listInfoMap = null;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public Set<FixResult> check(Program program) {
        if (program.getSymbolTable() != null) {
            variableInfoMap = program.getSymbolTable().getVariables();
            listInfoMap = program.getSymbolTable().getLists();
        } else {
            variableInfoMap = Collections.emptyMap();
            listInfoMap = Collections.emptyMap();
        }
        this.program = program;

        // Note: We intentionally avoid calling super.check(program) here to bypass
        // the potentially unreliable deletion check in AbstractFixHeuristic (AstNodeUtil.hasBlockId).
        // Instead, we use BlockIdNodeFinder which is more robust.

        // 1. Check for deletion or type change
        Optional<BlockIdNodeFinder.NodeContext> context = BlockIdNodeFinder.find(program, getBugLocationBlockId());

        if (getBugLocationBlockId() != null) {
            if (context.isEmpty()) {
                // Block deleted
                addFixWithLooseComment();
                return Collections.unmodifiableSet(fixes);
            }

            if (!(context.get().node() instanceof StringLiteral)) {
                // Type changed (e.g. reverted to parameter/variable)
                BlockIdNodeFinder.NodeContext ctx = context.get();
                addFix(ctx.node(), ctx.node().getMetadata(),
                        ctx.actor(),
                        ctx.script() != null ? ctx.script() : ctx.procedure());
                return Collections.unmodifiableSet(fixes);
            }
        }

        // 2. Visit to find semantic fix (variable not in scope)
        visit(program);

        return Collections.unmodifiableSet(fixes);
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        if (currentActor.getIdent() == null) {
            super.visit(actor);
            return;
        }
        String actorName = currentActor.getIdent().getName();

        variablesInScope = variableInfoMap.values().stream()
                .filter(v -> v.global() || (v.actor() != null && v.actor().equals(actorName)))
                .map(VariableInfo::variableName)
                .collect(Collectors.toSet());

        variablesInScope.addAll(listInfoMap.values().stream()
                .filter(v -> v.global() || (v.actor() != null && v.actor().equals(actorName)))
                .map(ExpressionListInfo::variableName)
                .collect(Collectors.toSet()));

        super.visit(actor);
    }

    @Override
    public void visit(StringLiteral node) {
        if (matchesBugLocation(node)) {
            // Check if parent is StrId (identifier context), ignore if so
            if (node.getParentNode() instanceof StrId) {
                visitChildren(node);
                return;
            }
            // Bug finder only checks when inside script or procedure context
            // Only report fix if we're in a valid context AND the text no longer matches
            if ((currentScript != null || currentProcedure != null)
                    && variablesInScope != null
                    && !variablesInScope.contains(node.getText())) {
                addFix(node, node.getMetadata());
            }
        }
        visitChildren(node);
    }
}
