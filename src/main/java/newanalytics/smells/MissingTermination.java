package newanalytics.smells;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.newast.model.ActorDefinition;
import scratch.newast.model.Program;
import scratch.newast.model.Script;
import scratch.newast.model.expression.bool.UnspecifiedBoolExpr;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.statement.control.IfElseStmt;
import scratch.newast.model.statement.control.IfThenStmt;
import scratch.newast.model.statement.control.UntilStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks for missing statements in repeat-until blocks.
 */
public class MissingTermination implements IssueFinder {

    private String name = "missing_termination";
    private List<String> found;
    private int counter;

    public MissingTermination() {
        found = new ArrayList<>();
        counter=0;
    }

    @Override
    public IssueReport check(Program program) {
        List<ActorDefinition> actorDefs = program.getActorDefinitionList().getDefintions();
        for (int i = 0; i < actorDefs.size(); i++) {
            List<Script> scripts = actorDefs.get(i).getScripts().getScriptList();
            for (int j = 0; j < scripts.size(); j++) {
                List<Stmt> stmts = scripts.get(0).getStmtList().getStmts().getListOfStmt();
                if(stmts.size()>0){
                    checkMissTermination(stmts, actorDefs.get(i).getIdent().getValue());
                }
            }
        }

        String notes = "All 'repeat until' blocks terminating correctly.";
        if (counter > 0) {
            notes = "Some 'repeat until' blocks have no termination statement.";
        }
        return new IssueReport(name, counter, found, notes);
    }

    private void checkMissTermination(List<Stmt> stmts, String actorName) {
        for (int i = 0; i < stmts.size(); i++) {
            if (stmts.get(i) instanceof UntilStmt){
                if (((UntilStmt) stmts.get(i)).getBoolExpr() instanceof UnspecifiedBoolExpr){
                    counter++;
                    found.add(actorName);
                }
            }else if (stmts.get(i) instanceof IfThenStmt){
                checkMissTermination(((IfThenStmt) stmts.get(i)).getThenStmts().getStmts().getListOfStmt(),actorName);
            }else if (stmts.get(i) instanceof IfElseStmt){
                checkMissTermination(((IfElseStmt) stmts.get(i)).getStmtList().getStmts().getListOfStmt(),actorName);
                checkMissTermination(((IfElseStmt) stmts.get(i)).getElseStmts().getStmts().getListOfStmt(),actorName);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
