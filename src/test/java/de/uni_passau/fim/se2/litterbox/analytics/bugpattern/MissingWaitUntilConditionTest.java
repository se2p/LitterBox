package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import static junit.framework.TestCase.fail;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MissingWaitUntilConditionTest {

    private static Program program;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/bugpattern/missingWaitUntilCondition.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            program = ProgramParser.parseProgram("missingWaitUntilCondition", objectMapper.readTree(file));
        } catch (IOException | ParsingException e) {
            fail();
        }
    }

    @Test
    public void testMissingPenUp() {
        MissingWaitUntilCondition finder = new MissingWaitUntilCondition();
        final IssueReport result = finder.check(program);
        Truth.assertThat(result.getCount()).isEqualTo(2);
        Truth.assertThat(result.getPosition().get(1)).isEqualTo("Sprite1");
        Truth.assertThat(result.getPosition().get(0)).isEqualTo("Stage");
        System.out.println(result);
    }
}