/*
 * Copyright (C) 2019-2025 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Registry that maps bug finders to their corresponding fix heuristics.
 *
 * <p>Each fix heuristic checks whether a previously reported bug has been fixed
 * in a modified version of the program. This registry provides a way to look up
 * which fix heuristic(s) should be used for a given bug finder.
 *
 * <p>The registry is built by instantiating each fix heuristic with a dummy
 * block ID and querying its name (which delegates to the related bug finder).
 */
public final class FixHeuristicRegistry {

    private static final Logger log = Logger.getLogger(FixHeuristicRegistry.class.getName());
    private static final String DUMMY_BLOCK_ID = "__registry_init__";

    private static final Map<String, List<Function<String, IssueFinder>>> REGISTRY;

    static {
        REGISTRY = buildRegistry();
    }

    private FixHeuristicRegistry() {
        // Utility class
    }

    /**
     * Builds the registry by instantiating each heuristic and querying its name.
     */
    private static Map<String, List<Function<String, IssueFinder>>> buildRegistry() {
        Map<String, List<Function<String, IssueFinder>>> registry = new LinkedHashMap<>();

        // Register all fix heuristics
        registerHeuristic(registry, AmbiguousCustomBlockSignatureFix::new);
        registerHeuristic(registry, AmbiguousParameterNameUsedFix::new);
        registerHeuristic(registry, BlockingIfElseFix::new);
        registerHeuristic(registry, CallWithoutDefinitionFix::new);
        registerHeuristic(registry, ComparingLiteralsFix::new);
        registerHeuristic(registry, CustomBlockWithForeverFix::new);
        registerHeuristic(registry, CustomBlockWithTerminationFix::new);
        registerHeuristic(registry, EndlessRecursionFix::new);
        registerHeuristic(registry, ExpressionAsTouchingOrColorFix::new);
        registerHeuristic(registry, FloatingPointComparisonFix::new);
        registerHeuristic(registry, ForeverInsideIfFix::new);
        registerHeuristic(registry, ForeverInsideLoopFix::new);
        registerHeuristic(registry, HideWithoutShowFix::new);
        registerHeuristic(registry, IllegalParameterRefactorFix::new);
        registerHeuristic(registry, ImmediateDeleteCloneAfterBroadcastFix::new);
        registerHeuristic(registry, ImmediateStopAfterSayFix::new);
        registerHeuristic(registry, InappropriateHandlerDeleteCloneFix::new);
        registerHeuristic(registry, InterruptedLoopSensingFix::new);
        registerHeuristic(registry, InvalidNumberStringFix::new);
        registerHeuristic(registry, KeySetPositionFix::new);
        registerHeuristic(registry, MessageAsLiteralFix::new);
        registerHeuristic(registry, MessageNeverReceivedFix::new);
        registerHeuristic(registry, MessageNeverSentFix::new);
        registerHeuristic(registry, MissingAskFix::new);
        registerHeuristic(registry, MissingBackdropSwitchFix::new);
        registerHeuristic(registry, MissingCloneCallFix::new);
        registerHeuristic(registry, MissingCloneInitializationFix::new);
        registerHeuristic(registry, MissingEraseAllFix::new);
        registerHeuristic(registry, MissingInitializationFix::new);
        registerHeuristic(registry, MissingLoopMousePositionFix::new);
        registerHeuristic(registry, MissingLoopSensingFix::new);
        registerHeuristic(registry, MissingLoopSensingLoopFix::new);
        registerHeuristic(registry, MissingLoopSensingWaitFix::new);
        registerHeuristic(registry, MissingPenDownFix::new);
        registerHeuristic(registry, MissingPenUpFix::new);
        registerHeuristic(registry, MissingResourceFix::new);
        registerHeuristic(registry, MissingTerminationConditionFix::new);
        registerHeuristic(registry, MissingWaitUntilConditionFix::new);
        registerHeuristic(registry, NoWorkingScriptsFix::new);
        registerHeuristic(registry, OrphanedParameterFix::new);
        registerHeuristic(registry, OverwrittenVisualEffectFix::new);
        registerHeuristic(registry, ParameterAsLiteralFix::new);
        registerHeuristic(registry, ParameterOutOfScopeFix::new);
        registerHeuristic(registry, RecursiveCloningFix::new);
        registerHeuristic(registry, RedundantAssignmentFix::new);
        registerHeuristic(registry, StutteringMovementFix::new);
        registerHeuristic(registry, TerminatedLoopFix::new);
        registerHeuristic(registry, TypeErrorFix::new);
        registerHeuristic(registry, VariableAsLiteralFix::new);

        // Make lists immutable
        registry.replaceAll((k, v) -> List.copyOf(v));

        log.info(() -> "Registered " + registry.size() + " bug finders with fix heuristics");
        return Collections.unmodifiableMap(registry);
    }

    private static void registerHeuristic(
            Map<String, List<Function<String, IssueFinder>>> registry,
            Function<String, ? extends AbstractFixHeuristic> factory
    ) {
        try {
            // Instantiate with dummy ID to get the finder name
            AbstractFixHeuristic heuristic = factory.apply(DUMMY_BLOCK_ID);
            String finderName = heuristic.getName();

            // Cast to IssueFinder function type
            Function<String, IssueFinder> issueFinderFactory = blockId -> factory.apply(blockId);
            registry.computeIfAbsent(finderName, k -> new ArrayList<>()).add(issueFinderFactory);

            log.fine(() -> "Registered fix heuristic for: " + finderName);
        } catch (Exception e) {
            log.warning("Failed to register fix heuristic: " + e.getMessage());
        }
    }

    /**
     * Returns the fix heuristics for a given bug finder.
     *
     * @param bugFinderName the name of the bug finder
     * @param bugLocationBlockId the block ID where the bug was originally reported
     * @return a list of fix heuristics to check, or empty if none are registered
     */
    public static List<IssueFinder> getFixHeuristics(String bugFinderName, String bugLocationBlockId) {
        List<Function<String, IssueFinder>> factories = REGISTRY.get(bugFinderName);
        if (factories == null) {
            return List.of();
        }
        return factories.stream()
                .map(factory -> factory.apply(bugLocationBlockId))
                .toList();
    }

    /**
     * Returns the names of all bug finders that have registered fix heuristics.
     *
     * @return an unmodifiable set of bug finder names
     */
    public static Set<String> getSupportedBugFinders() {
        return REGISTRY.keySet();
    }

    /**
     * Checks if a fix heuristic is registered for the given bug finder.
     *
     * @param bugFinderName the name of the bug finder
     * @return true if a fix heuristic is registered
     */
    public static boolean hasFixHeuristic(String bugFinderName) {
        return REGISTRY.containsKey(bugFinderName);
    }

    /**
     * Returns the total number of registered fix heuristics.
     *
     * @return the count of individual fix heuristics
     */
    public static int getHeuristicCount() {
        return REGISTRY.values().stream().mapToInt(List::size).sum();
    }
}
