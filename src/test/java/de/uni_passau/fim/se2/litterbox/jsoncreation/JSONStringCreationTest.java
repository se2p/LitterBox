package de.uni_passau.fim.se2.litterbox.jsoncreation;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class JSONStringCreationTest implements JsonTest {

    @Test
    void createProgramJSONStringWithVariableInComparison() throws ParsingException, IOException {
        final var program = getAST("src/test/fixtures/MWE.json");
        final String json = JSONStringCreator.createProgramJSONString(program);
        Assertions.assertTrue(json.contains("\"inputs\": {\"OPERAND1\": [3,[12,\"my variable\",\"`jEk@4|i[#Fk?(8x)AV.-my variable\"],[10,\"\"]],\"OPERAND2\": [1,[4,\"50\"]]}"));
    }

    @Test
    void createProgramJSONStringWithLooseVariable() throws ParsingException, IOException {
        final var program = getAST("src/test/fixtures/looseVariable.json");
        final String json = JSONStringCreator.createProgramJSONString(program);
        Assertions.assertTrue(json.contains("[12,\"meine Variable\",\"`jEk@4|i[#Fk?(8x)AV.-my variable\",676.0,120.0]"));
    }
}
