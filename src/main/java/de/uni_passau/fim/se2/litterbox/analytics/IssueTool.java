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

    private final static Map<String, IssueFinder> bugFinders = new LinkedHashMap<>();
    private final static Map<String, IssueFinder> smellFinders = new LinkedHashMap<>();
    private final static Map<String, IssueFinder> allFinders;

    static {
        registerBugFinder(new AmbiguousCustomBlockSignature());
        registerBugFinder(new AmbiguousParameterName());
        registerBugFinder(new AmbiguousParameterNameStrict());
        registerBugFinder(new CallWithoutDefinition());
        registerBugFinder(new ComparingLiterals());
        registerBugFinder(new CustomBlockWithForever());
        registerBugFinder(new CustomBlockWithTermination());
        registerBugFinder(new EndlessRecursion());
        registerBugFinder(new ExpressionAsTouchingOrColor());
        registerBugFinder(new ForeverInsideLoop());
        registerBugFinder(new IllegalParameterRefactor());
        registerBugFinder(new MessageNeverReceived());
        registerBugFinder(new MessageNeverSent());
        registerBugFinder(new MissingBackdropSwitch());
        registerBugFinder(new MissingCloneCall());
        registerBugFinder(new MissingCloneInitialization());
        registerBugFinder(new MissingInitialization());
        registerBugFinder(new MissingEraseAll());
        registerBugFinder(new MissingLoopSensing());
        registerBugFinder(new MissingPenDown());
        registerBugFinder(new MissingPenUp());
        registerBugFinder(new MissingTerminationCondition());
        registerBugFinder(new MissingWaitUntilCondition());
        registerBugFinder(new NoWorkingScripts());
        registerBugFinder(new OrphanedParameter());
        registerBugFinder(new ParameterOutOfScope());
        registerBugFinder(new PositionEqualsCheck());
        registerBugFinder(new RecursiveCloning());
        registerBugFinder(new StutteringMovement());

        // Smells
        registerSmellFinder(new EmptyControlBody());
        registerSmellFinder(new EmptyCustomBlock());
        registerSmellFinder(new EmptyProject());
        registerSmellFinder(new EmptyScript());
        registerSmellFinder(new EmptySprite());
        registerSmellFinder(new DeadCode());
        registerSmellFinder(new LongScript());
        registerSmellFinder(new NestedLoops());
        registerSmellFinder(new SameVariableDifferentSprite());
        registerSmellFinder(new UnusedVariable());
        registerSmellFinder(new UnusedCustomBlock());

        allFinders = new LinkedHashMap<>(bugFinders);
        allFinders.putAll(smellFinders);
    }

    public static List<IssueFinder> getFinders(String commandString) {
        List<IssueFinder> finders = new ArrayList<>();

        switch (commandString) {
            case ALL:
                finders.addAll(allFinders.values());
                break;
            case BUGS:
                finders.addAll(bugFinders.values());
                break;
            case SMELLS:
                finders.addAll(smellFinders.values());
                break;
            case DEFAULT:
                finders.addAll(allFinders.values().stream().filter(f -> !f.getName().toLowerCase().endsWith("strict")).collect(Collectors.toList()));
                break;
            default:
                for (String detectorName : commandString.split(",")) {
                    if (!allFinders.containsKey(detectorName)) {
                        // TODO: Hard crash might be more appropriate to notify user
                        log.log(Level.SEVERE, "Unknown finder: "+detectorName);
                        continue;
                    }
                    finders.add(allFinders.get(detectorName));
                }
                break;
        }
        return finders;
    }

    public static Collection<String> getAllFinderNames() {
        return Collections.unmodifiableSet(allFinders.keySet());
    }

    public static Collection<String> getBugFinderNames() {
        return Collections.unmodifiableSet(bugFinders.keySet());
    }

    public static Collection<String> getSmellFinderNames() {
        return Collections.unmodifiableSet(smellFinders.keySet());
    }

    static void registerSmellFinder(IssueFinder finder) {
        if (finder.getIssueType() != IssueFinder.IssueType.SMELL) {
            throw new RuntimeException("Cannot register IssueFinder of Type "
                    + finder.getIssueType()
                    + " as Smell IssueFinder");
        }

        smellFinders.put(finder.getName(), finder);
    }

    static void registerBugFinder(IssueFinder finder) {
        if (finder.getIssueType() != IssueFinder.IssueType.BUG) {
            throw new RuntimeException("Cannot register IssueFinder of Type "
                    + finder.getIssueType()
                    + " as Bug IssueFinder");
        }
        bugFinders.put(finder.getName(), finder);
    }
}
