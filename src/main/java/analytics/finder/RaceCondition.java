package analytics.finder;

import analytics.Issue;
import analytics.IssueFinder;
import scratch2.data.ScBlock;
import scratch2.data.Script;
import scratch2.structure.Project;
import scratch2.structure.Scriptable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Checks for race conditions.
 */
public class RaceCondition implements IssueFinder {

    private String name = "race_condition";

    @Override
    public Issue check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        List<String> pos = new ArrayList<>();
        List<String> variables = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                List<String> temp = new ArrayList<>();
                if (script != null) {
                    if (script.getBlocks().size() > 1 && script.getBlocks().get(0).getContent().replace("\"", "").startsWith("whenGreenFlag")) {
                        for (ScBlock b : script.getBlocks()) {
                            if (b.getContent().startsWith("\"setVar:to:\"")) {
                                String[] parts = b.getContent().replace("\"setVar:to:\"\"", "").split("\"");
                                if (parts.length > 0 && !temp.contains(parts[0])) {
                                    temp.add(parts[0]);
                                }
                            }
                        }
                    }
                }
                for (String s : temp) {
                    if (variables.contains(s)) {
                        pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                    } else {
                        variables.add(s);
                    }
                }
            }
        }
        String note = "No variable gets initialised multiple times from different scripts at the beginning.";
        if (pos.size() > 0) {
            note = "Some variables get initialised multiple times from different scripts at the beginning.";

        }
        return new Issue(name, pos.size(), pos, project.getPath(), note);
    }

}
