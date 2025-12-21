package de.uni_passau.fim.se2.litterbox.injection;

import de.uni_passau.fim.se2.litterbox.injection.bugs.*;

import java.util.*;

/**
 * Registry and management tool for BugInjectors.
 */
public class InjectorTool {

    /**
     * Generates a map of all available bug injectors.
     *
     * @return Map of injector name to BugInjector instance.
     */
    public static Map<String, BugInjector> generateBugInjectors() {
        Map<String, BugInjector> injectors = new LinkedHashMap<>();
        registerInjector(new ForeverInsideIfInjector(), injectors);
        registerInjector(new TerminatedLoopInjector(), injectors);
        registerInjector(new CustomBlockWithTerminationInjector(), injectors);
        registerInjector(new CustomBlockWithForeverInjector(), injectors);
        registerInjector(new ForeverInsideLoopInjector(), injectors);
        registerInjector(new MissingTerminationConditionInjector(), injectors);
        registerInjector(new EndlessRecursionInjector(), injectors);
        registerInjector(new BlockingIfElseInjector(), injectors);
        registerInjector(new MessageNeverSentInjector(), injectors);
        registerInjector(new MessageNeverReceivedInjector(), injectors);
        registerInjector(new ImmediateDeleteCloneAfterBroadcastInjector(), injectors);
        registerInjector(new MissingCloneInitializationInjector(), injectors);

        return injectors;
    }

    private static void registerInjector(BugInjector injector, Map<String, BugInjector> injectors) {
        injectors.put(injector.getName(), injector);
    }

    /**
     * Retrieves the list of injectors based on a comma-separated command string.
     * Supports "all" or specific injector names.
     *
     * @param commandString Comma-separated list of injector names or "all".
     * @return List of matching BugInjectors.
     * @throws IllegalArgumentException if an unknown injector name is provided.
     */
    public static List<BugInjector> getInjectors(String commandString) {
        if (commandString == null || commandString.isEmpty() || commandString.equalsIgnoreCase("all")) {
            return new ArrayList<>(generateBugInjectors().values());
        }

        String[] requestedNames = commandString.split(",");
        Map<String, BugInjector> allInjectors = generateBugInjectors();
        List<BugInjector> result = new ArrayList<>();

        for (String name : requestedNames) {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (allInjectors.containsKey(trimmed)) {
                result.add(allInjectors.get(trimmed));
            } else {
                throw new IllegalArgumentException("Unknown injector: " + trimmed);
            }
        }
        return result;
    }
}
