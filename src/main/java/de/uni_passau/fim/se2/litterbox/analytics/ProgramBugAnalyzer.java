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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ProgramBugAnalyzer implements ProgramAnalyzer<Set<Issue>> {
    private final List<IssueFinder> issueFinders;
    private final boolean ignoreLooseBlocks;

    public ProgramBugAnalyzer(final String detectors, final boolean ignoreLooseBlocks) {
        this.issueFinders = IssueTool.getFinders(detectors);
        this.ignoreLooseBlocks = ignoreLooseBlocks;
    }

    @Override
    public Set<Issue> analyze(Program program) {
        Preconditions.checkNotNull(program);
        Set<Issue> issues = new LinkedHashSet<>();
        for (IssueFinder issueFinder : issueFinders) {
            issueFinder.setIgnoreLooseBlocks(ignoreLooseBlocks);
            issues.addAll(issueFinder.check(program));
        }
        return issues;
    }
}
