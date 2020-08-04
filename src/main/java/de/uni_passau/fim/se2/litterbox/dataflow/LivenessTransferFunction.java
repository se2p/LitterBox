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

public class LivenessTransferFunction implements TransferFunction<Use> {
    @Override
    public Set<Use> apply(CFGNode node, Set<Use> inFacts) {

        // LiveOut(n) = (Live(n) \ kill (n)) ∪ gen(n)
        //
        //gen(n) = { v | v is used at n }
        //kill(n) = { v | v is modified at n }
        //
        Set<Use> result = new LinkedHashSet<>(inFacts);

        // Remove all uses of variables that are modified here
        Set<Defineable> definitions = node.getDefinitions()
                .stream()
                .map(Definition::getDefinable)
                .collect(Collectors.toSet());
        result.removeIf(d -> definitions.contains(d.getDefinable()));

        // Add new uses of variables that are used here
        Set<Use> uses = node.getUses();
        result.addAll(uses);

        return result;
    }
}
