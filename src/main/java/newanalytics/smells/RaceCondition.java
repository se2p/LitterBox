package newanalytics.smells;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.newast.model.Program;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

/**
 * Checks for race conditions.
 */
public class RaceCondition implements IssueFinder {

    String name = "race_condition";

    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(program.getStage());
        scriptables.addAll(program.getSprites());
        List<String> pos = new ArrayList<>();
        List<String> variables = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                List<String> temp = new ArrayList<>();
                if (program.getVersion().equals(Version.SCRATCH2)) {
                    checkVariables2(script, temp);
                } else if (program.getVersion().equals(Version.SCRATCH3)) {
                    checkVariables3(script, temp);
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
        return new IssueReport(name, pos.size(), pos, program.getPath(), note);

         */
        throw new RuntimeException("not implemented");
    }

    private void checkVariables3(Script script, List<String> temp) {
        if (script.getBlocks().size() > 1 && script.getBlocks().get(0).getContent().startsWith(Identifier.GREEN_FLAG.getValue())) {
            for (ScBlock b : script.getBlocks()) {
                if (b.getContent().startsWith(Identifier.SET_VAR.getValue())) {
                    String var = b.getFields().get(Identifier.FIELD_VARIABLE.getValue()).get(0);
                    temp.add(var);
                }
            }

        }
    }

    @Override
    public String getName() {
        return name;
    }
}
