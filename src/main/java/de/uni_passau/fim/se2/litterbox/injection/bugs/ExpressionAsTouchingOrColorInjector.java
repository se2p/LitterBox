/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.AsTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Injects ExpressionAsTouchingOrColor bug by replacing a Touching or ColorTouchingColor
 * expression's operand with an expression (variable) instead of a literal touchable/color.
 */
public class ExpressionAsTouchingOrColorInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new ExpressionAsTouchingOrColor();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        CustomMappingCloneVisitor visitor = new CustomMappingCloneVisitor();
        Program cloned = (Program) program.accept(visitor);
        if (visitor.isModified()) {
            cloned.accept(new ParentVisitor());

            // Generate script strings for CSV output using first modified location
            String originalScript = "";
            String mutatedScript = "";
            String spriteName = "";
            String scriptId = "";

            if (visitor.firstModifiedNode != null) {
                spriteName = findContainingActorName(visitor.firstModifiedNode);
                scriptId = findContainingScriptId(visitor.firstModifiedNode);
                originalScript = safeGetScratchBlocks(findContextNode(visitor.firstModifiedNode));
            }
            if (visitor.firstModifiedNodeReplacement != null) {
                mutatedScript = safeGetScratchBlocks(findContextNode(visitor.firstModifiedNodeReplacement));
            }

            return Collections.singletonList(
                    new InjectionResult(cloned, spriteName, scriptId, originalScript, mutatedScript));
        }
        return Collections.emptyList();
    }

    @Override
    protected ASTNode findContextNode(ASTNode node) {
        // Force ActorDefinition context to show full scope of changes and avoid empty scripts
        ActorDefinition actor = findContainingActor(node);
        if (actor != null) {
            return actor;
        }
        return super.findContextNode(node);
    }

    private static class CustomMappingCloneVisitor extends CloneVisitor {
        private boolean modified = false;
        ASTNode firstModifiedNode = null;
        ASTNode firstModifiedNodeReplacement = null;

        public boolean isModified() {
            return modified;
        }

        private void markModified(ASTNode original, ASTNode replacement) {
            if (!modified) {
                modified = true;
                firstModifiedNode = original;
                firstModifiedNodeReplacement = replacement;
            }
        }

        @Override
        public ASTNode visit(Touching node) {
            if (node.getTouchable() instanceof MousePointer
                    || node.getTouchable() instanceof Edge
                    || node.getTouchable() instanceof SpriteTouchable) {
                Touching replacement = new Touching(
                        new AsTouchable(
                                new Variable(
                                        new StrId(new StringLiteral("var_" + UUID.randomUUID())),
                                        new NoBlockMetadata()
                                )
                        ),
                        apply(node.getMetadata())
                );
                markModified(node, replacement);
                return replacement;
            }
            return super.visit(node);
        }

        @Override
        public ASTNode visit(ColorTouchingColor node) {
            if (node.getOperand1() instanceof ColorLiteral) {
                ColorTouchingColor replacement = new ColorTouchingColor(
                        new FromNumber(
                                new AsNumber(
                                        new Variable(
                                                new StrId(new StringLiteral("var_" + UUID.randomUUID())),
                                                new NoBlockMetadata()
                                        )
                                )
                        ),
                        apply(node.getOperand2()),
                        apply(node.getMetadata())
                );
                markModified(node, replacement);
                return replacement;
            }
            return super.visit(node);
        }

        @Override
        public ASTNode visit(SpriteTouchingColor node) {
            if (node.getColor() instanceof ColorLiteral) {
                SpriteTouchingColor replacement = new SpriteTouchingColor(
                        new FromNumber(
                                new AsNumber(
                                        new Variable(
                                                new StrId(new StringLiteral("var_" + UUID.randomUUID())),
                                                new NoBlockMetadata()
                                        )
                                )
                        ),
                        apply(node.getMetadata())
                );
                markModified(node, replacement);
                return replacement;
            }
            return super.visit(node);
        }

        @Override
        public ASTNode visit(SetPenColorToColorStmt node) {
            if (node.getColorExpr() instanceof ColorLiteral) {
                SetPenColorToColorStmt replacement = new SetPenColorToColorStmt(
                        new FromNumber(
                                new AsNumber(
                                        new Variable(
                                                new StrId(new StringLiteral("var_" + UUID.randomUUID())),
                                                new NoBlockMetadata()
                                        )
                                )
                        ),
                        apply(node.getMetadata())
                );
                markModified(node, replacement);
                return replacement;
            }
            return super.visit(node);
        }
    }
}
