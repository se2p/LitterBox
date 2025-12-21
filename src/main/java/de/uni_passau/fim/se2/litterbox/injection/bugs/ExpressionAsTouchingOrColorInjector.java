/*
 * Copyright (C) 2019-2024 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ExpressionAsTouchingOrColor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.AsTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Injects ExpressionAsTouchingOrColor bug by replacing a Touching or ColorTouchingColor
 * expression's operand with an expression (variable) instead of a literal touchable/color.
 * <p>
 * Note: This injector uses a custom CloneVisitor approach because the Touching and
 * ColorTouchingColor nodes have immutable fields and cannot be modified in-place.
 */
public class ExpressionAsTouchingOrColorInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new ExpressionAsTouchingOrColor();
    }

    @Override
    public List<Program> inject(Program program) {
        CustomMappingCloneVisitor visitor = new CustomMappingCloneVisitor();
        Program cloned = (Program) program.accept(visitor);
        return Collections.singletonList(cloned);
    }

    private static class CustomMappingCloneVisitor extends CloneVisitor {
        @Override
        public ASTNode visit(Touching node) {
            return new Touching(
                    new AsTouchable(
                            new Variable(
                                    new StrId(new StringLiteral("var_" + UUID.randomUUID())),
                                    new NoBlockMetadata()
                            )
                    ),
                    node.getMetadata()
            );
        }

        @Override
        public ASTNode visit(ColorTouchingColor node) {
            return new ColorTouchingColor(
                    new FromNumber(
                            new AsNumber(
                                    new Variable(
                                            new StrId(new StringLiteral("var_" + UUID.randomUUID())),
                                            new NoBlockMetadata()
                                    )
                            )
                    ),
                    apply(node.getOperand2()), // Clone the second color
                    node.getMetadata()
            );
        }
    }
}
