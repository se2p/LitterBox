/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.*;
import de.uni_passau.fim.se2.litterbox.analytics.smells.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static de.uni_passau.fim.se2.litterbox.utils.GroupConstants.*;

/**
 * Holds all IssueFinder and executes them.
 * Register new implemented checks here.
 */
public class IssueTool {

    private static final Logger log = Logger.getLogger(BugAnalyzer.class.getName());

    private static Map<String, IssueFinder> generateBugFinders() {
        Map<String, IssueFinder> bugFinders = new LinkedHashMap<>();
        registerBugFinder(new AmbiguousCustomBlockSignature(), bugFinders);
        registerBugFinder(new AmbiguousParameterNameUsed(), bugFinders);
        registerBugFinder(new CallWithoutDefinition(), bugFinders);
        registerBugFinder(new ComparingLiterals(), bugFinders);
        registerBugFinder(new CustomBlockWithForever(), bugFinders);
        registerBugFinder(new CustomBlockWithTermination(), bugFinders);
        registerBugFinder(new EndlessRecursion(), bugFinders);
        registerBugFinder(new ExpressionAsTouchingOrColor(), bugFinders);
        registerBugFinder(new ForeverInsideLoop(), bugFinders);
        registerBugFinder(new IllegalParameterRefactor(), bugFinders);
        registerBugFinder(new MessageNeverReceived(), bugFinders);
        registerBugFinder(new MessageNeverSent(), bugFinders);
        registerBugFinder(new MissingAsk(), bugFinders);
        registerBugFinder(new MissingBackdropSwitch(), bugFinders);
        registerBugFinder(new MissingCloneCall(), bugFinders);
        registerBugFinder(new MissingCloneInitialization(), bugFinders);
        registerBugFinder(new MissingInitialization(), bugFinders);
        registerBugFinder(new MissingEraseAll(), bugFinders);
        registerBugFinder(new MissingLoopSensing(), bugFinders);
        registerBugFinder(new MissingPenDown(), bugFinders);
        registerBugFinder(new MissingPenUp(), bugFinders);
        registerBugFinder(new MissingTerminationCondition(), bugFinders);
        registerBugFinder(new MissingWaitUntilCondition(), bugFinders);
        registerBugFinder(new NoWorkingScripts(), bugFinders);
        registerBugFinder(new OrphanedParameter(), bugFinders);
        registerBugFinder(new ParameterOutOfScope(), bugFinders);
        registerBugFinder(new PositionEqualsCheck(), bugFinders);
        registerBugFinder(new RecursiveCloning(), bugFinders);
        registerBugFinder(new StutteringMovement(), bugFinders);
        registerBugFinder(new VariableAsLiteral(), bugFinders);

        return bugFinders;
    }

    private static Map<String, IssueFinder> generateAllFinders() {
        Map<String, IssueFinder> allFinders = new LinkedHashMap<>(generateBugFinders());
        allFinders.putAll(generateSmellFinders());
        return allFinders;
    }

    private static Map<String, IssueFinder> generateSmellFinders() {
        Map<String, IssueFinder> smellFinders = new LinkedHashMap<>();

        // Smells
        registerSmellFinder(new AmbiguousParameterNameUnused(), smellFinders);
        registerSmellFinder(new DeadCode(), smellFinders);
        registerSmellFinder(new EmptyControlBody(), smellFinders);
        registerSmellFinder(new EmptyCustomBlock(), smellFinders);
        registerSmellFinder(new EmptyProject(), smellFinders);
        registerSmellFinder(new EmptyScript(), smellFinders);
        registerSmellFinder(new EmptySprite(), smellFinders);
        registerSmellFinder(new DeadCode(), smellFinders);
        registerSmellFinder(new DoubleIf(), smellFinders);
        registerSmellFinder(new DuplicatedScript(), smellFinders);
        registerSmellFinder(new DuplicateSprite(), smellFinders);
        registerSmellFinder(new LongScript(), smellFinders);
        registerSmellFinder(new MiddleMan(), smellFinders);
        registerSmellFinder(new MultiAttributeModification(), smellFinders);
        registerSmellFinder(new NestedLoops(), smellFinders);
        registerSmellFinder(new SameVariableDifferentSprite(), smellFinders);
        registerSmellFinder(new SequentialActions(), smellFinders);
        registerSmellFinder(new UnusedCustomBlock(), smellFinders);
        registerSmellFinder(new UnusedVariable(), smellFinders);
        registerSmellFinder(new VariableInitializationRace(), smellFinders);

        return smellFinders;
    }

    public static List<IssueFinder> getFinders(String commandString) {
        List<IssueFinder> finders = new ArrayList<>();

        switch (commandString) {
            case ALL:
                finders = new ArrayList<>(generateAllFinders().values());
                break;
            case BUGS:
                finders = new ArrayList<>(generateBugFinders().values());
                break;
            case SMELLS:
                finders = new ArrayList<>(generateSmellFinders().values());
                break;
            case DEFAULT:
                finders.addAll(generateAllFinders().values().stream().filter(f -> !f.getName().toLowerCase().endsWith("strict")).collect(Collectors.toList()));
                break;
            default:
                for (String detectorName : commandString.split(",")) {
                    Map<String, IssueFinder> allFinders = generateAllFinders();
                    if (!allFinders.containsKey(detectorName)) {
                        // TODO: Hard crash might be more appropriate to notify user
                        log.log(Level.SEVERE, "Unknown finder: " + detectorName);
                        continue;
                    }
                    finders.add(allFinders.get(detectorName));
                }
                break;
        }
        return Collections.unmodifiableList(finders);
    }

    public static Collection<String> getAllFinderNames() {
        return Collections.unmodifiableSet(generateAllFinders().keySet());
    }

    public static Collection<String> getBugFinderNames() {
        return Collections.unmodifiableSet(generateBugFinders().keySet());
    }

    public static Collection<String> getSmellFinderNames() {
        return Collections.unmodifiableSet(generateSmellFinders().keySet());
    }

    static void registerSmellFinder(IssueFinder finder, Map<String, IssueFinder> smellFinders) {
        if (finder.getIssueType() != IssueFinder.IssueType.SMELL) {
            throw new RuntimeException("Cannot register IssueFinder of Type "
                    + finder.getIssueType()
                    + " as Smell IssueFinder");
        }

        smellFinders.put(finder.getName(), finder);
    }

    static void registerBugFinder(IssueFinder finder, Map<String, IssueFinder> bugFinders) {
        if (finder.getIssueType() != IssueFinder.IssueType.BUG) {
            throw new RuntimeException("Cannot register IssueFinder of Type "
                    + finder.getIssueType()
                    + " as Bug IssueFinder");
        }
        bugFinders.put(finder.getName(), finder);
    }
}
