package de.uni_passau.fim.se2.litterbox.analytics.llm;

import de.uni_passau.fim.se2.litterbox.analytics.ProgramAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class LLMProgramCreator implements ProgramAnalyzer<Program> {
    @Override
    public Program analyze(Program program) {
        return null;
    }

//    public LLMProgramCreator (
//            String query
//    ) {
//        super(target, ignoreLooseBlocks);
//    }
//
//    @Override
//    public String callLLM(Program program) {
//        ScratchLLM<OpenAiApi, DefaultPrompts> scratchLLM = new ScratchLLM<>(new OpenAiApi(), new DefaultPrompts(language));
//        return scratchLLM.autoComplete(program, target);
//    }
}
