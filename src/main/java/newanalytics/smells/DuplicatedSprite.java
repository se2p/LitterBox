package newanalytics.smells;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;

import java.util.*;

/**
 * Checks for duplicated sprites.
 */
public class DuplicatedSprite implements IssueFinder {

    String name = "duplicated_sprite";

    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count;
        List<String> duplicated = new ArrayList<>();
        List<String> pos = new ArrayList<>();
        Map<String, List<String>> scriptMap = new HashMap<>();
        for (Scriptable scable : scriptables) {
            if (scable.getScripts().size() > 0) {
                scriptMap.put(scable.getName(), new ArrayList<>());
                for (Script script : scable.getScripts()) {
                    scriptMap.get(scable.getName()).add(script.getBlocks().toString());
                }
            }
        }
        for (String check : scriptMap.keySet()) {
            for (String check2 : scriptMap.keySet()) {
                if (!check.equals(check2)) {
                    if (equalLists(scriptMap.get(check), scriptMap.get(check2))) {
                        if (!pos.contains(check2 + " and " + check) && !duplicated.contains(check2)) {
                            pos.add(check + " and " + check2);
                            duplicated.add(check2);
                        }
                    }
                }
            }
        }
        count = pos.size();
        String notes = "There are no duplicated sprites in your project.";
        if (count > 0) {
            notes = "There are duplicated sprites in your project.";
        }

        return new IssueReport(name, count, pos, project.getPath(), notes);
    }


    private boolean equalLists(List<String> one, List<String> two) {
        if (one == null && two == null) {
            return true;
        }

        if (one == null || two == null || one.size() != two.size()) {
            return false;
        }

        one = new ArrayList<>(one);
        two = new ArrayList<>(two);
        Collections.sort(one);
        Collections.sort(two);
        return one.equals(two);
    }

    @Override
    public String getName() {
        return name;
    }
}
