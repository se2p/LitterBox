package newanalytics.smells;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.newast.model.ActorDefinition;
import scratch.newast.model.Program;
import scratch.newast.model.Script;
import scratch.newast.model.event.GreenFlag;
import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.expression.bool.IsKeyPressed;
import scratch.newast.model.expression.bool.IsMouseDown;
import scratch.newast.model.expression.bool.Touching;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.statement.control.IfElseStmt;
import scratch.newast.model.statement.control.IfThenStmt;
import scratch.newast.model.statement.control.RepeatForeverStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks for missing loops in event based actions.
 */
public class MissingForever implements IssueFinder {

    private String name = "missing_forever_loop";
    private List<String> found;
    private int counter;

    public MissingForever() {
        found = new ArrayList<>();
        counter = 0;
    }

    @Override
    public IssueReport check(Program program) {
        List<ActorDefinition> actorDefs = program.getActorDefinitionList().getDefintions();
        for (int i = 0; i < actorDefs.size(); i++) {
            List<Script> scripts = actorDefs.get(i).getScripts().getScriptList();
            for (int j = 0; j < scripts.size(); j++) {
                List<Stmt> stmts = scripts.get(0).getStmtList().getStmts().getListOfStmt();
                if (stmts.size() > 0 && scripts.get(0).getEvent() instanceof GreenFlag) {

                    checkMissForever(stmts, actorDefs.get(i).getIdent().getValue());
                }
            }
        }
        String note = "There is no fishy touching or keyPressed checks without a loop.";
        if (found.size() > 0) {
            note = "The project contains some fishy touching and / or keyPressed checks without a loop.";

        }
        return new IssueReport(name, counter, found, note);
    }

    private void checkMissForever(List<Stmt> stmts, String actorName) {
        for (int i = 0; i < stmts.size(); i++) {
            if (stmts.get(i) instanceof RepeatForeverStmt) {
                return;
            } else if (stmts.get(i) instanceof IfThenStmt) {
                BoolExpr bool = ((IfThenStmt) stmts.get(i)).getBoolExpr();
                if (bool instanceof IsKeyPressed || bool instanceof Touching || bool instanceof IsMouseDown) {
                    found.add(actorName);
                    counter++;
                } else {
                    checkMissForever(((IfThenStmt) stmts.get(i)).getThenStmts().getStmts().getListOfStmt(), actorName);
                }
            } else if (stmts.get(i) instanceof IfElseStmt) {
                BoolExpr bool = ((IfElseStmt) stmts.get(i)).getBoolExpr();
                if (bool instanceof IsKeyPressed || bool instanceof Touching || bool instanceof IsMouseDown) {
                    found.add(actorName);
                    counter++;
                } else {
                    checkMissForever(((IfElseStmt) stmts.get(i)).getStmtList().getStmts().getListOfStmt(), actorName);
                    checkMissForever(((IfElseStmt) stmts.get(i)).getElseStmts().getStmts().getListOfStmt(), actorName);
                }
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
