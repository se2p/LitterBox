package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.Program;
import scratch.ast.model.variable.Identifier;
import scratch.ast.parser.ProgramParser;
import scratch.ast.parser.symboltable.ProcedureInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Ambiguous_Procedure_Signature implements IssueFinder {
    private static final String NOTE1 = "There are no ambiguous procedure signatures in your project.";
    private static final String NOTE2 = "Some of the procedures signatures are ambiguous.";
    public static final String NAME = "ambiguous_procedure_signature";
    public static final String SHORT_NAME = "ambgsprcdrsgntr";

    @Override
    public IssueReport check(Program program) {
        List<String> found = new ArrayList<>();
        HashMap<Identifier, ProcedureInfo> procs = ProgramParser.procDefMap.getProcedures();
        List<ProcedureInfo> procedureInfos = new ArrayList<>(procs.values());
        for (int i = 0; i < procedureInfos.size(); i++) {
            ProcedureInfo current = procedureInfos.get(i);
            for (int j = 0; j < procedureInfos.size(); j++) {
                if (i != j && current.equals(procedureInfos.get(j))) {
                    found.add(current.getActorName());
                }
            }
        }
        String notes = NOTE1;
        if (found.size() > 0) {
            notes = NOTE2;
        }

        return new IssueReport(NAME, found.size(), found, notes);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
