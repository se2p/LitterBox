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
package de.uni_passau.fim.se2.litterbox.analytics.questions;

/**
 * The scope of focus in the block model matrix for code comprehension questions.
 * This represents the granularity or size of code being considered.
 */
public enum ScopeOfFocus {
    /**
     * Individual elements (single blocks, variables, literals).
     */
    ATOM,

    /**
     * Groups of related statements (loop body, condition branch).
     */
    BLOCK,

    /**
     * Connections between parts (how components interact).
     */
    RELATION,

    /**
     * Whole program or large sections.
     */
    MACRO
}
