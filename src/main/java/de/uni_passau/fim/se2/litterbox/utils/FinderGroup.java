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
package de.uni_passau.fim.se2.litterbox.utils;

import java.util.Arrays;
import java.util.Optional;

/**
 * Pre-defined groups of {@link de.uni_passau.fim.se2.litterbox.analytics.IssueFinder}.
 */
public enum FinderGroup {
    ALL("all"),
    SMELLS("smells"),
    BUGS("bugs"),
    BUGS_SCRIPTS("script-bugs"),
    PERFUMES("perfumes"),
    DEFAULT("default"),
    FLAWS("flaws"),
    MOST_COMMON_BUGS("most_common_bugs");

    private final String group;

    FinderGroup(final String group) {
        this.group = group;
    }

    public static Optional<FinderGroup> tryFromString(final String groupName) {
        return Arrays.stream(FinderGroup.values())
                .filter(issueType -> issueType.group().equals(groupName))
                .findAny();
    }

    public String group() {
        return group;
    }
}
