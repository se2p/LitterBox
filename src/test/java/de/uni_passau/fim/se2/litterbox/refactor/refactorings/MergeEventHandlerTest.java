package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.MergeEventHandlerFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class MergeEventHandlerTest implements JsonTest {

    @Test
    public void testMergeEventHandlerFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/mergeEventHandler.json");
        MergeEventHandlerFinder finder = new MergeEventHandlerFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
    }

    @Test
    public void testMergeEventHandler() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/mergeEventHandler.json");
        MergeEventHandlerFinder finder = new MergeEventHandlerFinder();
        List<Refactoring> refactorings = finder.check(program);
        Refactoring r = refactorings.get(0);
        Program refactored = r.apply(program);
        //System.out.println(r.toString());
        assertThat(program).isNotEqualTo(refactored);
    }


//    @Test
//    public void testMergeEventHandler2() throws ParsingException, IOException {
//        Program program = getAST("src/test/fixtures/refactoring/mergeEventHandler.json");
//        MergeEventHandlerFinder finder = new MergeEventHandlerFinder();
//        List<Refactoring> refactorings = finder.check(program);
//        Program refactored;
//        Refactoring r = refactorings.get(0);
//        refactored = r.apply(program);
//        //System.out.println(r.toString());
//        File f = new File("/Users/felix/Desktop/ScratchProjects/mergeEventHandler.sb3");
//        JSONFileCreator.writeSb3FromProgram(refactored, "/Users/felix/Desktop", f, "_refactored");
//        assertThat(program).isNotEqualTo(refactored);
//    }
}
