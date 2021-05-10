package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExtractScriptTest implements JsonTest {

    private Program program;
    private Script script;
    private Refactoring refactoring;

    @BeforeEach
    public void setUp() throws ParsingException, IOException {
        program = getAST("src/test/testprojects/testdummyrefactorings.sb3");
        ActorDefinition actorDefinition = program.getActorDefinitionList().getDefinitions().get(1);
        script = actorDefinition.getScripts().getScriptList().get(0);
        refactoring = new ExtractScript(script);
    }

    @Test
    public void applyTest() {
        ExtractScript refactoring = new ExtractScript(script);
        Program refactored = refactoring.apply(program);

        ActorDefinition refactoredActor = refactored.getActorDefinitionList().getDefinitions().get(1);
        ActorDefinition refactoredStage = refactored.getActorDefinitionList().getDefinitions().get(0);

        assertFalse(refactoredActor.getScripts().getScriptList().contains(script));
        assertTrue(refactoredStage.getScripts().getScriptList().contains(script));
    }

    @Test
    public void getNameTest() {
        Script script = mock(Script.class);
        ScriptList scriptList = mock(ScriptList.class);

        when(script.getParentNode()).thenReturn(scriptList);

        ExtractScript refactoring = new ExtractScript(script);
        assertEquals("extract_script", refactoring.getName());
    }

    @Test
    public void toStringTest() {
        Script script = mock(Script.class);
        ScriptList scriptList = mock(ScriptList.class);

        when(script.getParentNode()).thenReturn(scriptList);
        when(script.getUniqueName()).thenReturn("Script");

        ExtractScript refactoring = new ExtractScript(script);
        assertTrue(refactoring.toString().startsWith("extract_script on script:"));
    }

    @Test
    void testEqualOfRefactorings() {
        Script nonEqual = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(new ArrayList<>()));
        Refactoring equalRefactoring = new ExtractScript(script);
        Refactoring nonEqualRefactoring = new ExtractScript(nonEqual);

        assertEquals(refactoring, refactoring);
        assertEquals(refactoring, equalRefactoring);
        assertNotEquals(refactoring, nonEqualRefactoring);
    }

    @Test
    void testHashCodeOfRefactorings() {
        Script nonEqual = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(new ArrayList<>()));
        Refactoring equalRefactoring = new ExtractScript(script);
        Refactoring nonEqualRefactoring = new ExtractScript(nonEqual);

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
