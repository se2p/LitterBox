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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class FixHeuristicRegistryTest {

    private static IssueDTO createTestIssueDTO(String blockId) {
        return new IssueDTO(
                0, "TestFinder", "TestFinder", IssueType.BUG, 0, "Sprite1",
                blockId, null, null, null, null, null, null, 0, null, null
        );
    }

    @Test
    public void testHeuristicCount() {
        int count = FixHeuristicRegistry.getHeuristicCount();
        Assertions.assertTrue(count >= 46, "Expected at least 46 fix heuristics, but found: " + count);
    }

    @Test
    public void testAllHeuristicsCanBeInstantiated() {
        Set<String> supportedFinders = FixHeuristicRegistry.getSupportedBugFinders();
        for (String finderName : supportedFinders) {
            List<FixHeuristic> heuristics = FixHeuristicRegistry.getFixHeuristics(finderName, createTestIssueDTO("test-id"));
            Assertions.assertFalse(heuristics.isEmpty(), "No heuristics found for supported finder: " + finderName);
            for (FixHeuristic heuristic : heuristics) {
                Assertions.assertNotNull(heuristic, "Heuristic instance is null for: " + finderName);
                Assertions.assertEquals(finderName, heuristic.getName(), "Heuristic name mismatch for: " + finderName);
            }
        }
    }

    @Test
    public void testSpecificHeuristicsArePresent() {
        Assertions.assertTrue(FixHeuristicRegistry.hasFixHeuristic("missing_initialization"));
        Assertions.assertTrue(FixHeuristicRegistry.hasFixHeuristic("forever_inside_loop"));
        Assertions.assertTrue(FixHeuristicRegistry.hasFixHeuristic("type_error"));
        Assertions.assertTrue(FixHeuristicRegistry.hasFixHeuristic("immediate_delete_clone_after_broadcast"));
    }
}
