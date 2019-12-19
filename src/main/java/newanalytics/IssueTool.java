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
package newanalytics;

import java.io.IOException;
import java.util.*;

import newanalytics.bugpattern.*;
import newanalytics.ctscore.FlowControl;
import newanalytics.smells.*;
import newanalytics.utils.BlockCount;
import newanalytics.utils.ProcedureCount;
import newanalytics.utils.SpriteCount;
import org.apache.commons.csv.CSVPrinter;
import scratch.ast.model.Program;
import utils.CSVWriter;

import static utils.GroupConstants.*;
import static utils.GroupConstants.CTSCORE;

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
        bugFinder.put(MissingPenUp.SHORT_NAME, new MissingPenUp());
        bugFinder.put(AmbiguousParameterName.SHORT_NAME, new AmbiguousParameterName());
        bugFinder.put(AmbiguousProcedureSignature.SHORT_NAME, new AmbiguousProcedureSignature());
        bugFinder.put(MissingPenDown.SHORT_NAME, new MissingPenDown());
        bugFinder.put(MissingEraseAll.SHORT_NAME, new MissingEraseAll());
        bugFinder.put(NoWorkingScripts.SHORT_NAME, new NoWorkingScripts());
        bugFinder.put(MissingCloneInitialization.SHORT_NAME, new MissingCloneInitialization());
        bugFinder.put(MissingCloneCall.SHORT_NAME, new MissingCloneCall());
        bugFinder.put(OrphanedParameter.SHORT_NAME, new OrphanedParameter());
        bugFinder.put(ParameterOutOfScope.SHORT_NAME, new ParameterOutOfScope());
        bugFinder.put(IllegalParameterRefactor.SHORT_NAME, new IllegalParameterRefactor());
        bugFinder.put(ProcedureWithForever.SHORT_NAME, new ProcedureWithForever());
        bugFinder.put(ProcedureWithTermination.SHORT_NAME, new ProcedureWithTermination());
        bugFinder.put(ForeverInsideLoop.SHORT_NAME, new ForeverInsideLoop());
        bugFinder.put(EqualsCondition.SHORT_NAME, new EqualsCondition());
        bugFinder.put(CallWithoutDefinition.SHORT_NAME, new CallWithoutDefinition());
        bugFinder.put(MessageNeverReceived.SHORT_NAME, new MessageNeverReceived());
        bugFinder.put(MessageNeverSent.SHORT_NAME, new MessageNeverSent());
        bugFinder.put(EndlessRecursion.SHORT_NAME, new EndlessRecursion());
//        finder.put("glblstrt", new GlobalStartingPoint());
//        finder.put("strt", new StartingPoint());
        bugFinder.put(StutteringMovement.SHORT_NAME, new StutteringMovement());
//        finder.put("dblif", new DoubleIf());
        bugFinder.put(MissingLoopSensing.SHORT_NAME, new MissingLoopSensing());
        bugFinder.put(MissingTerminationCondition.SHORT_NAME, new MissingTerminationCondition());
        bugFinder.put(ExpressionAsColor.SHORT_NAME, new ExpressionAsColor());
        bugFinder.put(RecursiveCloning.SHORT_NAME, new RecursiveCloning());
        smellFinder.put(DeadCode.SHORT_NAME, new DeadCode());
//        finder.put("attrmod", new AttributeModification());

//        finder.put("squact", new SequentialActions());
//        finder.put("sprtname", new SpriteNaming());
        smellFinder.put(LongScript.SHORT_NAME, new LongScript());
        smellFinder.put(NestedLoops.SHORT_NAME, new NestedLoops());
        smellFinder.put(UnusedVariable.SHORT_NAME, new UnusedVariable());
        smellFinder.put(UnusedProcedure.SHORT_NAME, new UnusedProcedure());
//        finder.put("dplscrpt", new DuplicatedScript());
//        finder.put("racecnd", new RaceCondition());
        smellFinder.put(EmptyControlBody.SHORT_NAME, new EmptyControlBody());
        smellFinder.put(EmptyScript.SHORT_NAME, new EmptyScript());
        smellFinder.put(EmptySprite.SHORT_NAME, new EmptySprite());
        smellFinder.put(EmptyProject.SHORT_NAME, new EmptyProject());
        smellFinder.put(EmptyProcedure.SHORT_NAME, new EmptyProcedure());
//        finder.put("mdlman", new MiddleMan());
//        finder.put("vrblscp", new VariableScope());
//        finder.put("dplsprt", new DuplicatedSprite());
//        finder.put("inappint", new InappropriateIntimacy());

        //UtilFinder
        utilFinder.put(BlockCount.SHORT_NAME, new BlockCount());
        utilFinder.put(SpriteCount.SHORT_NAME, new SpriteCount());
        utilFinder.put(ProcedureCount.SHORT_NAME, new ProcedureCount());
//
//        // To evaluate the CT score
//        finder.put("logthink", new LogicalThinking());
//        finder.put("abstr", new Abstraction());
//        finder.put("para", new Parallelism());
//        finder.put("synch", new Synchronization());
        ctScoreFinder.put(FlowControl.SHORT_NAME, new FlowControl());
//        finder.put("userint", new UserInteractivity());
//        finder.put("datarep", new DataRepresentation());
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

    public static List<String> getOnlyUniqueActorList(List<String> foundSpritesWithIssues) {
        Set<String> uniqueSprites = new TreeSet<>(foundSpritesWithIssues);
        return new ArrayList<>(uniqueSprites);

    }
}