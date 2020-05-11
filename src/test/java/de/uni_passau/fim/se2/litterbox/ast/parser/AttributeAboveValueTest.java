package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.AttributeAboveValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.fail;

public class AttributeAboveValueTest {
    private static Program project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/valueAbove.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = ProgramParser.parseProgram("valueAbove", objectMapper.readTree(file));
        } catch (IOException | ParsingException e) {
            fail();
        }
    }

    @Test
    public void testEvent() {
        Assertions.assertTrue(project.getActorDefinitionList().getDefintions().get(1).getScripts().getScriptList().get(0).getEvent() instanceof AttributeAboveValue);
        Assertions.assertEquals("loudness",
                ((AttributeAboveValue) project.getActorDefinitionList().getDefintions().get(1).getScripts().getScriptList().get(0).getEvent()).getAttribute().getType());
        Assertions.assertTrue(project.getActorDefinitionList().getDefintions().get(1).getScripts().getScriptList().get(1).getEvent() instanceof AttributeAboveValue);
        Assertions.assertEquals("timer",
                ((AttributeAboveValue) project.getActorDefinitionList().getDefintions().get(1).getScripts().getScriptList().get(1).getEvent()).getAttribute().getType());
    }
}
