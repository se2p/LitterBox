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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.event.EventAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumFunct;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.NameNum;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.FixedDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.FixedInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.FixedNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TFixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
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
import de.uni_passau.fim.se2.litterbox.ast.visitor.MusicExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TranslateExtensionVisitor;

import java.util.Locale;

public class TokenVisitor
        implements ScratchVisitor, PenExtensionVisitor, MusicExtensionVisitor, TranslateExtensionVisitor {

    private final boolean normalised;

    private String token = "";

    private TokenVisitor(final boolean normalised) {
        this.normalised = normalised;
    }

    /**
     * Retrieves the actual literal represented by a node.
     *
     * @param node A node of the AST.
     * @return The literal value of the given node.
     */
    public static String getToken(final ASTNode node) {
        return TokenVisitor.getToken(node, false);
    }

    /**
     * Retrieves the actual normalised literal represented by a node.
     *
     * @param node A node of the AST.
     * @return The literal value of the given node.
     */
    public static String getNormalisedToken(final ASTNode node) {
        return getToken(node, true);
    }

    private static String getToken(final ASTNode node, final boolean normalised) {
        final TokenVisitor visitor = new TokenVisitor(normalised);
        node.accept(visitor);
        return visitor.token;
    }

    private void saveString(final String token) {
        if (normalised) {
            this.token = StringUtil.normaliseString(token);
        } else {
            this.token = token;
        }
    }

    private void saveNumber(final double value) {
        if (Math.floor(value) == value) {
            token = Integer.toString((int) value);
        } else {
            token = String.format(Locale.ROOT, "%.2f", value);
        }
    }

    @Override
    public void visit(ASTNode node) {
        token = node.getUniqueName();
    }

    @Override
    public void visit(StringLiteral node) {
        saveString(node.getText());
    }

    @Override
    public void visit(StrId node) {
        saveString(node.getName());
    }

    @Override
    public void visit(BoolLiteral node) {
        token = Boolean.toString(node.getValue());
    }

    @Override
    public void visit(NumberLiteral node) {
        saveNumber(node.getValue());
    }

    @Override
    public void visit(TimeComp node) {
        token = node.getType().toString();
    }

    @Override
    public void visit(SoundEffect node) {
        token = node.getType().toString();
    }

    @Override
    public void visit(RotationStyle node) {
        token = node.getType().toString();
    }

    @Override
    public void visit(NumFunct node) {
        token = node.getType().toString();
    }

    @Override
    public void visit(NameNum node) {
        token = node.getType().toString();
    }

    @Override
    public void visit(LayerChoice node) {
        token = node.getType().toString();
    }

    @Override
    public void visit(GraphicEffect node) {
        token = node.getType().toString();
    }

    @Override
    public void visit(ForwardBackwardChoice node) {
        token = node.getType().toString();
    }

    @Override
    public void visit(EventAttribute node) {
        token = node.getType().toString();
    }

    @Override
    public void visit(DragMode node) {
        token = node.getType().toString();
    }

    @Override
    public void visit(FixedAttribute node) {
        token = node.getType().toString();
    }

    @Override
    public void visit(FixedNote node) {
        saveNumber(node.getNote());
    }

    @Override
    public void visit(FixedInstrument node) {
        token = node.getType().toString();
    }

    @Override
    public void visit(FixedDrum node) {
        token = node.getType().toString();
    }

    @Override
    public void visit(TFixedLanguage node) {
        token = node.getType().toString();
    }

    @Override
    public void visit(ColorLiteral node) {
        token = node.getRGB();
    }
}
