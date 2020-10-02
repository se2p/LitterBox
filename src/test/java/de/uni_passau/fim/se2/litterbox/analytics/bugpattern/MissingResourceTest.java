package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class MissingResourceTest  implements JsonTest {

    @Test
    public void testMissingResources() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/missingResources.json");
        MissingResource finder = new MissingResource();
        Set<Issue> reports = finder.check(program);
        Truth.assertThat(reports).hasSize(3);
    }

    @Test
    public void testEmpty() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        MissingResource finder = new MissingResource();
        Set<Issue> reports = finder.check(program);
        Truth.assertThat(reports).hasSize(0);
    }
}

