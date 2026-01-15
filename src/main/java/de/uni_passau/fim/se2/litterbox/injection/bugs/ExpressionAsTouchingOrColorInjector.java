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

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ExpressionAsTouchingOrColor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Join;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Username;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.AsTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Injects ExpressionAsTouchingOrColor bug by replacing a Touching or ColorTouchingColor
 * expression's operand with a reporter expression instead of a literal touchable/color.
 */
public class ExpressionAsTouchingOrColorInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new ExpressionAsTouchingOrColor();
    private static final Random RANDOM = new Random();

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
            String originalScriptId = "";
            String mutatedScriptId = "";

            if (visitor.firstModifiedNode != null) {
                spriteName = findContainingActorName(visitor.firstModifiedNode);
                originalScriptId = findContainingScriptId(visitor.firstModifiedNode);
                originalScript = safeGetScratchBlocks(findContextNode(visitor.firstModifiedNode));
            }
            if (visitor.firstModifiedNodeReplacement != null) {
                ASTNode mutatedContext = findContextNode(visitor.firstModifiedNodeReplacement);
                mutatedScript = safeGetScratchBlocks(mutatedContext);
                mutatedScriptId = findContainingScriptId(visitor.firstModifiedNodeReplacement);
            }

            return Collections.singletonList(
                    new InjectionResult(cloned, spriteName, originalScriptId, mutatedScriptId, originalScript,
                            mutatedScript));
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

    private Expression getRandomReporter() {
        // Create new metadata with unique ID for the injected block
        BlockMetadata metadata = new DataBlockMetadata(CloneVisitor.generateUID(), null, 0, 0);
        int choice = RANDOM.nextInt(9);
        return switch (choice) {
            case 0 -> new Add(new NumberLiteral(1), new NumberLiteral(1), metadata);
            case 1 -> new DistanceTo(
                    new MousePos(new DataBlockMetadata(CloneVisitor.generateUID(), null, 0, 0)),
                    metadata
            );
            case 2 -> new MouseX(metadata);
            case 3 -> new MouseY(metadata);
            case 4 -> new Loudness(metadata);
            case 5 -> new Timer(metadata);
            case 6 -> new Current(new TimeComp("year"), metadata);
            case 7 -> new Join(new StringLiteral("a"), new StringLiteral("b"), metadata);
            case 8 -> new Username(metadata);
            default ->
                // Fallback to simple number literal if something goes wrong (shouldn't happen)
                    new NumberLiteral(1);
        };
    }

    private static class CustomMappingCloneVisitor extends CloneVisitor {
        private boolean modified = false;
        ASTNode firstModifiedNode = null;
        ASTNode firstModifiedNodeReplacement = null;
        private final ExpressionAsTouchingOrColorInjector injector = new ExpressionAsTouchingOrColorInjector();

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
                // Touching block expects an Expression wrapped in AsTouchable
                // Any reporter is fine here
                Touching replacement = new Touching(
                        new AsTouchable(injector.getRandomReporter(),
                                new DataBlockMetadata(CloneVisitor.generateUID(), null, 0, 0)
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
                // ColorTouchingColor (operand 1) expects Color. FromNumber implements Color.
                // FromNumber expects NumExpr.
                Expression randomReporter = injector.getRandomReporter();
                NumExpr numExpr;

                if (randomReporter instanceof NumExpr tmp) {
                    numExpr = tmp;
                } else {
                    // Wrap non-number reporters in AsNumber
                    numExpr = new AsNumber(
                            randomReporter,
                            new DataBlockMetadata(CloneVisitor.generateUID(), null, 0, 0)
                    );
                }

                ColorTouchingColor replacement = new ColorTouchingColor(
                        new FromNumber(numExpr,
                                new DataBlockMetadata(CloneVisitor.generateUID(), null, 0, 0)
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
                // SpriteTouchingColor expects Color. FromNumber implements Color.
                Expression randomReporter = injector.getRandomReporter();
                NumExpr numExpr;
                if (randomReporter instanceof NumExpr tmp) {
                    numExpr = tmp;
                } else {
                    numExpr = new AsNumber(
                            randomReporter,
                            new DataBlockMetadata(CloneVisitor.generateUID(), null, 0, 0)
                    );
                }

                SpriteTouchingColor replacement = new SpriteTouchingColor(
                        new FromNumber(numExpr,
                                new DataBlockMetadata(CloneVisitor.generateUID(), null, 0, 0)
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
                // SetPenColorToColorStmt expects Color.
                Expression randomReporter = injector.getRandomReporter();
                NumExpr numExpr;
                if (randomReporter instanceof NumExpr tmp) {
                    numExpr = tmp;
                } else {
                    numExpr = new AsNumber(
                            randomReporter,
                            new DataBlockMetadata(CloneVisitor.generateUID(), null, 0, 0)
                    );
                }

                SetPenColorToColorStmt replacement = new SetPenColorToColorStmt(
                        new FromNumber(numExpr,
                                new DataBlockMetadata(CloneVisitor.generateUID(), null, 0, 0)
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
