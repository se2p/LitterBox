package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TextToSpeechBlockCountTest implements JsonTest {

    @Test
    public void testAll() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/stmtParser/allTextToSpeech.json");
        TextToSpeechBlockCount parameterName = new TextToSpeechBlockCount();
        Assertions.assertEquals(3, parameterName.calculateMetric(empty));
    }

    @Test
    public void testPenAndTTS() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/metrics/penAndTTS.json");
        TextToSpeechBlockCount parameterName = new TextToSpeechBlockCount();
        Assertions.assertEquals(2, parameterName.calculateMetric(empty));
    }
}
