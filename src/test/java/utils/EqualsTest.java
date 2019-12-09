package utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import scratch.ast.model.StmtList;
import scratch.ast.model.literals.NumberLiteral;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.control.RepeatTimesStmt;
import scratch.ast.model.statement.spritelook.ListOfStmt;
import scratch.ast.model.statement.spritemotion.MoveSteps;

import java.util.ArrayList;
import java.util.List;

public class EqualsTest {

    @Test
    public void testScriptEquals(){
        NumberLiteral num = new NumberLiteral(2);
        MoveSteps goTo = new MoveSteps(new NumberLiteral(12));
        List<Stmt> list = new ArrayList<>();
        list.add(goTo);
        RepeatTimesStmt repeat = new RepeatTimesStmt(num, new StmtList(new ListOfStmt(list)));
        NumberLiteral num2 = new NumberLiteral(2);
        Assertions.assertTrue(num.equals(num2));
        MoveSteps goTo2 = new MoveSteps(new NumberLiteral(12));
        Assertions.assertTrue(goTo.equals(goTo2));
        List<Stmt> list2 = new ArrayList<>();
        list2.add(goTo2);

        RepeatTimesStmt repeat2 = new RepeatTimesStmt(num2, new StmtList(new ListOfStmt(list2)));
        Assertions.assertTrue(repeat.equals(repeat2));
    }
}
