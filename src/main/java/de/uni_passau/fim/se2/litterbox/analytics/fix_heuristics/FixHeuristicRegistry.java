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

import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

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
 * IssueDTO and querying its name (which delegates to the related bug finder).
 */
public final class FixHeuristicRegistry {

    private static final Logger log = Logger.getLogger(FixHeuristicRegistry.class.getName());

    /**
     * Sentinel IssueDTO used only during registry initialization to discover bug finder names.
     *
     * <p>Each fix heuristic is associated with a bug finder, but the association is defined
     * via {@link AbstractFixHeuristic#getRelatedBugFinder()} which requires instantiation.
     * Rather than requiring each heuristic to duplicate the bug finder name as a static constant,
     * we instantiate once during class loading with this sentinel issue to query the name.
     *
     * <p>This is a trade-off: the alternative (static constants in each heuristic) would
     * require maintaining duplicate name strings in 48 files.
     */
    private static final IssueDTO DUMMY_ISSUE = new IssueDTO(
            0, "__registry_init__", "__dummy__", null, 0, null,
            "__registry_init__", null, null, null, null, null, null, 0, null, null
    );

    private static final Map<String, List<Function<IssueDTO, FixHeuristic>>> REGISTRY;

    /**
     * List of heuristic class names that failed to register during initialization.
     */
    private static final List<String> FAILED_REGISTRATIONS = new ArrayList<>();

    static {
        REGISTRY = buildRegistry();
        if (!FAILED_REGISTRATIONS.isEmpty()) {
            log.severe(() -> "Failed to register " + FAILED_REGISTRATIONS.size()
                    + " fix heuristics: " + FAILED_REGISTRATIONS);
        }
    }

    private FixHeuristicRegistry() {
        // Utility class
    }

    /**
     * Builds the registry by instantiating each heuristic and querying its name.
     */
    private static Map<String, List<Function<IssueDTO, FixHeuristic>>> buildRegistry() {
        Map<String, List<Function<IssueDTO, FixHeuristic>>> registry = new LinkedHashMap<>();

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
        registerHeuristic(registry, InvalidActorBlocksFix::new);
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
        registerHeuristic(registry, TerminatedLoopFix::new);
        registerHeuristic(registry, TypeErrorFix::new);
        registerHeuristic(registry, VariableAsLiteralFix::new);

        // Make lists immutable
        registry.replaceAll((k, v) -> List.copyOf(v));

        log.info(() -> "Registered " + registry.size() + " bug finders with fix heuristics");
        return Collections.unmodifiableMap(registry);
    }

    private static void registerHeuristic(
            Map<String, List<Function<IssueDTO, FixHeuristic>>> registry,
            Function<IssueDTO, ? extends AbstractFixHeuristic> factory
    ) {
        String factoryClassName = "unknown";
        try {
            // Instantiate with dummy issue to get the finder name
            AbstractFixHeuristic heuristic = factory.apply(DUMMY_ISSUE);
            factoryClassName = heuristic.getClass().getSimpleName();
            String finderName = heuristic.getName();

            // Cast to FixHeuristic function type
            Function<IssueDTO, FixHeuristic> fixHeuristicFactory = factory::apply;
            registry.computeIfAbsent(finderName, k -> new ArrayList<>()).add(fixHeuristicFactory);

            log.fine(() -> "Registered fix heuristic for: " + finderName);
        } catch (RuntimeException e) {
            String failedClass = factoryClassName;
            FAILED_REGISTRATIONS.add(failedClass);
            log.warning(() -> "Failed to register fix heuristic " + failedClass + ": " + e.getMessage());
        }
    }

    /**
     * Returns the fix heuristics for a given bug finder.
     *
     * @param bugFinderName the name of the bug finder
     * @param originalIssue the original issue DTO
     * @return a list of fix heuristics to check, or empty if none are registered
     */
    public static List<FixHeuristic> getFixHeuristics(String bugFinderName, IssueDTO originalIssue) {
        List<Function<IssueDTO, FixHeuristic>> factories = REGISTRY.get(bugFinderName);
        if (factories == null) {
            return List.of();
        }
        return factories.stream()
                .map(factory -> factory.apply(originalIssue))
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

    /**
     * Returns the list of fix heuristic class names that failed to register during initialization.
     *
     * <p>This method is primarily intended for testing to verify all heuristics registered successfully.
     *
     * @return an unmodifiable list of failed class names (empty if all registrations succeeded)
     */
    public static List<String> getFailedRegistrations() {
        return List.copyOf(FAILED_REGISTRATIONS);
    }

    /**
     * Checks if all fix heuristics registered successfully during initialization.
     *
     * @return true if no registrations failed
     */
    public static boolean allRegistrationsSucceeded() {
        return FAILED_REGISTRATIONS.isEmpty();
    }
}
