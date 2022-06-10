/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

public class NodeDeletionVisitor extends OnlyCodeCloneVisitor {

    private ASTNode target;

    public NodeDeletionVisitor(ASTNode target) {
        this.target = target;
    }

    @Override
    public ASTNode visit(Equals node) {
        if (isTargetNode(node)) {
            return new UnspecifiedBoolExpr();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(LessThan node) {
        if (isTargetNode(node)) {
            return new UnspecifiedBoolExpr();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(BiggerThan node) {
        if (isTargetNode(node)) {
            return new UnspecifiedBoolExpr();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Not node) {
        if (isTargetNode(node)) {
            return new UnspecifiedBoolExpr();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(And node) {
        if (isTargetNode(node)) {
            return new UnspecifiedBoolExpr();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Or node) {
        if (isTargetNode(node)) {
            return new UnspecifiedBoolExpr();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Qualified node) {
        if (isTargetNode(node)) {
            return new StringLiteral("");
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ColorTouchingColor node) {
        if (isTargetNode(node)) {
            return new UnspecifiedBoolExpr();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Touching node) {
        if (isTargetNode(node)) {
            return new UnspecifiedBoolExpr();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(WithExpr node) {
        if (isTargetNode(node)) {
            return new Next(node.getMetadata());
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StringContains node) {
        if (isTargetNode(node)) {
            return new UnspecifiedBoolExpr();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(IsKeyPressed node) {
        if (isTargetNode(node)) {
            return new UnspecifiedBoolExpr();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(IsMouseDown node) {
        if (isTargetNode(node)) {
            return new UnspecifiedBoolExpr();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(FromNumber node) {
        if (isTargetNode(node)) {
            return new ColorLiteral(0, 0, 0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Add node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(AsNumber node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Current node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DaysSince2000 node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DistanceTo node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Div node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(IndexOf node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(LengthOfString node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(LengthOfVar node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Loudness node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Minus node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Mod node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(MouseX node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(MouseY node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Mult node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(NumFunctOf node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PickRandom node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Round node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Timer node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(AsString node) {
        if (isTargetNode(node)) {
            return new StringLiteral("");
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(AttributeOf node) {
        if (isTargetNode(node)) {
            return new StringLiteral("");
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ItemOfVariable node) {
        if (isTargetNode(node)) {
            return new StringLiteral("");
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Join node) {
        if (isTargetNode(node)) {
            return new StringLiteral("");
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(LetterOf node) {
        if (isTargetNode(node)) {
            return new StringLiteral("");
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Username node) {
        if (isTargetNode(node)) {
            return new StringLiteral("");
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(FromExpression node) {
        if (isTargetNode(node)) {
            return new RandomPos(node.getMetadata());
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Costume node) {
        if (isTargetNode(node)) {
            return new StringLiteral("");
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Backdrop node) {
        if (isTargetNode(node)) {
            return new StringLiteral("");
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Direction node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PositionX node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PositionY node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Size node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Volume node) {
        if (isTargetNode(node)) {
            return new NumberLiteral(0);
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Answer node) {
        if (isTargetNode(node)) {
            return new StringLiteral("");
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(AttributeFromVariable node) {
        if (isTargetNode(node)) {
            return new AttributeFromFixed(new FixedAttribute("y position"));
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SpriteTouchingColor node) {
        if (isTargetNode(node)) {
            return new UnspecifiedBoolExpr();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Variable node) {
        if (isTargetNode(node)) {
            return new UnspecifiedExpression();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ScratchList node) {
        if (isTargetNode(node)) {
            return new UnspecifiedExpression();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Parameter node) {
        if (isTargetNode(node)) {
            return new UnspecifiedExpression();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ListContains node) {
        if (isTargetNode(node)) {
            return new UnspecifiedBoolExpr();
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ExprLanguage node) {
        if (isTargetNode(node)) {
            return new FixedLanguage("en", node.getMetadata());
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ExprVoice node) {
        if (isTargetNode(node)) {
            return new FixedVoice("Alto", node.getMetadata());
        }
        return super.visit(node);
    }

    protected boolean isTargetNode(ASTNode node) {
        return node == target;
    }
}
