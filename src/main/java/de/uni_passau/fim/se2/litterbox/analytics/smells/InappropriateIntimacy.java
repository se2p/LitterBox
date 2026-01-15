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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Direction;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.PositionX;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.PositionY;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Size;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Backdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.*;

public class InappropriateIntimacy extends AbstractIssueFinder {

    private static final String NAME = "inappropriate_intimacy";
    private static final int MIN_DIFFERENCE = 2;

    @Override
    public void visit(Script script) {
        currentScript = script;
        Set<String> localVariables = new HashSet<>();
        if (program != null && currentActor != null) {
            for (VariableInfo info : program.getSymbolTable().getVariables().values()) {
                if (info.actor().equals(currentActor.getIdent().getName())) {
                    localVariables.add(info.variableName());
                }
            }
        }

        AttributeAccessVisitor visitor = new AttributeAccessVisitor(localVariables);
        script.accept(visitor);

        int ownAccesses = visitor.getOwnAccesses();
        Map<String, Integer> otherSpriteAccesses = visitor.getOtherSpriteAccesses();

        for (Map.Entry<String, Integer> entry : otherSpriteAccesses.entrySet()) {
            if (entry.getValue() - ownAccesses >= MIN_DIFFERENCE) {
                Hint hint = Hint.fromKey(NAME);
                hint.setParameter(Hint.HINT_SPRITE, entry.getKey());
                addIssue(script.getEvent(), script.getEvent().getMetadata(), IssueSeverity.MEDIUM, hint);
                return;
            }
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Collection<String> getHintKeys() {
        return Collections.singletonList(NAME);
    }

    private static class AttributeAccessVisitor implements ScratchVisitor {
        private int ownAccesses = 0;
        private final Map<String, Integer> otherSpriteAccesses = new HashMap<>();
        private final Set<String> localVariables;

        public AttributeAccessVisitor(Set<String> localVariables) {
            this.localVariables = localVariables;
        }

        public int getOwnAccesses() {
            return ownAccesses;
        }

        public Map<String, Integer> getOtherSpriteAccesses() {
            return otherSpriteAccesses;
        }

        @Override
        public void visit(Backdrop node) {
            ownAccesses++;
            visitChildren(node);
        }

        @Override
        public void visit(Costume node) {
            ownAccesses++;
            visitChildren(node);
        }

        @Override
        public void visit(Size node) {
            ownAccesses++;
            visitChildren(node);
        }

        @Override
        public void visit(Direction node) {
            ownAccesses++;
            visitChildren(node);
        }

        @Override
        public void visit(PositionX node) {
            ownAccesses++;
            visitChildren(node);
        }

        @Override
        public void visit(PositionY node) {
            ownAccesses++;
            visitChildren(node);
        }

        @Override
        public void visit(LocalIdentifier node) {
            if (localVariables.contains(node.getName())) {
                ownAccesses++;
            }
            visitChildren(node);
        }

        @Override
        public void visit(AttributeOf node) {
            if (node.getElementChoice() instanceof WithExpr withExpr) {
                Expression expression = withExpr.getExpression();
                String spriteName = null;
                if (expression instanceof StringLiteral stringLiteral) {
                    spriteName = stringLiteral.getText();
                } else if (expression instanceof LocalIdentifier localIdentifier) {
                    spriteName = localIdentifier.getName();
                }

                if (spriteName != null) {
                    otherSpriteAccesses.put(spriteName, otherSpriteAccesses.getOrDefault(spriteName, 0) + 1);
                }
            }
            visitChildren(node);
        }
    }
}
