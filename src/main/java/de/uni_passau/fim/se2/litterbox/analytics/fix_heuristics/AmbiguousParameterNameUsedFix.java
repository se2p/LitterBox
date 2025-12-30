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

import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ArgumentInfo;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.AmbiguousParameterNameUsed;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

/**
 * Fix heuristic for the AmbiguousParameterNameUsed bug.
 *
 * <p>The bug is reported when a procedure has multiple parameters with the same name.
 * The bug is fixed if all parameter names are now unique.
 */
public class AmbiguousParameterNameUsedFix extends AbstractFixHeuristic {

    private final IssueFinder relatedBugFinder = new AmbiguousParameterNameUsed();

    public AmbiguousParameterNameUsedFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }


    @Override
    public void visit(ProcedureDefinition node) {
        super.visit(node);
        if (matchesBugLocation(node)) {
            checkProc(node);
        }
    }

    private void checkProc(ProcedureDefinition node) {
        if (node.getIdent() == null || !procMap.containsKey(node.getIdent())) {
            return;
        }
        ArgumentInfo[] arguments = procMap.get(node.getIdent()).getArguments();
        boolean ambiguousFound = false;

        for (int i = 0; i < arguments.length; i++) {
            ArgumentInfo current = arguments[i];
            for (int j = i + 1; j < arguments.length; j++) {
                if (current.name().equals(arguments[j].name())) {
                    ambiguousFound = true;
                    break;
                }
            }
            if (ambiguousFound) {
                break;
            }
        }

        if (!ambiguousFound) {
            addIssue(node, node.getMetadata());
        }
    }
}
