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
package de.uni_passau.fim.se2.litterbox.injection;

import de.uni_passau.fim.se2.litterbox.injection.bugs.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry and management tool for BugInjectors.
 */
public class InjectorTool {

    private InjectorTool() {
        // utility class
    }

    /**
     * Generates a map of all available bug injectors.
     *
     * @return Map of injector name to BugInjector instance.
     */
    public static Map<String, BugInjector> generateBugInjectors() {
        Map<String, BugInjector> injectors = new LinkedHashMap<>();

        // Control flow bugs
        registerInjector(new ForeverInsideIfInjector(), injectors);
        registerInjector(new ForeverInsideLoopInjector(), injectors);
        registerInjector(new TerminatedLoopInjector(), injectors);
        registerInjector(new BlockingIfElseInjector(), injectors);
        registerInjector(new MissingTerminationConditionInjector(), injectors);
        registerInjector(new MissingWaitUntilConditionInjector(), injectors);
        registerInjector(new InterruptedLoopSensingInjector(), injectors);

        // Custom block bugs
        registerInjector(new CustomBlockWithTerminationInjector(), injectors);
        registerInjector(new CustomBlockWithForeverInjector(), injectors);
        registerInjector(new EndlessRecursionInjector(), injectors);
        registerInjector(new CallWithoutDefinitionInjector(), injectors);
        registerInjector(new RecursiveCloningInjector(), injectors);

        // Message/broadcast bugs
        registerInjector(new MessageNeverSentInjector(), injectors);
        registerInjector(new MessageNeverReceivedInjector(), injectors);
        registerInjector(new MessageAsLiteralInjector(), injectors);

        // Clone bugs
        registerInjector(new ImmediateDeleteCloneAfterBroadcastInjector(), injectors);
        registerInjector(new MissingCloneInitializationInjector(), injectors);
        registerInjector(new MissingCloneCallInjector(), injectors);
        registerInjector(new InappropriateHandlerDeleteCloneInjector(), injectors);

        // Parameter-related bugs
        registerInjector(new OrphanedParameterInjector(), injectors);
        registerInjector(new AmbiguousCustomBlockSignatureInjector(), injectors);
        registerInjector(new AmbiguousParameterNameUsedInjector(), injectors);
        registerInjector(new ParameterAsLiteralInjector(), injectors);
        registerInjector(new ParameterOutOfScopeInjector(), injectors);
        registerInjector(new IllegalParameterRefactorInjector(), injectors);

        // Variable/literal bugs
        registerInjector(new VariableAsLiteralInjector(), injectors);
        registerInjector(new ComparingLiteralsInjector(), injectors);
        registerInjector(new InvalidNumberStringInjector(), injectors);
        registerInjector(new TypeErrorInjector(), injectors);

        // Missing initialization/resource bugs
        registerInjector(new MissingInitializationInjector(), injectors);
        registerInjector(new MissingResourceInjector(), injectors);
        registerInjector(new MissingAskInjector(), injectors);
        registerInjector(new MissingBackdropSwitchInjector(), injectors);

        // Sensing bugs
        registerInjector(new MissingLoopSensingInjector(), injectors);
        registerInjector(new MissingLoopMousePositionInjector(), injectors);
        registerInjector(new KeySetPositionInjector(), injectors);

        // Pen bugs
        registerInjector(new MissingPenDownInjector(), injectors);
        registerInjector(new MissingPenUpInjector(), injectors);
        registerInjector(new MissingEraseAllInjector(), injectors);

        // Looks/effects bugs
        registerInjector(new HideWithoutShowInjector(), injectors);
        registerInjector(new OverwrittenVisualEffectInjector(), injectors);
        registerInjector(new ImmediateStopAfterSayThinkInjector(), injectors);

        // Expression bugs
        registerInjector(new ExpressionAsTouchingOrColorInjector(), injectors);
        registerInjector(new FloatingPointComparisonInjector(), injectors);

        // Redundancy/structure bugs
        registerInjector(new RedundantAssignmentInjector(), injectors);
        registerInjector(new NoWorkingScriptsInjector(), injectors);
        registerInjector(new InvalidActorBlocksInjector(), injectors);

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
