package de.uni_passau.fim.se2.litterbox.ast.model.extension.mblock;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class GeneralMBlockTest implements JsonTest {

    @Test
    public void testVariantsParsing() throws IOException {
        Program program = null;
        try {
            program = getAST("./src/test/fixtures/mblock/mBlock_all_variants.json");
        } catch (ParsingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        Assertions.assertTrue(program != null);
    }
}
