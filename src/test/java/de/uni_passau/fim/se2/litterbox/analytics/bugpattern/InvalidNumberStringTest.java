package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.PlayNoteForBeats;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.FixedNote;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPosXY;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnLeft;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnRight;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Set;

public class InvalidNumberStringTest {

    private static class TestableInvalidNumberString extends InvalidNumberString {
        public Set<Issue> getIssues() {
            return this.issues;
        }
        
        public void setCurrentScript(Script script) {
            this.currentScript = script;
        }
    }

    private Script createDummyScript() {
        return new Script(new GreenFlag(new NoBlockMetadata()), new StmtList(new ArrayList<>()));
    }

    @Test
    void testInvalidNumberStringStructure() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        StringLiteral stringLiteral = new StringLiteral("1.2.3");
        AsNumber asNumber = new AsNumber(stringLiteral);
        finder.visit(asNumber);
        
        Assertions.assertEquals(1, finder.getIssues().size());
    }

    @Test
    void testScientificNotationStructure() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        StringLiteral stringLiteral = new StringLiteral("1e5");
        AsNumber asNumber = new AsNumber(stringLiteral);
        finder.visit(asNumber);
        
        Assertions.assertEquals(1, finder.getIssues().size());
    }

    @Test
    void testValidNumberString() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        StringLiteral stringLiteral = new StringLiteral("1.23");
        AsNumber asNumber = new AsNumber(stringLiteral);
        finder.visit(asNumber);
        
        Assertions.assertEquals(0, finder.getIssues().size());
    }

    @Test
    void testSayBlock() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        StringLiteral stringLiteral = new StringLiteral("1.2.3");
        Say say = new Say(stringLiteral, new NoBlockMetadata());
        
        say.accept(finder);
        
        Assertions.assertEquals(0, finder.getIssues().size());
    }

    @Test
    void testSayForSecsBlock() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        StringLiteral text = new StringLiteral("1.2.3");
        StringLiteral secsString = new StringLiteral("1.2.3");
        AsNumber secs = new AsNumber(secsString);
        
        SayForSecs sayForSecs = new SayForSecs(text, secs, new NoBlockMetadata());
            
        sayForSecs.accept(finder);
        
        Assertions.assertEquals(1, finder.getIssues().size());
    }

    @Test
    void testGlideBlock() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        AsNumber secs = new AsNumber(new StringLiteral("1.2.3"));
        AsNumber x = new AsNumber(new StringLiteral("1e5"));
        AsNumber y = new AsNumber(new StringLiteral("1.2.3"));
        
        GlideSecsToXY glide = new GlideSecsToXY(secs, x, y, new NoBlockMetadata());
            
        glide.accept(finder);
        
        Assertions.assertEquals(3, finder.getIssues().size());
    }

    @Test
    void testSetVariableBlock() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        StringLiteral value = new StringLiteral("1.2.3");
        SetVariableTo setVar = new SetVariableTo(new StrId("var"), value, new NoBlockMetadata());
        
        setVar.accept(finder);
        
        Assertions.assertEquals(0, finder.getIssues().size());
    }
    
    @Test
    void testPlayNoteForBeats() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        AsNumber beats = new AsNumber(new StringLiteral("1.2.3"));
        PlayNoteForBeats playNote = new PlayNoteForBeats(new FixedNote(60, new NoBlockMetadata()), beats, new NoBlockMetadata());
        
        playNote.accept(finder);
        
        Assertions.assertEquals(1, finder.getIssues().size());
    }

    @Test
    void testCoordinates() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());

        AsNumber x = new AsNumber(new StringLiteral("1.2.3"));
        AsNumber y = new AsNumber(new StringLiteral("1.2.3"));
        GoToPosXY goTo = new GoToPosXY(x, y, new NoBlockMetadata());
        goTo.accept(finder);
        Assertions.assertEquals(2, finder.getIssues().size());

        finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        AsNumber setX = new AsNumber(new StringLiteral("1.2.3"));
        SetXTo setXTo = new SetXTo(setX, new NoBlockMetadata());
        setXTo.accept(finder);
        Assertions.assertEquals(1, finder.getIssues().size());
    }

    @Test
    void testAngles() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());

        AsNumber degreesLeft = new AsNumber(new StringLiteral("1.2.3"));
        TurnLeft turnLeft = new TurnLeft(degreesLeft, new NoBlockMetadata());
        turnLeft.accept(finder);
        Assertions.assertEquals(1, finder.getIssues().size());

        finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        AsNumber degreesRight = new AsNumber(new StringLiteral("1.2.3"));
        TurnRight turnRight = new TurnRight(degreesRight, new NoBlockMetadata());
        turnRight.accept(finder);
        Assertions.assertEquals(1, finder.getIssues().size());

        finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        AsNumber direction = new AsNumber(new StringLiteral("1.2.3"));
        PointInDirection point = new PointInDirection(direction, new NoBlockMetadata());
        point.accept(finder);
        Assertions.assertEquals(1, finder.getIssues().size());
    }

    @Test
    void testOtherNumericBlocks() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());

        AsNumber steps = new AsNumber(new StringLiteral("1.2.3"));
        MoveSteps move = new MoveSteps(steps, new NoBlockMetadata());
        move.accept(finder);
        Assertions.assertEquals(1, finder.getIssues().size());

        finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        AsNumber seconds = new AsNumber(new StringLiteral("1.2.3"));
        WaitSeconds wait = new WaitSeconds(seconds, new NoBlockMetadata());
        wait.accept(finder);
        Assertions.assertEquals(1, finder.getIssues().size());

        finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        AsNumber times = new AsNumber(new StringLiteral("1.2.3"));
        RepeatTimesStmt repeat = new RepeatTimesStmt(times, new StmtList(new ArrayList<>()), new NoBlockMetadata());
        repeat.accept(finder);
        Assertions.assertEquals(1, finder.getIssues().size());
    }
}
