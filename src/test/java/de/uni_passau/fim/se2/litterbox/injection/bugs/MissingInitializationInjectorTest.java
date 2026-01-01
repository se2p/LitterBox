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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingInitialization;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingInitializationInjectorTest implements JsonTest {

    @Test
    public void testMissingInitializationInjector() throws IOException, ParsingException {
        // Script with initialization
        String script = """
                when green flag clicked
                go to x: (0) y: (0)
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should generate exactly one mutated program");

        // Verify detector finds the bug
        MissingInitialization detector = new MissingInitialization();
        Set<Issue> issues = detector.check(injected.getFirst().mutatedProgram());
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Missing Initialization issue");
    }

    @Test
    public void testHideShowInitialization() throws IOException, ParsingException {
        // Hide/Show are excluded because the finder explicitly skips VISIBILITY attributes
        String script = """
                when green flag clicked
                hide
                move (10) steps
                show
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(0, injected.size(), "Should not inject for hide/show (finder skips visibility)");
    }

    @Test
    public void testPointInDirectionInitialization() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                point in direction (90)
                move (10) steps
                turn right (15) degrees
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for point in direction block");
    }

    @Test
    public void testSwitchCostumeInitialization() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                switch costume to (costume1 v)
                move (10) steps
                next costume
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for switch costume block");
    }

    @Test
    public void testSetSizeInitialization() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set size to (100) %
                move (10) steps
                change size by (10)
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for set size block");
    }

    @Test
    public void testSetVolumeInitialization() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set volume to (100) %
                play sound (meow v) until done
                change volume by (-10)
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for set volume block");
    }

    @Test
    public void testNoInitNoInjection() throws IOException, ParsingException {
        // Script without any initialization blocks
        String script = """
                when green flag clicked
                move (10) steps
                say [hello]
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject without initialization blocks");
    }

    @Test
    public void testMultipleInitializationBlocks() throws IOException, ParsingException {
        // Multiple initialization blocks - each gets its own mutation
        // Note: hide is excluded (finder skips visibility), so only 2 injections
        String script = """
                when green flag clicked
                go to x: (0) y: (0)
                point in direction (90)
                hide
                move (10) steps
                turn right (15) degrees
                show
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Should create 2 mutations (go to, point in direction - hide is excluded)
        Assertions.assertEquals(2, injected.size(), "Should create mutation for each initialization block (excluding hide/show)");
    }
}
