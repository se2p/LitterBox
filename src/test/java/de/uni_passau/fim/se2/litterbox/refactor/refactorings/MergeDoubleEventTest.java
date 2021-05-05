package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MergeDoubleEventTest {

    @Test
    public void applyTest() {
        File testFile = new File("src/test/testprojects/testdoublestmts.sb3");
        Program program = null;
        try {
            program = new Scratch3Parser().parseFile(testFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(program);

        ActorDefinition actorDefinition = program.getActorDefinitionList().getDefinitions().get(1);
        List<Script> scriptList = actorDefinition.getScripts().getScriptList();
        Event event1 = null;
        Event event2 = null;
        for (Script script : scriptList) {
            if (event1 == null) {
                event1 = script.getEvent();
            } else {
                event2 = script.getEvent();
            }
        }
        assertNotNull(event1);
        assertNotNull(event2);
        assertEquals(event1, event2);

        MergeDoubleEvent refactoring = new MergeDoubleEvent(event2, event1);
        Program refactoredProgram = refactoring.apply(program);

        ActorDefinition actor = refactoredProgram.getActorDefinitionList().getDefinitions().get(1);
        List<Script> refactoredScripts = actor.getScripts().getScriptList();

        int eventCount = 0;
        for (Script script : refactoredScripts) {
            if (script.getEvent() != null) {
                ++eventCount;
            }
        }
        assertEquals(1, eventCount);
    }

    @Test
    public void getNameTest() {
        GreenFlag greenFlag = mock(GreenFlag.class);
        Script script = mock(Script.class);
        ScriptList scriptList = mock(ScriptList.class);

        when(greenFlag.getParentNode()).thenReturn(script);
        when(script.getParentNode()).thenReturn(scriptList);

        MergeDoubleEvent refactoring = new MergeDoubleEvent(greenFlag, greenFlag);
        assertEquals("merge_double_event", refactoring.getName());
    }

    @Test
    public void toStringTest() {
        GreenFlag greenFlag = mock(GreenFlag.class);
        Script script = mock(Script.class);
        ScriptList scriptList = mock(ScriptList.class);

        when(greenFlag.getParentNode()).thenReturn(script);
        when(script.getParentNode()).thenReturn(scriptList);
        when(greenFlag.getUniqueName()).thenReturn("Event");

        MergeDoubleEvent refactoring = new MergeDoubleEvent(greenFlag, greenFlag);
        assertTrue(refactoring.toString().startsWith("merge_double_event on scripts:"));
    }
}
