package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import newanalytics.IssueTool;
import scratch.ast.model.Program;
import scratch.ast.model.variable.Identifier;
import scratch.ast.parser.symboltable.ArgumentInfo;
import scratch.ast.parser.symboltable.ProcedureInfo;
import utils.Preconditions;

import java.util.*;

public class AmbiguousParameterName implements IssueFinder {
    private static final String NOTE1 = "There are no ambiguous parameter names in your project.";
    private static final String NOTE2 = "Some of the procedures contain ambiguous parameter names.";
    public static final String NAME = "ambiguous_parameter_name";
    public static final String SHORT_NAME = "ambgsprmtrname";

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        List<String> found = new ArrayList<>();
        Map<String, Map<Identifier, ProcedureInfo>> procs = program.getProcedureMapping().getProcedures();
        Set<String> actors = procs.keySet();
        for (String actor : actors) {
            Map<Identifier, ProcedureInfo> current = procs.get(actor);
            Set<Identifier> ids = current.keySet();
            for (Identifier id : ids) {
                ProcedureInfo currentProc = current.get(id);
                if (checkArguments(currentProc.getArguments())) {
                    found.add(currentProc.getActorName());
                }
            }
        }
        String notes = NOTE1;
        if (found.size() > 0) {
            notes = NOTE2;
        }

        return new IssueReport(NAME, found.size(), IssueTool.getOnlyUniqueActorList(found), notes);
    }

    private boolean checkArguments(ArgumentInfo[] arguments) {
        for (int i = 0; i < arguments.length; i++) {
            ArgumentInfo current = arguments[i];
            for (int j = 0; j < arguments.length; j++) {
                if (i != j && current.getName().equals(arguments[j].getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
