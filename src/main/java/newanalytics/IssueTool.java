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
import newanalytics.ctscore.Abstraction;
import newanalytics.ctscore.DataRepresentation;
import newanalytics.ctscore.FlowControl;
import newanalytics.ctscore.LogicalThinking;
import newanalytics.ctscore.Parallelism;
import newanalytics.ctscore.Synchronization;
import newanalytics.ctscore.UserInteractivity;
import newanalytics.smells.AttributeModification;
import newanalytics.smells.BroadcastSync;
import newanalytics.smells.CloneInitialization;
import newanalytics.smells.CountBlocks;
import newanalytics.smells.DoubleIf;
import newanalytics.smells.DuplicatedScript;
import newanalytics.smells.DuplicatedSprite;
import newanalytics.smells.EmptyBody;
import newanalytics.smells.EmptyScript;
import newanalytics.smells.GlobalStartingPoint;
import newanalytics.smells.InappropriateIntimacy;
import newanalytics.smells.LaggyMovement;
import newanalytics.smells.LongScript;
import newanalytics.smells.LooseBlocks;
import newanalytics.smells.MiddleMan;
import newanalytics.smells.MissingForever;
import newanalytics.smells.MissingTermination;
import newanalytics.smells.NestedLoops;
import newanalytics.smells.NoOpProject;
import newanalytics.smells.Noop;
import newanalytics.smells.RaceCondition;
import newanalytics.smells.SequentialActions;
import newanalytics.smells.SpriteNaming;
import newanalytics.smells.StartingPoint;
import newanalytics.smells.UnusedVariable;
import newanalytics.smells.VariableScope;
import org.apache.commons.csv.CSVPrinter;
import scratch.ast.model.Program;
import utils.CSVWriter;

/**
 * Holds all IssueFinder and executes them.
 * Register new implemented checks here.
 */
public class IssueTool {

    private Map<String, IssueFinder> finder = new HashMap<>();

    public IssueTool() {
        finder.put("cnt", new CountBlocks());
        finder.put("glblstrt", new GlobalStartingPoint());
        finder.put("strt", new StartingPoint());
        finder.put("lggymve", new LaggyMovement());
        finder.put("dblif", new DoubleIf());
        finder.put("mssfrev", new MissingForever());
        finder.put("clninit", new CloneInitialization());
        finder.put("msstrm", new MissingTermination());
        finder.put("lsblck", new LooseBlocks());
        finder.put("attrmod", new AttributeModification());
        finder.put("emptybd", new EmptyBody());
        finder.put("squact", new SequentialActions());
        finder.put("sprtname", new SpriteNaming());
        finder.put("lngscr", new LongScript());
        finder.put("brdcstsync", new BroadcastSync());
        finder.put("nstloop", new NestedLoops());
        finder.put("dplscrpt", new DuplicatedScript());
        finder.put("racecnd", new RaceCondition());
        finder.put("emptyscrpt", new EmptyScript());
        finder.put("mdlman", new MiddleMan());
        finder.put("noop", new Noop());
        finder.put("vrblscp", new VariableScope());
        finder.put("unusedvar", new UnusedVariable());
        finder.put("dplsprt", new DuplicatedSprite());
        finder.put("inappint", new InappropriateIntimacy());
        finder.put("noopprjct", new NoOpProject());

        // To evaluate the CT score
        finder.put("logthink", new LogicalThinking());
        finder.put("abstr", new Abstraction());
        finder.put("para", new Parallelism());
        finder.put("synch", new Synchronization());
        finder.put("flow", new FlowControl());
        finder.put("userint", new UserInteractivity());
        finder.put("datarep", new DataRepresentation());
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
}