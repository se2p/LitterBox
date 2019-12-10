package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.Program;
import scratch.ast.model.variable.Identifier;
import scratch.ast.parser.ProgramParser;
import scratch.ast.parser.symboltable.ArgumentInfo;
import scratch.ast.parser.symboltable.ProcedureInfo;

import java.util.*;

public class AmbiguousParameterName implements IssueFinder {
    private static final String NOTE1 = "There are no ambiguous parameter names in your project.";
    private static final String NOTE2 = "Some of the procedures contain ambiguous parameter names.";
    public static final String NAME = "ambiguous_parameter_name";
    public static final String SHORT_NAME = "ambgsprmtrname";

    @Override
    public IssueReport check(Program program) {
        List<String> found = new ArrayList<>();
        HashMap<Identifier, ProcedureInfo> procs = program.getProcedureMapping().getProcedures();
        Set<Identifier> ids = procs.keySet();
        for (Identifier id : ids) {
            ProcedureInfo current = procs.get(id);
            if (checkArguments(current.getArguments())) {
                found.add(current.getActorName());
            }
        }
        String notes = NOTE1;
        if (found.size() > 0) {
            notes = NOTE2;
        }

        return new IssueReport(NAME, found.size(), found, notes);
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
