package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SeparateScriptTest {

    @Test
    public void applyTest() {
        File testFile = new File("src/test/testprojects/testsemantics.sb3");
        Program program = null;
        try {
            program = new Scratch3Parser().parseFile(testFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(program);

        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        ScriptList scriptList = actor.getScripts();
        Script script = scriptList.getScriptList().get(0);
        SeparateScriptBySemantics refactoring = new SeparateScriptBySemantics(script);
        Program refactored = refactoring.apply(program);

        ActorDefinition refactoredActor = refactored.getActorDefinitionList().getDefinitions().get(1);
        List<Script> refactoredScripts = refactoredActor.getScripts().getScriptList();
        assertEquals(5, refactoredScripts.size());
    }

    @Test
    public void getNameTest() {
        Script script = mock(Script.class);
        ScriptList scriptList = mock(ScriptList.class);
        GreenFlag greenFlag = mock(GreenFlag.class);

        when(script.getParentNode()).thenReturn(scriptList);
        when(script.getEvent()).thenReturn(greenFlag);

        SeparateScriptBySemantics refactoring = new SeparateScriptBySemantics(script);
        assertEquals("separate_script_by_semantics", refactoring.getName());
    }

    @Test
    public void toStringTest() {
        Script script = mock(Script.class);
        ScriptList scriptList = mock(ScriptList.class);
        GreenFlag greenFlag = mock(GreenFlag.class);

        when(script.getParentNode()).thenReturn(scriptList);
        when(script.getEvent()).thenReturn(greenFlag);
        when(script.getUniqueName()).thenReturn("Script");

        SeparateScriptBySemantics refactoring = new SeparateScriptBySemantics(script);
        assertTrue(refactoring.toString().startsWith("separate_script_by_semantics on script:"));
    }
}
