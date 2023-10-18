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

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.LinkedHashMap;
import java.util.Map;

public class ComputationalThinkingScoreParallelization implements MetricExtractor<Program>, ScratchVisitor {
    public final static String NAME = "ct_score_parallelization";
    private int score = 0;
    private Map<Event, Integer> eventMap = new LinkedHashMap<>();

    @Override
    public MetricResult calculateMetric(Program program) {
        // TODO: Missing videoSensing_whenMotionGreaterThan --> 3
        score = 0;
        eventMap.clear();
        program.accept(this);
        return new MetricResult(NAME, score);
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        // 2 Scripts start on the same received message
        int count = eventMap.getOrDefault(node, 0) + 1;
        eventMap.put(node, count);

        if (count > 1) {
            score = 3;
        }
    }


    @Override
    public void visit(BackdropSwitchTo node) {
        // 2 Scripts start on the same backdrop change
        int count = eventMap.getOrDefault(node, 0) + 1;
        eventMap.put(node, count);

        if (count > 1) {
            score = 3;
        }
    }

    @Override
    public void visit(AttributeAboveValue node) {
        // 2 Scripts start on the same multimedia (audio, timer) event
        int count = eventMap.getOrDefault(node, 0) + 1;
        eventMap.put(node, count);

        if (count > 1) {
            score = 3;
        }
    }

    @Override
    public void visit(KeyPressed node) {
        // 2 Scripts start on the same key pressed
        int count = eventMap.getOrDefault(node, 0) + 1;
        eventMap.put(node, count);

        if (count > 1) {
            score = Math.max(score, 2);
        }
    }

    @Override
    public void visit(SpriteClicked node) {
        // Sprite with 2 scripts on clicked
        int count = eventMap.getOrDefault(node, 0) + 1;
        eventMap.put(node, count);

        if (count > 1) {
            score = Math.max(score, 2);
        }
    }

    @Override
    public void visit(StageClicked node) {
        // Stage with 2 scripts on clicked
        int count = eventMap.getOrDefault(node, 0) + 1;
        eventMap.put(node, count);

        if (count > 1) {
            score = Math.max(score, 2);
        }
    }

    @Override
    public void visit(GreenFlag node) {
        // 2 scripts on green flag
        int count = eventMap.getOrDefault(node, 0) + 1;
        eventMap.put(node, count);

        if (count > 1) {
            score = Math.max(score, 1);
        }
    }

    @Override
    public String getName() {
        return "ct_score_parallelization";
    }
}
