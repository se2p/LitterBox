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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import newanalytics.bugpattern.*;
import newanalytics.ctscore.FlowControl;
import newanalytics.smells.DeadCode;
import newanalytics.smells.EmptyScript;
import newanalytics.smells.EmptySprite;
import newanalytics.smells.LongScript;
import org.apache.commons.csv.CSVPrinter;
import scratch.ast.model.Program;
import utils.CSVWriter;
import java.util.*;

/**
 * Holds all IssueFinder and executes them.
 * Register new implemented checks here.
 */
public class IssueTool {

    private Map<String, IssueFinder> finder = new HashMap<>();

    public IssueTool() {
        finder.put(MissingPenUp.SHORT_NAME, new MissingPenUp());
        finder.put(AmbiguousParameterName.SHORT_NAME, new AmbiguousParameterName());
        finder.put(AmbiguousProcedureSignature.SHORT_NAME, new AmbiguousProcedureSignature());
        finder.put(MissingPenDown.SHORT_NAME, new MissingPenDown());
        finder.put(MissingEraseAll.SHORT_NAME, new MissingEraseAll());
        finder.put(NoWorkingScripts.SHORT_NAME, new NoWorkingScripts());
        finder.put(MissingCloneInitialization.SHORT_NAME, new MissingCloneInitialization());
        finder.put(OrphanedParameter.SHORT_NAME, new OrphanedParameter());
        finder.put(ParameterOutOfScope.SHORT_NAME, new ParameterOutOfScope());
        finder.put(IllegalParameterRefactor.SHORT_NAME, new IllegalParameterRefactor());
//        finder.put("cnt", new CountBlocks());
//        finder.put("glblstrt", new GlobalStartingPoint());
//        finder.put("strt", new StartingPoint());
//        finder.put("lggymve", new LaggyMovement());
//        finder.put("dblif", new DoubleIf());
        finder.put(MissingLoopSensing.SHORT_NAME, new MissingLoopSensing());
//        finder.put("clninit", new CloneInitialization());
        finder.put(MissingTerminationCondition.SHORT_NAME, new MissingTerminationCondition());
        finder.put(DeadCode.SHORT_NAME, new DeadCode());
//        finder.put("attrmod", new AttributeModification());
//        finder.put("emptybd", new EmptyBody());
//        finder.put("squact", new SequentialActions());
//        finder.put("sprtname", new SpriteNaming());
        finder.put(LongScript.SHORT_NAME, new LongScript());
//        finder.put("brdcstsync", new BroadcastSync());
//        finder.put("nstloop", new NestedLoops());
//        finder.put("dplscrpt", new DuplicatedScript());
//        finder.put("racecnd", new RaceCondition());
        finder.put(EmptyScript.SHORT_NAME, new EmptyScript());
        finder.put(EmptySprite.SHORT_NAME, new EmptySprite());
//        finder.put("mdlman", new MiddleMan());
//        finder.put("noop", new Noop());
//        finder.put("vrblscp", new VariableScope());
//        finder.put("unusedvar", new UnusedVariable());
//        finder.put("dplsprt", new DuplicatedSprite());
//        finder.put("inappint", new InappropriateIntimacy());
//        finder.put("noopprjct", new NoOpProject());
//
//        // To evaluate the CT score
//        finder.put("logthink", new LogicalThinking());
//        finder.put("abstr", new Abstraction());
//        finder.put("para", new Parallelism());
//        finder.put("synch", new Synchronization());
        finder.put(FlowControl.SHORT_NAME, new FlowControl());
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
        if (dtctrs.equals("all")) {
            detectors = finder.keySet().toArray(new String[0]);
        } else {
            detectors = dtctrs.split(",");
        }
        for (String s : detectors) {
            if (finder.containsKey(s)) {
                IssueFinder iF = finder.get(s);
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
        if (dtctrs.equals("all")) {
            detectors = finder.keySet().toArray(new String[0]);
        } else {
            detectors = dtctrs.split(",");
        }
        for (String s : detectors) {
            if (finder.containsKey(s)) {
                IssueFinder iF = finder.get(s);
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

    public Map<String, IssueFinder> getFinder() {
        return finder;
    }

    public void setFinder(Map<String, IssueFinder> finder) {
        this.finder = finder;
    }

    public static List<String> getOnlyUniqueActorList(List<String> foundSpritesWithIssues){
        Set<String> uniqueSprites = new TreeSet<>(foundSpritesWithIssues);
        return new ArrayList<>(uniqueSprites);

    }
}