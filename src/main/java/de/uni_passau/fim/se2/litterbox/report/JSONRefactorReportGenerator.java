package de.uni_passau.fim.se2.litterbox.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

public class JSONRefactorReportGenerator extends JSONGenerator implements RefactorReportGenerator {
    private OutputStream outputStream = null;

    private boolean closeStream = false;

    public JSONRefactorReportGenerator(String fileName) throws IOException {
        outputStream = new FileOutputStream(fileName);
        closeStream = true;
    }

    public JSONRefactorReportGenerator(OutputStream stream) throws IOException {
        this.outputStream = stream;
    }
    @Override
    public void generateReport(Program program, RefactorSequence refactorSequence) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode refactoringNode = mapper.createArrayNode();
        ObjectNode metricNode = mapper.createObjectNode();
        ArrayNode fitnessNode = mapper.createArrayNode();

        addMetrics(metricNode, program);
        rootNode.set("metrics", metricNode);

        JsonNode seedNode = mapper.createObjectNode();
        ((ObjectNode) seedNode).put("seed", Long.toString(Randomness.getSeed()));
        rootNode.set("seed", seedNode);

        for (Refactoring refactoring : refactorSequence.getExecutedRefactorings()) {
            JsonNode childNode = mapper.createObjectNode();
            ((ObjectNode) childNode).put("name", refactoring.toString());
            refactoringNode.add(childNode);
        }
        rootNode.set("refactorings", refactoringNode);

        for (Map.Entry<FitnessFunction<RefactorSequence>, Double> entry : refactorSequence.getFitnessMap().entrySet()) {
            JsonNode childNode = mapper.createObjectNode();
            ((ObjectNode) childNode).put("fitnessFunction", entry.getKey().getClass().getName());
            ((ObjectNode) childNode).put("fitnessValue", Double.toString(entry.getValue()));
            fitnessNode.add(childNode);
        }

        String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        final PrintStream printStream = new PrintStream(outputStream);
        printStream.print(jsonString);
        if (closeStream) {
            outputStream.close();
        }
    }
}
