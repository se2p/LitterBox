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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.MultiBlockIssue;
import de.uni_passau.fim.se2.litterbox.analytics.clonedetection.CodeClone;

public class ClonedCodeType2 extends ClonedCode {

    public ClonedCodeType2() {
        super(CodeClone.CloneType.TYPE2, "clone_type_2");
    }

    @Override
    protected boolean compareNodes(MultiBlockIssue issue1, MultiBlockIssue issue2) {
        return issue1.getNormalizedNodes().equals(issue2.getNormalizedNodes());
    }
}
