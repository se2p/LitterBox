package de.uni_passau.fim.se2.litterbox.llm;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
public class ScratchLLM {

    private static final Logger log = Logger.getLogger(ScratchLLM.class.getName());

    private String apiKey = System.getenv("OPENAI_API_KEY");

    private OpenAiChatModel model;

    public ScratchLLM() {
        if (apiKey == null) {
            log.warning("OPENAI_API_KEY not set");
            apiKey = "demo"; // TODO: Only for testing, this uses the langchain4j proxy
        }

        // TODO: Generalise to allow other models (including non-OpenAI ones)
        model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(GPT_4_O_MINI)
                .build();
    }

    // TODO: There is a toScratchBlocks method but this seems to be
    //       removed in a different MR?
    private String generateScratchBlocks(AbstractNode node) {
        ScratchBlocksVisitor vis = new ScratchBlocksVisitor();
        vis.setAddActorNames(true);
        node.accept(vis);
        return vis.getScratchBlocks();
    }

    public String askAbout(Program program, String question) {
        String scratchBlocks = generateScratchBlocks(program);

        String prompt = """
                You are given the following Scratch program:
                %s

                Please answer the following question:
                %s
                """.formatted(scratchBlocks, question);

        log.fine("Asking LLM the following prompt: "+prompt);
        String result = model.generate(prompt);
        log.fine("LLM response:\n" + result);

        return result;
    }

    public String askAbout(Program program, String spriteName, String question) {
        List<ActorDefinition> actors = program.getActorDefinitionList().getDefinitions();
        Optional<ActorDefinition> actor = actors.stream().filter(a -> spriteName.equals(a.getIdent().getName())).findFirst();
        if (!actor.isPresent()) {
            log.warning("No actor found with name " + spriteName);
            return ""; // TODO proper error handling
        }

        String scratchBlocks = generateScratchBlocks(actor.get().getScripts());

        String prompt = """
                You are given the following Scratch sprite:
                %s

                Answer the following question:
                %s
                """.formatted(scratchBlocks, question);

        log.fine("Asking LLM the following prompt: "+prompt);

        String result = model.generate(prompt);
        log.fine("LLM response:\n" + result);

        return result;
    }

    public String improve(Program program, String detectors, boolean ignoreLooseBlocks) {
        String scratchBlocks = generateScratchBlocks(program);

        ProgramBugAnalyzer bugAnalyzer = new ProgramBugAnalyzer(detectors, ignoreLooseBlocks);
        Set<Issue> issues = bugAnalyzer.analyze(program);
        String issueDescription = issues.stream().map(Issue::getHint).collect(Collectors.joining("\n\n"));

        String prompt = """
                You are given the following Scratch program:
                %s

                The program contains the following bugs and code smells:
                %s

                Create a version of the program where this bug is fixed. Only output the scratchblocks code and nothing else.
                """.formatted(scratchBlocks, issueDescription);

        log.fine("Asking LLM the following prompt: "+prompt);

        String result = model.generate(prompt);
        log.fine("LLM response:\n" + result);

        // TODO: Parse stuff back to program and return actual Program rather than text
        // TODO: Check how many of the issues are actually fixed

        return result;
    }
}
