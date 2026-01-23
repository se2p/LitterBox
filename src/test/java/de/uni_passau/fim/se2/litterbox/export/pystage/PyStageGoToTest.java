package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.PickRandom;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.MonitorMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class PyStageGoToTest {

    @Test
    public void testGoToWithRandomNumberIncludesHelper() {
        // Construct "go to (pick random 1 to 10)"
        PickRandom pickRandom = new PickRandom(new NumberLiteral(1), new NumberLiteral(10), new NoBlockMetadata());
        FromExpression fromExpr = new FromExpression(new AsString(pickRandom), new NoBlockMetadata());
        GoToPos goTo = new GoToPos(fromExpr, new NoBlockMetadata());

        StmtList stmts = new StmtList(goTo);
        GreenFlag event = new GreenFlag(new NoBlockMetadata());
        Script script = new Script(event, stmts);
        event.setParentNode(script);
        ScriptList scripts = new ScriptList(List.of(script));

        ActorMetadata metadata = new ActorMetadata(
                new CommentMetadataList(Collections.emptyList()),
                0,
                new ImageMetadataList(Collections.emptyList()),
                new SoundMetadataList(Collections.emptyList())
        );

        ActorDefinition actor = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("Sprite1"),
                new DeclarationStmtList(Collections.emptyList()),
                new SetStmtList(Collections.emptyList()),
                new ProcedureDefinitionList(Collections.emptyList()),
                scripts,
                metadata
        );

        // Program construction to ensure helpers are emitted
        MonitorMetadataList monitorList = new MonitorMetadataList(Collections.emptyList());
        ExtensionMetadata extensionMetadata = new ExtensionMetadata(Collections.emptyList());
        MetaMetadata metaMetadata = new MetaMetadata("3.0.0", "0.2.0", "Mozilla/5.0");
        ProgramMetadata progMetadata = new ProgramMetadata(monitorList, extensionMetadata, metaMetadata);
        ActorDefinitionList actorList = new ActorDefinitionList(List.of(actor));
        Program program = new Program(new StrId("Program"), actorList, null, null, progMetadata);

        String result = PyStageVisitor.of(program);

        // Check for helper definition
        assertThat(result).contains("def _go_to_helper(source_sprite, target):");
        
        // Check usage of helper
        // It should call _go_to_helper(self, ...) instead of self.go_to_sprite(...) directly
        // The expected output will use _to_num and _pick_random helpers as well
        // We look for partial match to be robust to formatting
        assertThat(result).contains("_go_to_helper");
        assertThat(result).contains("_pick_random(1, 10)");
    }
}
