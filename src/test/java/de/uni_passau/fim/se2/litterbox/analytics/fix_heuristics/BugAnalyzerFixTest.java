/*
 * Copyright (C) 2019-2022 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
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

    @Test
    public void testComparingLiteralsMultiFix() throws IOException, ParsingException {
        BugAnalyzer bugAnalyzer = new BugAnalyzer(Path.of("./src/test/fixtures/fix_heuristics/comparingLiteralsFix.json"), null, "bugs", false, false, false);
        bugAnalyzer.setPriorResultPath(Path.of("./src/test/fixtures/jsonReport/comparingLiteralsResult.json"));
        List<Issue> issues = new ArrayList<>(bugAnalyzer.check(getAST("./src/test/fixtures/fix_heuristics/comparingLiteralsFix.json")));
        Assertions.assertEquals(6, issues.size());
        Assertions.assertTrue(issues.get(3).getFinder() instanceof ComparingLiteralsFix);
        Assertions.assertTrue(issues.get(4).getFinder() instanceof ComparingLiteralsFix);
        Assertions.assertTrue(issues.get(5).getFinder() instanceof ComparingLiteralsFix);
    }
}
