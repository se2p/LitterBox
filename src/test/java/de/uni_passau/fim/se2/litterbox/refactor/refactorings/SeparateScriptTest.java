package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SeparateScriptTest implements JsonTest {

    private Program program;
    private Script script;
    private Refactoring refactoring;

    @BeforeEach
    public void setUp() throws ParsingException, IOException {
        program = getAST("src/test/fixtures/refactoring/testsemantics.json");
        ActorDefinition actorDefinition = program.getActorDefinitionList().getDefinitions().get(1);
        script = actorDefinition.getScripts().getScriptList().get(0);
        refactoring = new SeparateScriptBySemantics(script);
    }

    @Test
    public void applyTest() {
        SeparateScriptBySemantics refactoring = new SeparateScriptBySemantics(script);
        Program refactored = refactoring.apply(program);

        ActorDefinition refactoredActor = refactored.getActorDefinitionList().getDefinitions().get(1);
        List<Script> refactoredScripts = refactoredActor.getScripts().getScriptList();
        assertEquals(5, refactoredScripts.size());
    }

    @Test
    public void getNameTest() {
        assertEquals("separate_script_by_semantics", refactoring.getName());
    }

    @Test
    void toStringTest() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/testsemantics.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        SeparateScriptBySemantics refactoring = new SeparateScriptBySemantics(script);
        assertThat(refactoring.toString()).startsWith("separate_script_by_semantics on script:");
    }

    @Test
    void testEqualOfRefactorings() {
        Script nonEqual = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(new ArrayList<>()));
        Refactoring equalRefactoring = new SeparateScriptBySemantics(script);
        Refactoring nonEqualRefactoring = new SeparateScriptBySemantics(nonEqual);

        assertEquals(refactoring, refactoring);
        assertEquals(refactoring, equalRefactoring);
        assertNotEquals(refactoring, nonEqualRefactoring);
    }

    @Test
    void testHashCodeOfRefactorings() {
        Script nonEqual = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(new ArrayList<>()));
        Refactoring equalRefactoring = new SeparateScriptBySemantics(script);
        Refactoring nonEqualRefactoring = new SeparateScriptBySemantics(nonEqual);

        assertEquals(refactoring.hashCode(), refactoring.hashCode());
        assertEquals(refactoring.hashCode(), equalRefactoring.hashCode());
        assertNotEquals(refactoring.hashCode(), nonEqualRefactoring.hashCode());
    }

    @Test
    public void testASTStructure() {
        Program refactored = refactoring.apply(program);
        CloneVisitor visitor = new CloneVisitor();
        Program clone = visitor.apply(refactored);
        assertEquals(refactored, clone);
    }
}
