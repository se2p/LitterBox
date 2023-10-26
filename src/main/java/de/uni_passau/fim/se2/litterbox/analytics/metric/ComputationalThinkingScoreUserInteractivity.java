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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsMouseDown;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseX;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseY;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ComputationalThinkingScoreUserInteractivity implements MetricExtractor<Program>, ScratchVisitor {
    public static final String NAME = "ct_score_user_interactivity";
    private int score = 0;

    @Override
    public double calculateMetric(Program program) {
        score = 0;
        program.accept(this);
        return score;
    }

    @Override
    public void visit(GreenFlag node) {
        score = Math.max(1, score);
        visitChildren(node);
    }

    @Override
    public void visit(KeyPressed node) {
        score = Math.max(2, score);
        visitChildren(node);
    }

    @Override
    public void visit(SpriteClicked node) {
        score = Math.max(2, score);
        visitChildren(node);
    }

    @Override
    public void visit(StageClicked node) {
        score = Math.max(2, score);
        visitChildren(node);
    }

    @Override
    public void visit(IsMouseDown node) {
        score = Math.max(2, score);
    }

    @Override
    public void visit(MousePos node) {
        score = Math.max(2, score);
    }

    @Override
    public void visit(MouseX node) {
        score = Math.max(2, score);
    }

    @Override
    public void visit(MouseY node) {
        score = Math.max(2, score);
    }

    @Override
    public void visit(MousePointer node) {
        score = Math.max(2, score);
    }

    @Override
    public void visit(IsKeyPressed node) {
        score = Math.max(2, score);
    }

    @Override
    public void visit(AskAndWait node) {
        score = Math.max(2, score);
    }

    @Override
    public void visit(Answer node) {
        score = Math.max(2, score);
    }

    // TODO: Missing: 'videoSensing_videoToggle', 'videoSensing_videoOn',
    // TODO: Missing: 'videoSensing_setVideoTransparency', 'videoSensing_whenMotionGreaterThan'
    @Override
    public void visit(AttributeAboveValue node) {
        // TODO: Dr. Scratch Paper says all these events, implementation only considers loudness
        if (node.getAttribute().getType() == EventAttribute.EventAttributeType.LOUDNESS) {
            score = 3;
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
