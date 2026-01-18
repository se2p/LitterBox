package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static com.google.common.truth.Truth.assertThat;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

public class GoboscriptVariableTest {

    @Test
    public void testGlobalVariableDeclaration() {
        // Create a Stage with a global variable "Score"
        DeclarationIdentAsTypeStmt decl = new DeclarationIdentAsTypeStmt(
                new Variable(new de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId("Score")),
                new NumberType()
        );

        DeclarationStmtList decls = new DeclarationStmtList(new ArrayList<>());
        decls.getDeclarationStmts().add(decl);

        ActorMetadata actorMetadata = new ActorMetadata(
                new CommentMetadataList(new ArrayList<>()),
                0,
                new ImageMetadataList(new ArrayList<>()),
                new SoundMetadataList(new ArrayList<>())
        );

        ActorDefinition stage = new ActorDefinition(
                de.uni_passau.fim.se2.litterbox.ast.model.ActorType.getStage(),
                new de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId("Stage"),
                decls,
                new de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList(new ArrayList<>()),
                new de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList(new ArrayList<>()),
                new de.uni_passau.fim.se2.litterbox.ast.model.ScriptList(new ArrayList<>()),
                actorMetadata
        );

        ProgramMetadata programMetadata = new ProgramMetadata(
                new MonitorMetadataList(new ArrayList<>()),
                new ExtensionMetadata(new ArrayList<>()),
                new MetaMetadata("", "", "")
        );

        Program program = new Program(
                new de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId("Program"),
                new de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList(new ArrayList<>()),
                null,
                null,
                programMetadata
        );
        program.getActorDefinitionList().getDefinitions().add(stage);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.setProgram(program);
        visitor.visit(stage);

        String output = visitor.getOutput();
        assertThat(output).contains("stage Stage");
        // We expect "var Score;"
        assertThat(output).contains("var Score;");
    }
}
