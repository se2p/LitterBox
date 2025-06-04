/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.ProgramBugAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BugAnalyzerFixTest implements JsonTest {

    private final Scratch3Parser parser = new Scratch3Parser();

    @Test
    public void testLoopSensingFix() throws ParsingException {
        ProgramBugAnalyzer programBugAnalyzer = new ProgramBugAnalyzer("bugs", false, Path.of("./src/test/fixtures/jsonReport/missingLoopSensingStopResult.json"));
        List<Issue> issues = new ArrayList<>(programBugAnalyzer.analyze(parser.parseFile(Path.of("./src/test/fixtures/fix_heuristics/missingLoopSensingStopWait.json").toFile())));
        Assertions.assertEquals(2, issues.size());
        Assertions.assertInstanceOf(MissingLoopSensingWaitFix.class, issues.get(1).getFinder());
        Assertions.assertEquals("NG5_c]Sc%NFgdLaV]%Cq", AstNodeUtil.getBlockId(issues.get(1).getCodeLocation()));
    }


    @Test
    public void testLoopSensingMultiFixOne() throws ParsingException {
        ProgramBugAnalyzer programBugAnalyzer = new ProgramBugAnalyzer("bugs", false, Path.of("./src/test/fixtures/jsonReport/missingLoopSensingMultiResult.json"));
        List<Issue> issues = new ArrayList<>(programBugAnalyzer.analyze(parser.parseFile(Path.of("./src/test/fixtures/fix_heuristics/missingLoopSensingMultiFixOne.json").toFile())));
        Assertions.assertEquals(3, issues.size());
        Assertions.assertInstanceOf(MissingLoopSensingLoopFix.class, issues.get(2).getFinder());
        Assertions.assertEquals("NG5_c]Sc%NFgdLaV]%Cq", AstNodeUtil.getBlockId(issues.get(2).getCodeLocation()));
    }

    @Test
    public void testComparingLiteralsMultiFix() throws ParsingException {
        ProgramBugAnalyzer programBugAnalyzer = new ProgramBugAnalyzer("bugs", false, Path.of("./src/test/fixtures/jsonReport/comparingLiteralsResult.json"));
        List<Issue> issues = new ArrayList<>(programBugAnalyzer.analyze(parser.parseFile(Path.of("./src/test/fixtures/fix_heuristics/comparingLiteralsFix.json").toFile())));
        Assertions.assertEquals(6, issues.size());
        Assertions.assertInstanceOf(ComparingLiteralsFix.class, issues.get(3).getFinder());
        Assertions.assertInstanceOf(ComparingLiteralsFix.class, issues.get(4).getFinder());
        Assertions.assertInstanceOf(ComparingLiteralsFix.class, issues.get(5).getFinder());
    }

}
