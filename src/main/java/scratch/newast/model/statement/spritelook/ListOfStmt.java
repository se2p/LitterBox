package scratch.newast.model.statement.spritelook;

import scratch.newast.model.statement.Stmt;

import java.util.List;

public class ListOfStmt {

    List<Stmt> listOfStmt;

    public ListOfStmt(List<Stmt> listOfStmt) {
        this.listOfStmt = listOfStmt;
    }

    public List<Stmt> getListOfStmt() {
        return listOfStmt;
    }

    public void setListOfStmt(List<Stmt> listOfStmt) {
        this.listOfStmt = listOfStmt;
    }
}
