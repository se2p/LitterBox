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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

import static de.uni_passau.fim.se2.litterbox.utils.GroupConstants.*;

/**
 * Holds all IssueFinder and executes them.
 * Register new implemented checks here.
 */
public class IssueTool {

    private Map<String, IssueFinder> bugFinder = new LinkedHashMap<>();
    private Map<String, IssueFinder> smellFinder = new LinkedHashMap<>();

    public IssueTool() {
        bugFinder.put(AmbiguousCustomBlockSignature.NAME, new AmbiguousCustomBlockSignature());
        bugFinder.put(AmbiguousParameterName.NAME, new AmbiguousParameterName());
        bugFinder.put(AmbiguousParameterNameStrict.NAME, new AmbiguousParameterNameStrict());
        bugFinder.put(CallWithoutDefinition.NAME, new CallWithoutDefinition());
        bugFinder.put(ComparingLiterals.NAME, new ComparingLiterals());
        bugFinder.put(CustomBlockWithForever.NAME, new CustomBlockWithForever());
        bugFinder.put(CustomBlockWithTermination.NAME, new CustomBlockWithTermination());
        bugFinder.put(EndlessRecursion.NAME, new EndlessRecursion());
        bugFinder.put(ExpressionAsTouchingOrColor.NAME, new ExpressionAsTouchingOrColor());
        bugFinder.put(ForeverInsideLoop.NAME, new ForeverInsideLoop());
        bugFinder.put(IllegalParameterRefactor.NAME, new IllegalParameterRefactor());
        bugFinder.put(MessageNeverReceived.NAME, new MessageNeverReceived());
        bugFinder.put(MessageNeverSent.NAME, new MessageNeverSent());
        bugFinder.put(MissingBackdropSwitch.NAME, new MissingBackdropSwitch());
        bugFinder.put(MissingCloneCall.NAME, new MissingCloneCall());
        bugFinder.put(MissingCloneInitialization.NAME, new MissingCloneInitialization());
        bugFinder.put(MissingInitialization.NAME, new MissingInitialization());
        bugFinder.put(MissingEraseAll.NAME, new MissingEraseAll());
        bugFinder.put(MissingLoopSensing.NAME, new MissingLoopSensing());
        bugFinder.put(MissingPenDown.NAME, new MissingPenDown());
        bugFinder.put(MissingPenUp.NAME, new MissingPenUp());
        bugFinder.put(MissingTerminationCondition.NAME, new MissingTerminationCondition());
        bugFinder.put(MissingWaitUntilCondition.NAME, new MissingWaitUntilCondition());
        bugFinder.put(NoWorkingScripts.NAME, new NoWorkingScripts());
        bugFinder.put(OrphanedParameter.NAME, new OrphanedParameter());
        bugFinder.put(ParameterOutOfScope.NAME, new ParameterOutOfScope());
        bugFinder.put(PositionEqualsCheck.NAME, new PositionEqualsCheck());
        bugFinder.put(RecursiveCloning.NAME, new RecursiveCloning());
        bugFinder.put(StutteringMovement.NAME, new StutteringMovement());

        // Smells
        smellFinder.put(EmptyControlBody.NAME, new EmptyControlBody());
        smellFinder.put(EmptyCustomBlock.NAME, new EmptyCustomBlock());
        smellFinder.put(EmptyProject.NAME, new EmptyProject());
        smellFinder.put(EmptyScript.NAME, new EmptyScript());
        smellFinder.put(EmptySprite.NAME, new EmptySprite());
        smellFinder.put(DeadCode.NAME, new DeadCode());
        smellFinder.put(LongScript.NAME, new LongScript());
        smellFinder.put(NestedLoops.NAME, new NestedLoops());
        smellFinder.put(SameVariableDifferentSprite.NAME, new SameVariableDifferentSprite());
        smellFinder.put(UnusedVariable.NAME, new UnusedVariable());
        smellFinder.put(UnusedCustomBlock.NAME, new UnusedCustomBlock());
    }

    /**
     * Executes all checks
     *
     * @param program the project to check
     */
    public Set<Issue> check(Program program, String[] detectors) {
        Preconditions.checkNotNull(program);
        Set<Issue> issues = new LinkedHashSet<>();
        for (String s : detectors) {
            // TODO: Why reconstruct this map all the time...
            if (getAllFinder().containsKey(s)) {
                IssueFinder iF = getAllFinder().get(s);
                issues.addAll(iF.check(program));
            }
        }
        return issues;
    }

    /**
     * FIXME REMOVE ME AFTER SCRATCH3ANALYZER IS REMOVED
     * Executes all checks
     *
     * @param program the project to check
     */
    public Set<Issue> check(Program program, String dtctrs) {
        Preconditions.checkNotNull(program);
        Set<Issue> issues = new LinkedHashSet<>();
        String[] detectors;
        switch (dtctrs) {
            case ALL:
                detectors = getAllFinder().keySet().toArray(new String[0]);
                break;
            case BUGS:
                detectors = getBugFinder().keySet().toArray(new String[0]);
                break;
            case SMELLS:
                detectors = getSmellFinder().keySet().toArray(new String[0]);
                break;
            default:
                detectors = dtctrs.split(",");
                break;
        }
        for (String s : detectors) {
            // TODO: Why reconstruct this map all the time...
            if (getAllFinder().containsKey(s)) {
                IssueFinder iF = getAllFinder().get(s);
                issues.addAll(iF.check(program));
            }
        }
        return issues;
    }

    public Map<String, IssueFinder> getAllFinder() {
        Map<String, IssueFinder> returnMap = new HashMap<>(smellFinder);
        returnMap.putAll(bugFinder);
        return returnMap;
    }

    public Map<String, IssueFinder> getSmellFinder() {
        Map<String, IssueFinder> returnMap = new HashMap<>(smellFinder);
        return returnMap;
    }

    public Map<String, IssueFinder> getBugFinder() {
        Map<String, IssueFinder> returnMap = new HashMap<>(bugFinder);
        return returnMap;
    }
}