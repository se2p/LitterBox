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
package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.event.EventAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumFunct;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.NameNum;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.GraphicEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SoundEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ForwardBackwardChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.LayerChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.DragMode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.RotationStyle;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class TokenVisitor implements ScratchVisitor {

    private String token = "";

    public String getToken() {
        return StringUtils.deleteWhitespace(token).replace(",", "");
    }

    @Override
    public void visit(ASTNode node) {
        token = node.getUniqueName();
    }

    @Override
    public void visit(StringLiteral node) {
        token = node.getText();
    }

    @Override
    public void visit(BoolLiteral node) {
        token = Boolean.toString(node.getValue());
    }

    @Override
    public void visit(NumberLiteral node) {
        if (Math.floor(node.getValue()) == node.getValue()) {
            token = Integer.toString((int) node.getValue());
        } else {
            token = String.format(Locale.ROOT, "%.2f", node.getValue());
        }
    }

    @Override
    public void visit(TimeComp node) {
        token = node.getTypeName();
    }

    @Override
    public void visit(SoundEffect node) {
        token = node.getTypeName();
    }

    @Override
    public void visit(RotationStyle node) {
        token = node.getTypeName();
    }

    @Override
    public void visit(NumFunct node) {
        token = node.getTypeName();
    }

    @Override
    public void visit(NameNum node) {
        token = node.getTypeName();
    }

    @Override
    public void visit(LayerChoice node) {
        token = node.getTypeName();
    }

    @Override
    public void visit(GraphicEffect node) {
        token = node.getTypeName();
    }

    @Override
    public void visit(ForwardBackwardChoice node) {
        token = node.getTypeName();
    }

    @Override
    public void visit(EventAttribute node) {
        token = node.getTypeName();
    }

    @Override
    public void visit(DragMode node) {
        token = node.getTypeName();
    }

    @Override
    public void visit(FixedAttribute node) {
        token = node.getTypeName();
    }

    @Override
    public void visit(ColorLiteral node) {
        token = String.format("%02x%02x%02x", node.getRed(), node.getGreen(), node.getBlue());
    }
}
