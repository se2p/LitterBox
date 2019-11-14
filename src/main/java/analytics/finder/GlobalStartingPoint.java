package analytics.finder;

import analytics.IssueFinder;
import analytics.IssueReport;
import java.util.ArrayList;
import java.util.List;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

/**
 * Checks if the project has a starting point / 'GreenFlag'.
 */
public class GlobalStartingPoint implements IssueFinder {

    private String note1 = "The project is correctly initialized and has a 'Green Flag'.";
    private String note2 = "The project is not correctly initialized and has no 'Green Flag'!";
    private String name = "has_global_start";

    @Override
    public IssueReport check(Project project) {
        if (project.getVersion().equals(Version.SCRATCH2)) {
            return runCheck(project, Identifier.LEGACY_GREEN_FLAG.getValue());
        } else if (project.getVersion().equals(Version.SCRATCH3)) {
            return runCheck(project, Identifier.GREEN_FLAG.getValue());
        }
        return null;
    }

    private IssueReport runCheck(Project project, String idf) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        boolean hasGreenFlag = false;
        int count = 0;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script.getBlocks().size() > 1 && script.getBlocks().get(0).getContent().startsWith(idf)) {
                    hasGreenFlag = true;
                    break;
                }
            }
            if (hasGreenFlag) {
                break;
            }
        }
        String notes;
        if (!hasGreenFlag) {
            count = 1;
            //System.out.println(pos);
            pos = new ArrayList<>();
            pos.add("Project");
            notes = note2;
        } else {
            notes = note1;
        }
        return new IssueReport(name, count, pos, project.getPath(), notes);
    }

    @Override
    public String getName() {
        return name;
    }
}
