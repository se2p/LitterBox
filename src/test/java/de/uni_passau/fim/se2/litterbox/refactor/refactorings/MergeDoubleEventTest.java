package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MergeDoubleEventTest implements JsonTest {

    private Program program;
    private Event event0;
    private Event event1;
    private Event event2;
    private Refactoring refactoring;

    @BeforeEach
    public void setUp() throws ParsingException, IOException {
        program = getAST("src/test/testprojects/merge-double-event.sb3");
        event0 = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getEvent();
        event1 = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(1).getEvent();
        event2 = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(2).getEvent();
        refactoring = new MergeDoubleEvent(event1, event2);
    }

    @Test
    public void testASTStructure() throws ParsingException, IOException {
        Program refactored = refactoring.apply(program);
        CloneVisitor visitor = new CloneVisitor();
        Program clone = visitor.apply(refactored);
        assertEquals(clone, refactored);
    }

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
        assertEquals("merge_double_event", refactoring.getName());
    }

    @Test
    public void toStringTest() {
        assertThat(refactoring.toString()).isEqualTo(
                "merge_double_event\n"
                        + "Replaced scripts:\n"
                        + "\n"
                        + "when green flag clicked\n"
                        + "move (5) steps\n"
                        + "\n"
                        + "when green flag clicked\n"
                        + "move (2) steps\n"
                        + "\n"
                        + "Replacement:\n"
                        + "\n"
                        + "when green flag clicked\n"
                        + "move (5) steps\n"
                        + "move (2) steps\n");
    }
}
