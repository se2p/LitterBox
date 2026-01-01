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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ExpressionAsTouchingOrColor;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExpressionAsTouchingOrColorInjectorTest implements JsonTest {

    @Test
    public void testExpressionAsTouchingOrColorInjector() throws IOException, ParsingException {
        // Condition with touching
        String script = """
                when green flag clicked
                if <color [#000000] is touching [#ffffff]?> then
                    say [Hello]
                end
                """;

        Program program = parseScratchBlocks(script);

        ExpressionAsTouchingOrColorInjector injector = new ExpressionAsTouchingOrColorInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should inject bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verification: Check detector
        ExpressionAsTouchingOrColor detector = new ExpressionAsTouchingOrColor();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Expression As Touching Or Color issue");
    }

    @Test
    public void testColorLiteralReplacedWithExpression() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                if <color [#ff0000] is touching [#00ff00]?> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);

        // Verify original has ColorTouchingColor with ColorLiteral
        AtomicBoolean originalHasColorLiteral = new AtomicBoolean(false);
        program.accept(new ScratchVisitor() {
            @Override
            public void visit(ColorTouchingColor node) {
                if (node.getOperand1() instanceof ColorLiteral) {
                    originalHasColorLiteral.set(true);
                }
            }
        });
        Assertions.assertTrue(originalHasColorLiteral.get(), "Original should have ColorLiteral in ColorTouchingColor");

        ExpressionAsTouchingOrColorInjector injector = new ExpressionAsTouchingOrColorInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify AST: ColorTouchingColor should no longer have ColorLiteral as operand1
        AtomicBoolean mutatedHasColorLiteralOperand1 = new AtomicBoolean(false);
        mutated.accept(new ScratchVisitor() {
            @Override
            public void visit(ColorTouchingColor node) {
                if (node.getOperand1() instanceof ColorLiteral) {
                    mutatedHasColorLiteralOperand1.set(true);
                }
            }
        });
        Assertions.assertFalse(mutatedHasColorLiteralOperand1.get(),
                "ColorLiteral should be replaced with expression (not a ColorLiteral anymore)");
    }

    @Test
    public void testNoColorNoInjection() throws IOException, ParsingException {
        // Script without color touching blocks - should still inject for Touching
        String script = """
                when green flag clicked
                move (10) steps
                say [hello]
                """;

        Program program = parseScratchBlocks(script);

        ExpressionAsTouchingOrColorInjector injector = new ExpressionAsTouchingOrColorInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Note: The injector always returns a cloned program even if no changes were made
        // Bug will not be detected if there's nothing to inject
        ExpressionAsTouchingOrColor detector = new ExpressionAsTouchingOrColor();
        for (InjectionResult result : injected) {
            Set<Issue> issues = detector.check(result.mutatedProgram());
            // If no color/touching blocks, detector should find nothing
            Assertions.assertTrue(issues.isEmpty(), "Should not detect bug when no color/touching blocks exist");
        }
    }
}

