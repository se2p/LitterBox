package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.MergeLoopsFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class MergeLoopsTest implements JsonTest {

    @Test
    public void testMergeLoopsFinder_Bidirectional() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/mergeLoops.json");
        MergeLoopsFinder finder = new MergeLoopsFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(2);
        assertThat(refactorings.get(0)).isInstanceOf(MergeLoops.class);
        assertThat(refactorings.get(1)).isInstanceOf(MergeLoops.class);
    }

    @Test
    public void testMergeLoopsFinder_Oneway() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/unmergeableLoops.json");
        MergeLoopsFinder finder = new MergeLoopsFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).isEmpty();
    }

}
