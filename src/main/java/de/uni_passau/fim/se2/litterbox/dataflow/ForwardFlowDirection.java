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
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;

import java.util.Set;

public class ForwardFlowDirection implements FlowDirection {

    @Override
    public Set<CFGNode> getInNodes(ControlFlowGraph cfg, CFGNode cfgNode) {
        return cfg.getPredecessors(cfgNode);
    }

    @Override
    public Set<CFGNode> getOutNodes(ControlFlowGraph cfg, CFGNode cfgNode) {
        return cfg.getSuccessors(cfgNode);
    }

    @Override
    public Iterable<CFGNode> getInitialNodes(ControlFlowGraph cfg) {
        return cfg.getNodesInReversePostOrder();
    }
}
