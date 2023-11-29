package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.BugAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BugAnalyzerFixTest implements JsonTest {

    @Test
    public void testLoopSensingFix() throws IOException, ParsingException {
        BugAnalyzer bugAnalyzer = new BugAnalyzer(Path.of("./src/test/fixtures/fix_heuristcs/missingLoopSensingStopWait.json"), null, "bugs", false, false, false);
        bugAnalyzer.setPriorResultPath(Path.of("./src/test/fixtures/jsonReport/missingLoopSensingStopResult.json"));
        List<Issue> issues = new ArrayList<>(bugAnalyzer.check(getAST("./src/test/fixtures/fix_heuristics/missingLoopSensingStopWait.json")));
        Assertions.assertEquals(2, issues.size());
        Assertions.assertTrue(issues.get(1).getFinder() instanceof MissingLoopSensingWaitFix);
        Assertions.assertEquals("NG5_c]Sc%NFgdLaV]%Cq", AstNodeUtil.getBlockId(issues.get(1).getCodeLocation()));
    }

    @Test
    public void testLoopSensingMultiFixOne() throws IOException, ParsingException {
        BugAnalyzer bugAnalyzer = new BugAnalyzer(Path.of("./src/test/fixtures/fix_heuristcs/missingLoopSensingMultiFixOne.json"), null, "bugs", false, false, false);
        bugAnalyzer.setPriorResultPath(Path.of("./src/test/fixtures/jsonReport/missingLoopSensingMultiResult.json"));
        List<Issue> issues = new ArrayList<>(bugAnalyzer.check(getAST("./src/test/fixtures/fix_heuristics/missingLoopSensingMultiFixOne.json")));
        Assertions.assertEquals(3, issues.size());
        Assertions.assertTrue(issues.get(2).getFinder() instanceof MissingLoopSensingLoopFix);
        Assertions.assertEquals("NG5_c]Sc%NFgdLaV]%Cq", AstNodeUtil.getBlockId(issues.get(2).getCodeLocation()));
    }
}
