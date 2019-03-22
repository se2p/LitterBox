package analytics;

import analytics.finder.*;
import org.apache.commons.csv.CSVPrinter;
import scratch2.structure.Project;
import utils.CSVWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds all IssueFinder and executes them.
 * Register new implemented checks here.
 */
public class IssueTool {

    private List<IssueFinder> finder = new ArrayList<>();

    public IssueTool() {
        finder.add(new CountBlocks());
        finder.add(new GlobalStartingPoint());
        finder.add(new StartingPoint());
        finder.add(new LaggyMovement());
        finder.add(new DoubleIf());
        finder.add(new MissingForever());
        finder.add(new CloneInitialization());
        finder.add(new MissingTermination());
        finder.add(new LooseBlocks());
        finder.add(new AttributeModification());
        finder.add(new EmptyBody());
        finder.add(new SequentialActions());
        finder.add(new SpriteNaming());
        finder.add(new LongScript());
        finder.add(new BroadcastSync());
        finder.add(new NestedLoops());
        finder.add(new DuplicatedScript());
        finder.add(new RaceCondition());
        finder.add(new EmptyScript());
        finder.add(new MiddleMan());
        finder.add(new Noop());
        finder.add(new VariableScope());
        finder.add(new UnusedVariable());
        finder.add(new DuplicatedSprite());
        finder.add(new InappropriateIntimacy());
        finder.add(new NoOpProject());
    }

    /**
     * Executes all checks
     *
     * @param project the project to check
     */
    public void check(Project project, CSVPrinter printer, String name) {
        List<Issue> issues = new ArrayList<>();
        for (IssueFinder iF : finder) {
            if (project != null) {
                Issue issue = iF.check(project);
                issues.add(issue);
                //System.out.println(issue);
            }
        }

        try {
            CSVWriter.addData(printer, project, issues, name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}