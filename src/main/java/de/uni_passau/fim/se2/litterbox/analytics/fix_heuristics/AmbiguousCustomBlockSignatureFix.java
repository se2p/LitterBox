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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.AmbiguousCustomBlockSignature;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Fix heuristic for the AmbiguousCustomBlockSignature bug.
 *
 * <p>The bug is reported when multiple procedures have the same name in an actor.
 * The bug is fixed if the procedure no longer has duplicates with the same name.
 */
public class AmbiguousCustomBlockSignatureFix extends AbstractFixHeuristic {

    private final AmbiguousCustomBlockSignature relatedBugFinder = new AmbiguousCustomBlockSignature();

    public AmbiguousCustomBlockSignatureFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        visitChildren(node);
        if (matchesBugLocation(node)) {
            checkProc(node);
        }
    }

    private void checkProc(ProcedureDefinition node) {
        if (node.getIdent() == null || !procMap.containsKey(node.getIdent())) {
            return;
        }

        ProcedureInfo current = procMap.get(node.getIdent());
        List<ProcedureInfo> procedureInfos = new ArrayList<>(procMap.values());
        boolean duplicateFound = false;

        for (ProcedureInfo procedureInfo : procedureInfos) {
            if (procedureInfo != current
                    && current.getName().equals(procedureInfo.getName())
                    && current.getActorName().equals(procedureInfo.getActorName())) {
                duplicateFound = true;
                break;
            }
        }

        if (!duplicateFound) {
            addFix(node, node.getMetadata());
        }
    }
}
