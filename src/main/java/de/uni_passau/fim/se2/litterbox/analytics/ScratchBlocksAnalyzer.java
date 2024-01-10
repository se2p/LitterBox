package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class ScratchBlocksAnalyzer extends Analyzer<String> {
    private static final Logger log = Logger.getLogger(ExtractionAnalyzer.class.getName());

    public ScratchBlocksAnalyzer(Path input, Path output, boolean delete) {
        super(input, output, delete);
    }

    public ScratchBlocksAnalyzer() {
        super(null, null, false);
    }

    @Override
    public String check(Program program) {
        ScratchBlocksVisitor vis = new ScratchBlocksVisitor();
        program.accept(vis);
        return vis.getScratchBlocks();
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, String checkResult) throws IOException {

    }
}
