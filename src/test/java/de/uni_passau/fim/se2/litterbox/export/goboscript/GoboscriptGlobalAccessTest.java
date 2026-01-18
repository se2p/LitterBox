package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static com.google.common.truth.Truth.assertThat;

public class GoboscriptGlobalAccessTest {

    @Test
    public void testGlobalVariableAccessFromSprite() {
        // 1. Define Stage with global variable "Score"
        DeclarationIdentAsTypeStmt decl = new DeclarationIdentAsTypeStmt(
                new Variable(new StrId("Score")),
                new NumberType()
        );
        DeclarationStmtList stageDecls = new DeclarationStmtList(new ArrayList<>());
        stageDecls.getDeclarationStmts().add(decl);

        ActorMetadata stageMetadata = new ActorMetadata(new CommentMetadataList(new ArrayList<>()), 0, new ImageMetadataList(new ArrayList<>()), new SoundMetadataList(new ArrayList<>()));
        ActorDefinition stage = new ActorDefinition(
                ActorType.getStage(),
                new StrId("Stage"),
                stageDecls,
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(new ArrayList<>()),
                new ScriptList(new ArrayList<>()),
                stageMetadata
        );

        // 2. Define Sprite that uses "Score" (unqualified) using Parser
        String code = """
                when green flag clicked
                change [Score v] by (1)
                """;
        ScratchBlocksParser parser = new ScratchBlocksParser();
        Script script = parser.parseScript("Sprite1", code);
        
        ScriptList spriteScripts = new ScriptList(new ArrayList<>());
        spriteScripts.getScriptList().add(script);

        ActorMetadata spriteMetadata = new ActorMetadata(new CommentMetadataList(new ArrayList<>()), 0, new ImageMetadataList(new ArrayList<>()), new SoundMetadataList(new ArrayList<>()));
        ActorDefinition sprite = new ActorDefinition(
                ActorType.getSprite(),
                new StrId("Sprite1"),
                new DeclarationStmtList(new ArrayList<>()),
                new SetStmtList(new ArrayList<>()),
                new ProcedureDefinitionList(new ArrayList<>()),
                spriteScripts,
                spriteMetadata
        );

        Program program = new Program(
                new StrId("Program"),
                new ActorDefinitionList(new ArrayList<>()),
                null, 
                null, 
                new ProgramMetadata(new MonitorMetadataList(new ArrayList<>()), new ExtensionMetadata(new ArrayList<>()), new MetaMetadata("", "", ""))
        );
        program.getActorDefinitionList().getDefinitions().add(stage);
        program.getActorDefinitionList().getDefinitions().add(sprite);

        // 3. Visit Sprite
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.setProgram(program);
        visitor.visit(sprite);

        String output = visitor.getOutput();
        assertThat(output).contains("sprite Sprite1");
        // In Goboscript, global variables (defined in stage.gs) are accessed by name directly
        // without a Stage. prefix. The variable is just "Score", not "Stage.Score".
        assertThat(output).contains("Score += 1;");
    }
}
