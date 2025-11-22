package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
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
        
        // Test with "1.2.3"
        StringLiteral stringLiteral = new StringLiteral("1.2.3");
        AsNumber asNumber = new AsNumber(stringLiteral);
        finder.visit(asNumber);
        
        Assertions.assertEquals(1, finder.getIssues().size());
    }

    @Test
    void testScientificNotationStructure() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        // Test with "1e5"
        StringLiteral stringLiteral = new StringLiteral("1e5");
        AsNumber asNumber = new AsNumber(stringLiteral);
        finder.visit(asNumber);
        
        Assertions.assertEquals(1, finder.getIssues().size());
    }

    @Test
    void testValidNumberString() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        // Test with "1.23"
        StringLiteral stringLiteral = new StringLiteral("1.23");
        AsNumber asNumber = new AsNumber(stringLiteral);
        finder.visit(asNumber);
        
        Assertions.assertEquals(0, finder.getIssues().size());
    }

    @Test
    void testSayBlock() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        // Say "1.2.3" -> Should NOT trigger
        StringLiteral stringLiteral = new StringLiteral("1.2.3");
        // Say takes StringExpr, so no AsNumber wrapper
        de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say say = 
            new de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say(stringLiteral, new NoBlockMetadata());
        
        // We need to visit the Say node, which will visit its children
        // But InvalidNumberString only visits AsNumber. 
        // If we visit Say, it visits StringLiteral. InvalidNumberString doesn't visit StringLiteral directly.
        // So we just need to ensure that if we visit the children of Say, no issue is reported.
        // Since there is no AsNumber, no issue should be reported.
        
        // Simulate visiting children
        stringLiteral.accept(finder);
        
        Assertions.assertEquals(0, finder.getIssues().size());
    }

    @Test
    void testSayForSecsBlock() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        // Say "1.2.3" for "1.2.3" secs
        // Text "1.2.3" -> Should NOT trigger
        // Secs "1.2.3" -> Should trigger (wrapped in AsNumber)
        
        StringLiteral text = new StringLiteral("1.2.3");
        StringLiteral secsString = new StringLiteral("1.2.3");
        AsNumber secs = new AsNumber(secsString);
        
        de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs sayForSecs = 
            new de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs(text, secs, new NoBlockMetadata());
            
        // Visit text (StringLiteral) -> No issue
        text.accept(finder);
        Assertions.assertEquals(0, finder.getIssues().size());
        
        // Visit secs (AsNumber) -> Issue
        finder.visit(secs);
        Assertions.assertEquals(1, finder.getIssues().size());
    }

    @Test
    void testGlideBlock() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        // Glide "1.2.3" secs to x: "1e5" y: "1.2.3"
        // All should trigger
        
        AsNumber secs = new AsNumber(new StringLiteral("1.2.3"));
        AsNumber x = new AsNumber(new StringLiteral("1e5"));
        AsNumber y = new AsNumber(new StringLiteral("1.2.3"));
        
        de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY glide = 
            new de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY(secs, x, y, new NoBlockMetadata());
            
        finder.visit(secs);
        finder.visit(x);
        finder.visit(y);
        
        Assertions.assertEquals(3, finder.getIssues().size());
    }

    @Test
    void testSetVariableBlock() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        // Set variable to "1.2.3" -> Should NOT trigger
        // SetVariableTo takes Expression. If it's a string, it's StringLiteral, not AsNumber.
        
        StringLiteral value = new StringLiteral("1.2.3");
        // We need an Identifier, let's mock or create a simple one if possible, or just test the value visiting
        // Since we are manually visiting, we just visit the value.
        
        // If the parser parses `set var to "1.2.3"`, it creates SetVariableTo(id, StringLiteral("1.2.3")).
        // So we visit StringLiteral.
        
        value.accept(finder);
        
        Assertions.assertEquals(0, finder.getIssues().size());
    }
    
    @Test
    void testPlayNoteForBeats() {
        TestableInvalidNumberString finder = new TestableInvalidNumberString();
        finder.setCurrentScript(createDummyScript());
        
        // Play note for "1.2.3" beats -> Should trigger
        
        AsNumber beats = new AsNumber(new StringLiteral("1.2.3"));
        // Note is an enum or similar, but we only care about beats which is NumExpr
        
        finder.visit(beats);
        
        Assertions.assertEquals(1, finder.getIssues().size());
    }
}
