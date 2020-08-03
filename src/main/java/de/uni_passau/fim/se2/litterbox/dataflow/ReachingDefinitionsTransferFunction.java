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
package de.uni_passau.fim.se2.litterbox.dataflow;

import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.Definition;

import java.util.LinkedHashSet;
import java.util.Set;

public class ReachingDefinitionsTransferFunction implements TransferFunction<Definition> {
    @Override
    public Set<Definition> apply(CFGNode node, Set<Definition> inFacts) {
        // (inFacts \ {kill}) ∪ {gen}
        Set<Definition> result = new LinkedHashSet<>(inFacts);

        // Remove all Definitions of variables that are defined here
        Set<Definition> definitions = node.getDefinitions();
        result.removeIf(d -> definitions.contains(d));

        // Add new Definitions of variables that are defined here
        result.addAll(definitions);

        return result;
    }
}
