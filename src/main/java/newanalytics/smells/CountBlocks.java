package newanalytics.smells;

import java.util.ArrayList;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.newast.model.Program;
import scratch.structure.Scriptable;

/**
 * Counts the blocks of a project.
 */
public class CountBlocks implements IssueFinder {

    String name = "block_count";

    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(program.getStage());
        scriptables.addAll(program.getSprites());
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
        return new IssueReport(name, count, new ArrayList<>(), program.getPath(), "");
         */
        throw new RuntimeException("not implemented");
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
