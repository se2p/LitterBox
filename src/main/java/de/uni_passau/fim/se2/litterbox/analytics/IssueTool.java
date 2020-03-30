/*
 * Copyright (C) 2019 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.AmbiguousCustomBlockSignature;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.AmbiguousParameterName;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.CallWithoutDefinition;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.ComparingLiterals;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.CustomBlockWithForever;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.CustomBlockWithTermination;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.EndlessRecursion;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.ExpressionAsTouchingOrColor;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.ForeverInsideLoop;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.IllegalParameterRefactor;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MessageNeverReceived;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MessageNeverSent;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingBackdropSwitch;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingCloneCall;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingCloneInitialization;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingEraseAll;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingLoopSensing;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingPenDown;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingPenUp;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingTerminationCondition;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingWaitUntilCondition;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.NoWorkingScripts;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.OrphanedParameter;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.ParameterOutOfScope;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.PositionEqualsCheck;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.RecursiveCloning;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.SameVariableDifferentSprite;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.StutteringMovement;
import de.uni_passau.fim.se2.litterbox.analytics.ctscore.FlowControl;
import de.uni_passau.fim.se2.litterbox.analytics.smells.DeadCode;
import de.uni_passau.fim.se2.litterbox.analytics.smells.EmptyControlBody;
import de.uni_passau.fim.se2.litterbox.analytics.smells.EmptyCustomBlock;
import de.uni_passau.fim.se2.litterbox.analytics.smells.EmptyProject;
import de.uni_passau.fim.se2.litterbox.analytics.smells.EmptyScript;
import de.uni_passau.fim.se2.litterbox.analytics.smells.EmptySprite;
import de.uni_passau.fim.se2.litterbox.analytics.smells.LongScript;
import de.uni_passau.fim.se2.litterbox.analytics.smells.NestedLoops;
import de.uni_passau.fim.se2.litterbox.analytics.smells.UnusedCustomBlock;
import de.uni_passau.fim.se2.litterbox.analytics.smells.UnusedVariable;
import de.uni_passau.fim.se2.litterbox.analytics.utils.BlockCount;
import de.uni_passau.fim.se2.litterbox.analytics.utils.ProcedureCount;
import de.uni_passau.fim.se2.litterbox.analytics.utils.ProgramUsingPen;
import de.uni_passau.fim.se2.litterbox.analytics.utils.SpriteCount;
import de.uni_passau.fim.se2.litterbox.analytics.utils.WeightedMethodCount;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.CSVWriter;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static de.uni_passau.fim.se2.litterbox.utils.GroupConstants.*;

/**
 * Holds all IssueFinder and executes them.
 * Register new implemented checks here.
 */
public class IssueTool {

    private Map<String, IssueFinder> utilFinder = new HashMap<>();
    private Map<String, IssueFinder> bugFinder = new HashMap<>();
    private Map<String, IssueFinder> smellFinder = new HashMap<>();
    private Map<String, IssueFinder> ctScoreFinder = new HashMap<>();

    public IssueTool() {
        bugFinder.put(AmbiguousCustomBlockSignature.SHORT_NAME, new AmbiguousCustomBlockSignature());
        bugFinder.put(AmbiguousParameterName.SHORT_NAME, new AmbiguousParameterName());
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

        //Smells
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

        //UtilFinder
        utilFinder.put(BlockCount.SHORT_NAME, new BlockCount());
        utilFinder.put(SpriteCount.SHORT_NAME, new SpriteCount());
        utilFinder.put(ProcedureCount.SHORT_NAME, new ProcedureCount());
        utilFinder.put(ProgramUsingPen.SHORT_NAME, new ProgramUsingPen());
        utilFinder.put(WeightedMethodCount.SHORT_NAME, new WeightedMethodCount());

        // To evaluate the CT score
        ctScoreFinder.put(FlowControl.SHORT_NAME, new FlowControl());
    }

    public static List<String> getOnlyUniqueActorList(List<String> foundSpritesWithIssues) {
        Set<String> uniqueSprites = new TreeSet<>(foundSpritesWithIssues);
        return new ArrayList<>(uniqueSprites);

    }

    /**
     * Executes all checks. Only creates console output for a single project.
     *
     * @param program the project to check
     */
    public void checkRaw(Program program, String dtctrs) {
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
            case CTSCORE:
                detectors = getCTScoreFinder().keySet().toArray(new String[0]);
                break;
            default:
                detectors = dtctrs.split(",");
                break;
        }
        for (String s : detectors) {
            if (getAllFinder().containsKey(s)) {
                IssueFinder iF = getAllFinder().get(s);
                if (program != null) {
                    IssueReport issueReport = iF.check(program);
                    System.out.println(issueReport);
                }
            }
        }
    }

    /**
     * Executes all checks
     *
     * @param program the project to check
     */
    public void check(Program program, CSVPrinter printer, String dtctrs) {
        List<IssueReport> issueReports = new ArrayList<>();
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
            case CTSCORE:
                detectors = getCTScoreFinder().keySet().toArray(new String[0]);
                break;
            default:
                detectors = dtctrs.split(",");
                break;
        }
        for (String s : detectors) {
            if (getAllFinder().containsKey(s)) {
                IssueFinder iF = getAllFinder().get(s);
                if (program != null) {
                    IssueReport issueReport = iF.check(program);
                    issueReports.add(issueReport);
                    //System.out.println(issueReport);
                } else {
                    return;
                }
            }
        }
        try {
            CSVWriter.addData(printer, issueReports, program);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, IssueFinder> getAllFinder() {
        Map<String, IssueFinder> returnMap = new HashMap<>(smellFinder);
        returnMap.putAll(utilFinder);
        returnMap.putAll(bugFinder);
        returnMap.putAll(ctScoreFinder);
        return returnMap;
    }

    public Map<String, IssueFinder> getSmellFinder() {
        Map<String, IssueFinder> returnMap = new HashMap<>(smellFinder);
        returnMap.putAll(utilFinder);
        return returnMap;
    }

    public Map<String, IssueFinder> getBugFinder() {
        Map<String, IssueFinder> returnMap = new HashMap<>(bugFinder);
        returnMap.putAll(utilFinder);
        return returnMap;
    }

    public Map<String, IssueFinder> getCTScoreFinder() {
        Map<String, IssueFinder> returnMap = new HashMap<>(ctScoreFinder);
        returnMap.putAll(utilFinder);
        return returnMap;
    }
}