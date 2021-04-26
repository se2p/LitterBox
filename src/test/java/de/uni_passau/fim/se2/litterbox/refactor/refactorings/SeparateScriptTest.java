package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        refactoring.apply(program);

        assertEquals(5, scriptList.getScriptList().size());
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
        assertEquals("separate_script_by_semantics(Script)", refactoring.toString());
    }
}
