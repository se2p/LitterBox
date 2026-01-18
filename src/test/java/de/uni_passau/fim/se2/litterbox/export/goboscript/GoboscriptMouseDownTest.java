package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsMouseDown;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class GoboscriptMouseDownTest {

    @Test
    public void testMouseDownExport() {
        List<Stmt> stmtList = new ArrayList<>();
        NoBlockMetadata meta = new NoBlockMetadata();
        
        // wait until <mouse down?>
        stmtList.add(new WaitUntil(
            new IsMouseDown(meta),
            meta
        ));

        StmtList stmts = new StmtList(stmtList);
        
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(stmts);
        String result = visitor.getOutput();

        // Expectation: "mouse_down()" with parens.
        assertThat(result).contains("until mouse_down() {}");
    }

    @Test
    public void testOtherReportersExport() {
        List<Stmt> stmtList = new ArrayList<>();
        NoBlockMetadata meta = new NoBlockMetadata();
        
        // Use SetXTo for numeric reporters
        stmtList.add(new de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo(new de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseX(meta), meta));
        stmtList.add(new de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo(new de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseY(meta), meta));
        stmtList.add(new de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo(new de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Loudness(meta), meta));
        
        // Use Say for string reporters
        stmtList.add(new de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say(new de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer(meta), meta));
        stmtList.add(new de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say(new de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Username(meta), meta));

        StmtList stmts = new StmtList(stmtList);
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(stmts);
        String result = visitor.getOutput();
        
        assertThat(result).contains("set_x mouse_x();");
        assertThat(result).contains("set_x mouse_y();");
        assertThat(result).contains("set_x loudness();");
        assertThat(result).contains("say answer();");
        assertThat(result).contains("say username();");
    }
}
