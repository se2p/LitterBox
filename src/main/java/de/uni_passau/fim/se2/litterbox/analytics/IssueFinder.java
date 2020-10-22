/*
 * Copyright (C) 2020 LitterBox contributors
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

import java.util.Collection;
import java.util.Set;

/**
 * Interface for all IssueFinders.
 */
public interface IssueFinder {

    IssueType getIssueType();

    /**
     * Checks the given program for a specific issue.
     *
     * @param program The project to check
     * @return a IssueReport object
     */
    Set<Issue> check(Program program);

    String getName();

    Collection<String> getHintKeys();

    void setIgnoreLooseBlocks(boolean value);
}
