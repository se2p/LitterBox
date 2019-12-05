package utils;

import org.junit.jupiter.api.Test;
import scratch.ast.model.literals.NumberLiteral;
import scratch.ast.model.statement.control.RepeatTimesStmt;
import scratch.ast.model.statement.spritemotion.MoveSteps;

public class EqualsTest {

    @Test
    public void testScriptEquals(){
        NumberLiteral num = new NumberLiteral(2);
        MoveSteps goTo = new MoveSteps(new NumberLiteral(12));

        //RepeatTimesStmt repeat = new RepeatTimesStmt(num, );
    }
}
