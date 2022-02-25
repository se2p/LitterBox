package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class UnnecessaryIfTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryIf(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testUnnecessaryIf() throws IOException, ParsingException {
        assertThatFinderReports(1, new UnnecessaryIf(), "./src/test/fixtures/smells/unnecessaryIf.json");
    }

    @Test
    public void testDoubleIf() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryIf(), "./src/test/fixtures/smells/doubleIf.json");
    }

    @Test
    public void testDifferentIf() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryIf(), "./src/test/fixtures/smells/separateIfs.json");
    }

    @Test
    public void testDuplicates() throws IOException, ParsingException {
        UnnecessaryIf unnecessaryIf = new UnnecessaryIf();
        Program program = getAST("./src/test/fixtures/smells/unnecessaryIfDuplicate.json");
        List<Issue> issues = new ArrayList<>(unnecessaryIf.check(program));
        assertThat(issues).hasSize(2);
        assertThat(issues.get(0).isDuplicateOf(issues.get(1))).isTrue();
        assertThat(issues.get(1).isDuplicateOf(issues.get(0))).isTrue();
        assertThat(issues.get(0).isDuplicateOf(issues.get(0))).isFalse();
    }
}
