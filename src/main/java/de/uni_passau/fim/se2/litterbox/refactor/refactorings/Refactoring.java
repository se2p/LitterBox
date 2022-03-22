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
package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

/**
 * Interface for all Refactorings.
 */
public interface Refactoring {

    /**
     * Apply the refactoring onto the given program and return the new modified program tree. Note to only apply the
     * refactoring after a deep copy of the original program was made with the {@code deepCopy()} method to avoid
     * transformations on the original program tree!
     *
     * @param program The program before the refactoring.
     * @return The refactored program.
     */
    Program apply(Program program);

    String getName();
}
