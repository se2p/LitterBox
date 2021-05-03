package de.uni_passau.fim.se2.litterbox.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;

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
    public void generateReport(Program program, Collection<Refactoring> refactorings) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode refactoringNode = mapper.createArrayNode();
        ObjectNode metricNode = mapper.createObjectNode();

        addMetrics(metricNode, program);
        rootNode.set("metrics", metricNode);

        JsonNode seedNode = mapper.createObjectNode();
        ((ObjectNode) seedNode).put("seed", Long.toString(Randomness.getSeed()));
        rootNode.set("seed", seedNode);

        for (Refactoring refactoring : refactorings) {
            JsonNode childNode = mapper.createObjectNode();
            ((ObjectNode) childNode).put("name", refactoring.toString());
            refactoringNode.add(childNode);
        }
        rootNode.set("refactorings", refactoringNode);

        String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        final PrintStream printStream = new PrintStream(outputStream);
        printStream.print(jsonString);
        if (closeStream) {
            outputStream.close();
        }
    }
}
