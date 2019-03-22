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
 * Checks for duplicated scripts. Only uses full String representation comparison.
 * TODO: [Improvement] Compare sub-sequences of scripts (maybe reuse the commented code below, but care, it did not work as expected)
 */
public class DuplicatedScript implements IssueFinder {

    @Override
    public Issue check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count = 0;
        List<String> pos = new ArrayList<>();
        List<String> duplicated = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script != null) {
                    if (script.getBlocks().size() > 1) {
                        searchBlocks(scriptables, scable, script, pos, duplicated);
                        //findSubsequ(scriptables, scable, script, pos);
                    }
                }
            }
        }
        count = pos.size();
        String notes = "No duplicated code found.";
        if (count > 0) {
            notes = "Some scripts have duplicated code.";
        }

        String name = "duplicated_script";
        return new Issue(name, count, pos, project.getPath(), notes);
    }


    private void searchBlocks(List<Scriptable> scriptables, Scriptable currentSc, Script sc, List<String> pos, List<String> duplicated) {
        String toSearch = sc.getBlocks().toString();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script != null) {
                    if (script.getBlocks().size() > 1) {
                        if (script.getBlocks().toString().equals(toSearch) && script.getPosition() != sc.getPosition() && !duplicated.contains(toSearch)) {
                            pos.add(currentSc.getName() + " and " + scable.getName() + " at " + Arrays.toString(sc.getPosition()) + " and " + Arrays.toString(script.getPosition()));
                            duplicated.add(toSearch);
                        }
                    }
                }
            }
        }
    }

//    private void findSubsequ(List<Scriptable> scriptables, Scriptable scable, Script script, List<String> pos) {
//        List<ScBlock> blocks = script.getBlocks();
//        for (int i = 0; i < blocks.size(); i++) {
//            for (int m = 0; m < blocks.size(); m++) {
//                if (i < m) {
//                    List<ScBlock> temp = blocks.subList(i, m);
//                    searchSub(scriptables, scable, script.getPosition(), temp, pos);
//                }
//            }
//        }
//    }
//
//    private void searchSub(List<Scriptable> scriptables, Scriptable currentSc, double[] xy, List<ScBlock> subsequence, List<String> pos){
//        for (Scriptable scriptable : scriptables) {
//            for (Script sc : scriptable.getScripts()) {
//                if (sc.getBlocks().toString().contains(subsequence.toString())) {
//                    pos.add(currentSc.getName() + " and " + scriptable.getName() + " at " + Arrays.toString(xy) + " and " + Arrays.toString(sc.getPosition()));
//                }
//            }
//        }
//    }
}
