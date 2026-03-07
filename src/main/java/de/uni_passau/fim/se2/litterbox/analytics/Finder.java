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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.Collection;
import java.util.Set;

/**
 * Generic interface for finders that identify findings in Scratch programs.
 *
 * @param <T> the type of finding this finder produces
 */
public interface Finder<T extends Finding> {

    /**
     * Checks the given program for findings.
     *
     * @param program the project to check
     * @return a set of findings
     */
    Set<T> check(Program program);

    /**
     * Returns the unique name of this finder.
     *
     * @return the finder name
     */
    String getName();

    /**
     * Returns the hint keys used by this finder for translations.
     *
     * @return collection of hint keys
     */
    Collection<String> getHintKeys();

    /**
     * Sets whether loose (unconnected) blocks should be ignored.
     *
     * @param value true to ignore loose blocks
     */
    void setIgnoreLooseBlocks(boolean value);
}
