package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.llm.ScratchLLM;

import java.util.logging.Logger;

public class ProgramLLMAnalyzer implements ProgramAnalyzer<String> {

    private static final Logger log = Logger.getLogger(ProgramLLMAnalyzer.class.getName());

    private String query;

    private String targetSprite;

    private String detectors;

    private boolean ignoreLooseBlocks;

    // TODO: Probably should use two different analyzers rather than a flag
    private boolean fix;

    public ProgramLLMAnalyzer(String query, String targetSprite, String detectors, boolean ignoreLooseBlocks, boolean fix) {
        this.query = query;
        this.targetSprite = targetSprite;
        this.detectors = detectors;
        this.ignoreLooseBlocks = ignoreLooseBlocks;
        this.fix = fix;
    }

    @Override
    public String analyze(Program program) {
        ScratchLLM scratchLLM = new ScratchLLM();
        log.fine("Target sprite: " + targetSprite);
        String response;
        // TODO: Handle this properly once we know what APIs we actually want to offer
        if (fix) {
            response = scratchLLM.improve(program, detectors, ignoreLooseBlocks);
        } else {
            if (targetSprite != null) {
                response = scratchLLM.askAbout(program, targetSprite, query);
            } else {
                response = scratchLLM.askAbout(program, query);
            }
        }
        return response;
    }
}
