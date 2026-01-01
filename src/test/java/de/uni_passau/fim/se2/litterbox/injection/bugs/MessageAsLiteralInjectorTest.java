/*
 * Copyright (C) 2019-2026 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageAsLiteral;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MessageAsLiteralInjectorTest implements JsonTest {

    @Test
    public void testMessageAsLiteralInjector() throws IOException, ParsingException {
        // Program with a broadcast, receiver, and a Say block with different text
        String script = """
                when green flag clicked
                broadcast (myMsg v)
                say [Hello]
                when I receive [myMsg v]
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        // Verify original has broadcast and Say with "Hello"
        assertTrue(hasBroadcast(program), "Original should have Broadcast statement");

        List<String> originalSayTexts = collectSayTexts(program);
        assertTrue(originalSayTexts.contains("Hello"),
                "Original should have Say with 'Hello', found: " + originalSayTexts);

        // Inject
        MessageAsLiteralInjector injector = new MessageAsLiteralInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        Program mutated = injected.getFirst().mutatedProgram();

        // Debug: collect all Say texts after mutation
        List<String> mutatedSayTexts = collectSayTexts(mutated);

        // Verify:
        // 1. Broadcast is removed
        // 2. Say now contains the message name instead of original text
        assertFalse(hasBroadcast(mutated), "Broadcast should be removed");
        assertTrue(mutatedSayTexts.contains("myMsg"),
                "Say should now contain 'myMsg', found: " + mutatedSayTexts);
        assertFalse(mutatedSayTexts.contains("Hello"),
                "Say should no longer contain 'Hello', found: " + mutatedSayTexts);

        // Check detector finds the bug
        MessageAsLiteral detector = new MessageAsLiteral();
        Set<Issue> issues = detector.check(mutated);
        assertFalse(issues.isEmpty(), "Detector should find the injected bug");
    }

    private boolean hasBroadcast(Program program) {
        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            for (Script script : actor.getScripts().getScriptList()) {
                for (Stmt stmt : script.getStmtList().getStmts()) {
                    if (stmt instanceof Broadcast) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<String> collectSayTexts(Program program) {
        List<String> texts = new ArrayList<>();
        program.accept(new ScratchVisitor() {
            @Override
            public void visit(Say node) {
                if (node.getString() instanceof StringLiteral lit) {
                    texts.add(lit.getText());
                }
                visitChildren(node);
            }
        });
        return texts;
    }
}
