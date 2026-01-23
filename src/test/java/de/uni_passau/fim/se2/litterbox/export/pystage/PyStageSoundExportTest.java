package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.MonitorMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

class PyStageSoundExportTest {

    @Test
    void testSoundExportUsesFilePath() {
        // Arrange
        // Sound Setup
        SoundMetadata sound = new SoundMetadata("assetId", "pop", "83a9787d4cb6f3b7632b4ddfebf74367.wav", "wav", 123, 123);
        List<SoundMetadata> sounds = new ArrayList<>();
        sounds.add(sound);
        SoundMetadataList soundList = new SoundMetadataList(sounds);
        
        // Metadata Setup
        ActorMetadata actorMetadata = new ActorMetadata(new CommentMetadataList(new ArrayList<>()), 0, new ImageMetadataList(new ArrayList<>()), soundList);

        // Actor Setup
        ActorDefinition sprite = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("MySprite"),
                new DeclarationStmtList(new ArrayList<>()),
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(new ArrayList<>()),
                new ScriptList(new ArrayList<>()),
                actorMetadata
        );
        
        // Program Setup
        List<ActorDefinition> actors = new ArrayList<>();
        actors.add(sprite);
        ActorDefinitionList actorList = new ActorDefinitionList(actors);
        
        ProgramMetadata programMetadata = new ProgramMetadata(
            new MonitorMetadataList(new ArrayList<>()),
            new ExtensionMetadata(new ArrayList<>()),
            new MetaMetadata("3.0.0", "0.2.0", "Mozilla")
        );
        
        Program program = new Program(new StrId("TestProgram"), actorList, null, null, programMetadata);

        // Act
        String code = PyStageVisitor.of(program);

        // Assert
        // We expect: MySprite.add_sound("sounds/pop.wav")
        // Currently (Broken): MySprite.add_sound("pop")
        assertThat(code).contains("MySprite.add_sound(\"sounds/pop.wav\")");
    }
}
