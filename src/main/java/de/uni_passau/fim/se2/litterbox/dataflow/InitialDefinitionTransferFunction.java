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
import de.uni_passau.fim.se2.litterbox.cfg.Defineable;
import de.uni_passau.fim.se2.litterbox.cfg.Definition;
import de.uni_passau.fim.se2.litterbox.cfg.Use;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class InitialDefinitionTransferFunction implements TransferFunction<Definition> {

    @Override
    public Set<Definition> apply(CFGNode node, Set<Definition> inFacts) {

        // Gen: If this var is defined here without being read
        // Kill: If this var is used here

        // LiveOut(n) = (Live(n) ∪ gen(n)) \ kill (n)
        //
        //gen(n) = { v | v is used at n }
        //kill(n) = { v | v is modified at n }
        //
        Set<Definition> result = new LinkedHashSet<>(inFacts);

        // Add new defs of variables
        Set<Definition> defs = node.getDefinitions();
        result.addAll(defs);

        // Remove all defs of variables that are used here
        Set<Defineable> uses = node.getUses().parallelStream().map(Use::getDefinable).collect(Collectors.toSet());
        if (!uses.isEmpty()) {
            result.removeIf(d -> uses.contains(d.getDefinable()));
        }

        return result;
    }
}
