package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExtractScriptTest {

    @Test
    public void applyTest() {
        File testFile = new File("src/test/testprojects/testdummyrefactorings.sb3");
        Program program = null;
        try {
            program = new Scratch3Parser().parseFile(testFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(program);

        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        Script script = actor.getScripts().getScriptList().get(0);
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
        assertEquals("extract_script(Script)", refactoring.toString());
    }
}
