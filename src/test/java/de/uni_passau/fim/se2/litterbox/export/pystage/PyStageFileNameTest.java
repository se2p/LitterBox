package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class PyStageFileNameTest {

    @Test
    public void testSanitizedAssetPaths() {
        // Create metadata with assets having special characters
        ImageMetadata costume = new ImageMetadata("abc1234567890", "Costume ?! Name", "abc1234567890.png", "png", 1.0, 10.0, 10.0);
        SoundMetadata sound = new SoundMetadata("def1234567890", "Sound ?! Name", "def1234567890.wav", "wav", 44100, 100);

        ActorMetadata metadata = new ActorMetadata(
                new CommentMetadataList(Collections.emptyList()),
                0,
                new ImageMetadataList(List.of(costume)),
                new SoundMetadataList(List.of(sound))
        );

        ActorDefinition actor = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("Sprite1"),
                new DeclarationStmtList(Collections.emptyList()),
                new SetStmtList(Collections.emptyList()),
                new ProcedureDefinitionList(Collections.emptyList()),
                new ScriptList(Collections.emptyList()),
                metadata
        );

        String result = PyStageVisitor.of(actor);

        // Expect standard sanitization: special chars replaced by underscores
        // "Costume ?! Name" -> "Costume __ Name"
        
        // Check costume path
        assertThat(result).contains("images/Costume _! Name.png");
        
        // Check sound path
        assertThat(result).contains("sounds/Sound _! Name.wav");
    }
}
