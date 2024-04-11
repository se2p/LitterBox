package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

public class ProgramScratchBlocksAnalyzer implements ProgramAnalyzer<String> {

    @Override
    public String analyze(Program program) {
        ScratchBlocksVisitor vis = new ScratchBlocksVisitor();
        vis.setAddActorNames(true);
        program.accept(vis);
        return vis.getScratchBlocks();
    }
}
