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

import static de.uni_passau.fim.se2.litterbox.utils.GroupConstants.*;


import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.*;
import de.uni_passau.fim.se2.litterbox.analytics.smells.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

/**
 * Holds all IssueFinder and executes them.
 * Register new implemented checks here.
 */
public class IssueTool {

    private Map<String, IssueFinder> bugFinder = new LinkedHashMap<>();
    private Map<String, IssueFinder> smellFinder = new LinkedHashMap<>();

    public IssueTool() {
        bugFinder.put(AmbiguousCustomBlockSignature.SHORT_NAME, new AmbiguousCustomBlockSignature());
        bugFinder.put(AmbiguousParameterName.SHORT_NAME, new AmbiguousParameterName());
        bugFinder.put(AmbiguousParameterNameStrict.SHORT_NAME, new AmbiguousParameterNameStrict());
        bugFinder.put(CallWithoutDefinition.SHORT_NAME, new CallWithoutDefinition());
        bugFinder.put(ComparingLiterals.SHORT_NAME, new ComparingLiterals());
        bugFinder.put(CustomBlockWithForever.SHORT_NAME, new CustomBlockWithForever());
        bugFinder.put(CustomBlockWithTermination.SHORT_NAME, new CustomBlockWithTermination());
        bugFinder.put(EndlessRecursion.SHORT_NAME, new EndlessRecursion());
        bugFinder.put(ExpressionAsTouchingOrColor.SHORT_NAME, new ExpressionAsTouchingOrColor());
        bugFinder.put(ForeverInsideLoop.SHORT_NAME, new ForeverInsideLoop());
        bugFinder.put(IllegalParameterRefactor.SHORT_NAME, new IllegalParameterRefactor());
        bugFinder.put(MessageNeverReceived.SHORT_NAME, new MessageNeverReceived());
        bugFinder.put(MessageNeverSent.SHORT_NAME, new MessageNeverSent());
        bugFinder.put(MissingBackdropSwitch.SHORT_NAME, new MissingBackdropSwitch());
        bugFinder.put(MissingCloneCall.SHORT_NAME, new MissingCloneCall());
        bugFinder.put(MissingCloneInitialization.SHORT_NAME, new MissingCloneInitialization());
        bugFinder.put(MissingInitialization.SHORT_NAME, new MissingInitialization());
        bugFinder.put(MissingEraseAll.SHORT_NAME, new MissingEraseAll());
        bugFinder.put(MissingLoopSensing.SHORT_NAME, new MissingLoopSensing());
        bugFinder.put(MissingPenDown.SHORT_NAME, new MissingPenDown());
        bugFinder.put(MissingPenUp.SHORT_NAME, new MissingPenUp());
        bugFinder.put(MissingTerminationCondition.SHORT_NAME, new MissingTerminationCondition());
        bugFinder.put(MissingWaitUntilCondition.SHORT_NAME, new MissingWaitUntilCondition());
        bugFinder.put(NoWorkingScripts.SHORT_NAME, new NoWorkingScripts());
        bugFinder.put(OrphanedParameter.SHORT_NAME, new OrphanedParameter());
        bugFinder.put(ParameterOutOfScope.SHORT_NAME, new ParameterOutOfScope());
        bugFinder.put(PositionEqualsCheck.SHORT_NAME, new PositionEqualsCheck());
        bugFinder.put(RecursiveCloning.SHORT_NAME, new RecursiveCloning());
        bugFinder.put(SameVariableDifferentSprite.SHORT_NAME, new SameVariableDifferentSprite());
        bugFinder.put(StutteringMovement.SHORT_NAME, new StutteringMovement());

        // Smells
        smellFinder.put(EmptyControlBody.SHORT_NAME, new EmptyControlBody());
        smellFinder.put(EmptyCustomBlock.SHORT_NAME, new EmptyCustomBlock());
        smellFinder.put(EmptyProject.SHORT_NAME, new EmptyProject());
        smellFinder.put(EmptyScript.SHORT_NAME, new EmptyScript());
        smellFinder.put(EmptySprite.SHORT_NAME, new EmptySprite());
        smellFinder.put(DeadCode.SHORT_NAME, new DeadCode());
        smellFinder.put(LongScript.SHORT_NAME, new LongScript());
        smellFinder.put(NestedLoops.SHORT_NAME, new NestedLoops());
        smellFinder.put(UnusedVariable.SHORT_NAME, new UnusedVariable());
        smellFinder.put(UnusedCustomBlock.SHORT_NAME, new UnusedCustomBlock());
    }

    /**
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