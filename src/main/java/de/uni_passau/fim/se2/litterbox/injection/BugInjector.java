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
package de.uni_passau.fim.se2.litterbox.injection;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.List;

/**
 * Interface for bug injectors that mutate Scratch programs to introduce specific bug patterns.
 *
 * <p>Implementations of this interface create modified versions of programs that contain
 * bugs detectable by the associated {@link IssueFinder}. This is primarily used for
 * generating test cases, mutation testing, and validating bug detection capabilities.
 *
 * <p>Each injector is associated with a specific bug finder via {@link #getIssueFinder()},
 * ensuring that injected bugs are always detectable by the corresponding analysis.
 *
 * @see AbstractBugInjector
 * @see InjectionResult
 */
public interface BugInjector {

    /**
     * Returns a list of injection results, each containing a modified program with one instance
     * of the injected bug along with metadata about the injection.
     *
     * @param program The original program to inject bugs into.
     * @return List of injection results with mutated programs and metadata.
     */
    List<InjectionResult> inject(Program program);

    /**
     * Returns the canonical name of the bug injector.
     *
     * @return The canonical name of the bug injector.
     */
    String getName();

    /**
     * Returns the IssueFinder associated with this bug injector.
     *
     * @return The IssueFinder associated with this bug injector.
     */
    IssueFinder getIssueFinder();
}
