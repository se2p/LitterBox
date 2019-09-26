package analytics.finder;

import analytics.IssueReport;
import analytics.IssueFinder;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;

import java.util.ArrayList;
import java.util.List;

/**
 * Counts the blocks of a project.
 */
public class CountBlocks implements IssueFinder {

    String name = "block_count";

    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count = 0;
        List<Integer> countList = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                searchBlocks(script.getBlocks(), countList);
            }
        }
        for (int x : countList) {
            count += x;
        }
        return new IssueReport(name, count, new ArrayList<>(), project.getPath(), "");
    }

    private void searchBlocks(List<ScBlock> blocks, List<Integer> countList) {
        countList.add(blocks.size());
        for (ScBlock b : blocks) {
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks(b.getNestedBlocks(), countList);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks(b.getElseBlocks(), countList);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
