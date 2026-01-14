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
package de.uni_passau.fim.se2.litterbox.dependency;

import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;

public class PostDominatorTree extends DominatorTree {

    public PostDominatorTree(ControlFlowGraph cfg) {
        super(prepareGraph(cfg));
    }

    private static ControlFlowGraph prepareGraph(ControlFlowGraph cfg) {
        ControlFlowGraph reversed = cfg.reverseAndFilterInterproceduralDependencies();
        // Unused custom blocks, forever loops and infinite custom block recursions
        // are disconnected in the reversed CFG such no least common ancestor
        // may exist. To avoid this, we make everything depend on entry
        reversed.ensureAllNodesReachableFromEntry();
        return reversed;
    }

}
