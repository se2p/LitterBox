package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Current;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.MonitorMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class GoboscriptTimeTest implements JsonTest {

    @Test
    public void testCurrentTimeSyntax() {
        NoBlockMetadata meta = new NoBlockMetadata();
        List<Stmt> stmts = new ArrayList<>();

        // Add check for each time component
        for (TimeComp.TimeCompType type : TimeComp.TimeCompType.values()) {
            Current current = new Current(new TimeComp(type.getLabel()), meta);
            
            // Wrap in a statement: switch costume to current(...)
            Stmt stmt = new SwitchCostumeTo(
                 new WithExpr(current, meta),
                 meta
            );
            stmts.add(stmt);
        }
        
        Script script = new Script(new GreenFlag(meta), new StmtList(stmts));
        script.getEvent().setParentNode(script);
        List<Script> scriptList = new ArrayList<>();
        scriptList.add(script);

        ActorMetadata actorMeta = new ActorMetadata(
            new CommentMetadataList(new ArrayList<>()),
            0,
            new ImageMetadataList(new ArrayList<>()),
            new SoundMetadataList(new ArrayList<>())
        );

        ActorDefinition stage = new ActorDefinition(
            ActorType.getStage(),
            new StrId("Stage"),
            new DeclarationStmtList(new ArrayList<>()),
            new SetStmtList(new ArrayList<>()),
            new ProcedureDefinitionList(new ArrayList<>()),
            new ScriptList(scriptList),
            actorMeta
        );
        
        List<ActorDefinition> actors = new ArrayList<>();
        actors.add(stage);
        
         ProgramMetadata programMetadata = new ProgramMetadata(
            new MonitorMetadataList(new ArrayList<>()),
            new ExtensionMetadata(new ArrayList<>()),
            new MetaMetadata("3.0.0", "0.0.0", "agent")
        );
        
        Program program = new Program(new StrId("Program"), new ActorDefinitionList(actors), null, null, programMetadata);
        
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.setProgram(program);
        visitor.visit(stage);
        
        String result = visitor.getOutput();
        System.out.println("Result: " + result);

        assertThat(result).contains("current_year()");
        assertThat(result).contains("current_month()");
        assertThat(result).contains("current_date()");
        assertThat(result).contains("current_dayofweek()");
        assertThat(result).contains("current_hour()");
        assertThat(result).contains("current_minute()");
        assertThat(result).contains("current_second()");
    }
}
