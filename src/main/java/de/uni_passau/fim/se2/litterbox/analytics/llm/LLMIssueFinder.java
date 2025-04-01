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
package de.uni_passau.fim.se2.litterbox.analytics.llm;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class LLMIssueFinder implements IssueFinder {

    private static final String NAME = "llm_issue_finder";

    private IssueType issueType;

    public LLMIssueFinder(IssueType issueType) {
        this.issueType = issueType;
    }

    @Override
    public IssueType getIssueType() {
        return issueType;
    }

    /**
     * Checks the given program for a specific issue.
     *
     * @param program The project to check
     * @return a IssueReport object
     */
    public Set<Issue> check(Program program) {
        return Collections.emptySet();
    }

    public String getName() {
        return NAME;
    }

    public Collection<String> getHintKeys() {
        return Collections.emptySet();
    }

    public void setIgnoreLooseBlocks(boolean value) {
        // ignore
    }

    public boolean isDuplicateOf(Issue first, Issue other) {
        return false;
    }

    public int getDistanceTo(Issue first, Issue other) {
        return 1;
    }

    public boolean isSubsumedBy(Issue first, Issue other) {
        return false;
    }

    public boolean areCoupled(Issue first, Issue other) {
        return false;
    }
}
