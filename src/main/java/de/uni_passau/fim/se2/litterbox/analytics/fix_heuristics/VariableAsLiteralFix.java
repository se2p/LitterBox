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

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.VariableAsLiteral;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fix heuristic for the VariableAsLiteral bug.
 *
 * <p>The bug is reported when a string literal matches a variable name.
 * The bug is fixed if the literal no longer matches any variable name in scope.
 */
public class VariableAsLiteralFix extends AbstractFixHeuristic {

    private Map<String, VariableInfo> varMap;
    private Map<String, ExpressionListInfo> listMap;
    private Set<String> variablesInScope;

    private final IssueFinder relatedBugFinder = new VariableAsLiteral();

    public VariableAsLiteralFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }


    @Override
    public void visit(Program program) {
        varMap = program.getSymbolTable().getVariables();
        listMap = program.getSymbolTable().getLists();
        super.visit(program);
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        String actorName = currentActor.getIdent().getName();

        variablesInScope = varMap.values().stream()
                .filter(v -> v.global() || v.actor().equals(actorName))
                .map(VariableInfo::variableName)
                .collect(Collectors.toSet());

        variablesInScope.addAll(listMap.values().stream()
                .filter(v -> v.global() || v.actor().equals(actorName))
                .map(ExpressionListInfo::variableName)
                .collect(Collectors.toSet()));

        super.visit(actor);
    }

    @Override
    public void visit(StringLiteral node) {
        if (matchesBugLocation(node)) {
            // Check if parent is StrId (identifier context), ignore if so
            if (node.getParentNode() instanceof de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId) {
                super.visit(node);
                return;
            }
            // Bug finder only checks when inside script or procedure context
            // Only report fix if we're in a valid context AND the text no longer matches
            if ((currentScript != null || currentProcedure != null)
                    && variablesInScope != null
                    && !variablesInScope.contains(node.getText())) {
                addIssue(node, node.getMetadata());
            }
        }
        super.visit(node);
    }
}
