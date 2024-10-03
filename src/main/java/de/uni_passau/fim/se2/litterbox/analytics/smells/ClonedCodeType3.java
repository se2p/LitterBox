/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.MultiBlockIssue;
import de.uni_passau.fim.se2.litterbox.analytics.clonedetection.CodeClone;

import java.util.Set;
import java.util.stream.Collectors;

public class ClonedCodeType3 extends ClonedCode {

    public ClonedCodeType3() {
        super(CodeClone.CloneType.TYPE3, "clone_type_3");
    }

    @Override
    public boolean isSubsumedBy(Issue first, Issue other) {

        // Can be subsumed by a type 1 or 2 clone

        if (first == other) {
            // Don't check against self
            return false;
        }
        if (!(first.getFinder() instanceof ClonedCodeType3)) {
            return false;
        }

        if (other.getFinder() instanceof ClonedCodeType1 || other.getFinder() instanceof ClonedCodeType2) {
            if (!(first instanceof MultiBlockIssue mbIssue1) || !(other instanceof MultiBlockIssue mbIssue2)) {
                return false;
            }

            // If there is a type 1 or type 2 clone that covers all the nodes then it subsumes a type 3 issue

            Set<Integer> statements1 = mbIssue1.getNodes().stream().map(System::identityHashCode)
                    .collect(Collectors.toSet());
            Set<Integer> statements2 = mbIssue2.getNodes().stream().map(System::identityHashCode)
                    .collect(Collectors.toSet());

            statements1.removeAll(statements2);
            return statements1.isEmpty();
        }

        return false;
    }

    @Override
    protected boolean compareNodes(MultiBlockIssue issue1, MultiBlockIssue issue2) {
        return issue1.getNormalizedNodes().equals(issue2.getNormalizedNodes());
    }
}
