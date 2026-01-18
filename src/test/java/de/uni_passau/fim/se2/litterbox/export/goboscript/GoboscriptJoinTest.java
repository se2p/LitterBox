package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Join;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static com.google.common.truth.Truth.assertThat;

public class GoboscriptJoinTest {

    @Test
    public void testJoinReproduction() {
        // Construct: say(join("Hello ", "World"))
        NoBlockMetadata meta = new NoBlockMetadata();
        List<Stmt> stmtList = new ArrayList<>();
        
        stmtList.add(new Say(
            new Join(
                new StringLiteral("Hello "),
                new StringLiteral("World"),
                meta
            ),
            meta
        ));

        StmtList stmts = new StmtList(stmtList);
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(stmts);
        String result = visitor.getOutput();

        // EXPECTATION: Should be ("Hello " & "World")
        // CURRENT INCORRECT BEHAVIOR: join "Hello ", "World"
        assertThat(result).contains("(\"Hello \" & \"World\")");
    }

    @Test
    public void testNestedJoinReproduction() {
        // Construct: say(join(join("A", "B"), "C"))
        NoBlockMetadata meta = new NoBlockMetadata();
        List<Stmt> stmtList = new ArrayList<>();
        
        stmtList.add(new Say(
            new Join(
                new Join(
                   new StringLiteral("A"),
                   new StringLiteral("B"),
                   meta
                ),
                new StringLiteral("C"),
                meta
            ),
            meta
        ));

        StmtList stmts = new StmtList(stmtList);
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(stmts);
        String result = visitor.getOutput();

        // EXPECTATION: (("A" & "B") & "C")
        assertThat(result).contains("((\"A\" & \"B\") & \"C\")");
    }
}
