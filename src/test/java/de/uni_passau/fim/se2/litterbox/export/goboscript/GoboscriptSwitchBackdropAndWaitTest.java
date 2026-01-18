package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class GoboscriptSwitchBackdropAndWaitTest {

    @Test
    public void testSwitchBackdropAndWaitReplacedBySwitchBackdrop() {
        // Setup metadata
        List<ImageMetadata> costumes = new ArrayList<>();
        costumes.add(new ImageMetadata("assetId", "Backdrop1", "md5ext", "svg", 1.0, 0, 0));
        
        ImageMetadataList costumeList = new ImageMetadataList(costumes);
        ActorMetadata actorMeta = new ActorMetadata(
            new CommentMetadataList(new ArrayList<>()), 
            0, 
            costumeList, 
            new SoundMetadataList(new ArrayList<>())
        );

        // Define script: when flag clicked, switch backdrop to "Backdrop1" and wait
        NoBlockMetadata meta = new NoBlockMetadata();
        Script script = new Script(
            new GreenFlag(meta),
            new StmtList(List.of(
                new SwitchBackdropAndWait(
                    new WithExpr(new StringLiteral("Backdrop1"), meta),
                    meta
                )
            ))
        );
        // Manually link parent because emitBlock relies on it
        script.getEvent().setParentNode(script);

        ActorDefinition stage = new ActorDefinition(
            ActorType.getStage(),
            new StrId("Stage"),
            new DeclarationStmtList(new ArrayList<>()),
            new SetStmtList(new ArrayList<>()),
            new ProcedureDefinitionList(new ArrayList<>()),
            new ScriptList(List.of(script)),
            actorMeta
        );

        // We need to wrap the stage in a Program so that isBackdropAvailable can find it
        de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList actorList = 
            new de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList(List.of(stage));
            
        // Create valid metadata objects (cannot be null because AbstractNode constructor checks them)
        de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.MonitorMetadataList monitorMeta = 
            new de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.MonitorMetadataList(new ArrayList<>());
            
        de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata extMeta = 
            new de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata(new ArrayList<>());
            
        de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata metaMeta = 
            new de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata("1.0", "1.0", "test");
            
        de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata progMeta = 
            new de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata(monitorMeta, extMeta, metaMeta);
            
        de.uni_passau.fim.se2.litterbox.ast.model.Program program = 
            new de.uni_passau.fim.se2.litterbox.ast.model.Program(
                new StrId("Program"), 
                actorList, 
                new de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable(), 
                new de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping(), 
                progMeta
            );

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.setProgram(program);
        
        // We don't need to set available assets for this test as we want to test the instruction generation
        // But to avoid commenting out due to missing asset check, let's behave as if we didn't filter assets
        // (visitor.availableAssets is null by default, which means "all assets available")
        
        visitor.visit(stage);
        
        String output = visitor.getOutput();
        System.out.println(output);

        // We expect switch_backdrop, NOT switch_backdrop_and_wait
        assertThat(output).contains("switch_backdrop \"Backdrop1\";");
        assertThat(output).doesNotContain("switch_backdrop_and_wait");
    }
}
