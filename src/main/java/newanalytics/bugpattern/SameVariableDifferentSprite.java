package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import newanalytics.IssueTool;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.parser.symboltable.ExpressionListInfo;
import scratch.ast.parser.symboltable.VariableInfo;
import utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SameVariableDifferentSprite implements IssueFinder {
    private static final String NOTE1 = "There are no variables with the same name in your project.";
    private static final String NOTE2 = "Some of the variables have the same name but are in different sprites.";
    public static final String NAME = "same_variable_different_sprite";
    public static final String SHORT_NAME = "smvrbldffrntsprt";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        found = false;
        count = 0;
        actorNames = new LinkedList<>();
        String notes = NOTE1;
        if (count > 0) {
            notes = NOTE2;
        }
        Map<String, VariableInfo> variableInfoMap = program.getSymbolTable().getVariables();
        ArrayList<VariableInfo> varInfos = new ArrayList<>(variableInfoMap.values());
        for (int i = 0; i < varInfos.size(); i++) {
            String currentName = varInfos.get(i).getVariableName();
            String currentActor = varInfos.get(i).getActor();
            for (int j = 0; j < varInfos.size(); j++) {
                if (i != j && currentName.equals(varInfos.get(j).getVariableName()) && currentActor.equals(varInfos.get(j).getActor())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                found = false;
                count++;
                actorNames.add(currentActor);
            }
        }

        Map<String, ExpressionListInfo> listInfoMap = program.getSymbolTable().getLists();
        ArrayList<ExpressionListInfo> listInfos = new ArrayList<>(listInfoMap.values());
        for (int i = 0; i < listInfos.size(); i++) {
            String currentName = listInfos.get(i).getVariableName();
            String currentActor = listInfos.get(i).getActor();
            for (int j = 0; j < listInfos.size(); j++) {
                if (i != j && currentName.equals(listInfos.get(j).getVariableName()) && !currentActor.equals(listInfos.get(j).getActor())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                found = false;
                count++;
                actorNames.add(currentActor);
            }
        }

        return new IssueReport(NAME, count, IssueTool.getOnlyUniqueActorList(actorNames), notes);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
