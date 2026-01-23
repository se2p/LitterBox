package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DaysSince2000;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static com.google.common.truth.Truth.assertThat;

public class PyStageDaysSince2000Test {

    @Test
    public void testDaysSince2000Export() throws IOException, InterruptedException {
        // manually construct AST with DaysSince2000 block
        ActorDefinition actor = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("Sprite1"),
                new DeclarationStmtList(Collections.emptyList()),
                new SetStmtList(Collections.emptyList()),
                new ProcedureDefinitionList(Collections.emptyList()),
                new ScriptList(Collections.emptyList()),
                new de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata(
                        new de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList(Collections.emptyList()),
                        0,
                        new de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList(Collections.emptyList()),
                        new de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList(Collections.emptyList())
                )
        );

        String result = PyStageVisitor.of(new DaysSince2000(new NoBlockMetadata()));

        // Verify the output uses the correct pystage method name
        assertThat(result).contains("self.days_since()");
    }
}
