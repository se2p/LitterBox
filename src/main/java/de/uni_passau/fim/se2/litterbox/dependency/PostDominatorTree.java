package de.uni_passau.fim.se2.litterbox.dependency;

import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;

public class PostDominatorTree extends DominatorTree {

    public PostDominatorTree(ControlFlowGraph cfg) {
        super(cfg.reverse());
    }

}
