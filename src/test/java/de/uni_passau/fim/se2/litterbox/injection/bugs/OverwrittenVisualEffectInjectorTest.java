/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.OverwrittenVisualEffect;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class OverwrittenVisualEffectInjectorTest implements JsonTest {

    @Test
    public void testOverwrittenVisualEffectInjector() throws IOException, ParsingException {
        // Script with set graphic effect
        String script = """
                when green flag clicked
                set [ghost v] effect to (50)
                """;

        Program program = parseScratchBlocks(script);

        OverwrittenVisualEffectInjector injector = new OverwrittenVisualEffectInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify duplicate effect added
        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertTrue(result.contains("set [ghost v] effect to (50)"), "Original should remain");
        Assertions.assertTrue(result.contains("set [ghost v] effect to (0)"), "Reset to 0 should be added");

        // Verification: Check detector
        OverwrittenVisualEffect detector = new OverwrittenVisualEffect();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Overwritten Visual Effect issue");
    }

    @Test
    public void testCostumeSwitch() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                switch costume to (costume1 v)
                """;

        Program program = parseScratchBlocks(script);

        OverwrittenVisualEffectInjector injector = new OverwrittenVisualEffectInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for costume switch");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify duplicate costume switch added
        String result = ScratchBlocksVisitor.of(mutated);
        // Count occurrences
        int count = result.split("switch costume to").length - 1;
        Assertions.assertEquals(2, count, "Should have 2 switch costume statements. Got:\n" + result);

        OverwrittenVisualEffect detector = new OverwrittenVisualEffect();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find issue");
    }

    @Test
    public void testShowHideVisibility() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                show
                """;

        Program program = parseScratchBlocks(script);

        OverwrittenVisualEffectInjector injector = new OverwrittenVisualEffectInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for show");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify duplicate show added
        String result = ScratchBlocksVisitor.of(mutated);
        int count = result.split("show").length - 1;
        Assertions.assertEquals(2, count, "Should have 2 show statements. Got:\n" + result);

        OverwrittenVisualEffect detector = new OverwrittenVisualEffect();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find issue");
    }

    @Test
    public void testSayBubble() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                say [hello]
                """;

        Program program = parseScratchBlocks(script);

        OverwrittenVisualEffectInjector injector = new OverwrittenVisualEffectInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for say");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify duplicate say added
        String result = ScratchBlocksVisitor.of(mutated);
        int count = result.split("say \\[hello\\]").length - 1;
        Assertions.assertEquals(2, count, "Should have 2 say statements. Got:\n" + result);

        OverwrittenVisualEffect detector = new OverwrittenVisualEffect();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find issue");
    }

    @Test
    public void testNoEffectNoInjection() throws IOException, ParsingException {
        // Script without visual effect statements - should not inject
        String script = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        OverwrittenVisualEffectInjector injector = new OverwrittenVisualEffectInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject bug without visual effect statements");
    }
}
