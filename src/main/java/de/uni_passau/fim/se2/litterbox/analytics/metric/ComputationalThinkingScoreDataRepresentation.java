/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ListContains;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.LengthOfVar;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.ListStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ComputationalThinkingScoreDataRepresentation implements MetricExtractor<Program>, ScratchVisitor {

    private int score = 0;

    @Override
    public double calculateMetric(Program program) {
        score = 0;
        program.accept(this);
        return score;
    }

    @Override
    public void visit(ActorDefinition node) {
        // Make sure variable statements are only counted inside code
        // and not initialization of the program
        visit(node.getScripts());
        visit(node.getProcedureDefinitionList());
    }

    @Override
    public void visit(ListStmt node) {
        // AddTo, DeleteAllOf, DeleteOf, InsertAt, ReplaceItem
        score = 3;
    }

    @Override
    public void visit(LengthOfVar node) {
        // TODO: Shouldn't this be called LengthOfList?
        score = 3;
    }

    @Override
    public void visit(ItemOfVariable node) {
        // TODO: Shouldn't this be called ItemOfList?
        score = 3;
    }

    @Override
    public void visit(ListContains node) {
        score = 3;
    }

    @Override
    public void visit(ShowList node) {
        score = 3;
    }

    @Override
    public void visit(HideList node) {
        score = 3;
    }

    @Override
    public void visit(SetVariableTo node) {
        score = Math.max(2, score);
    }

    @Override
    public void visit(ChangeVariableBy node) {
        score = Math.max(2, score);
    }

    // TODO: Missing in Dr. Scratch, but if we count showing/hiding lists, we also do so for vars
    @Override
    public void visit(ShowVariable node) {
        score = Math.max(2, score);
    }

    // TODO: Missing in Dr. Scratch, but if we count showing/hiding lists, we also do so for vars
    @Override
    public void visit(HideVariable node) {
        score = Math.max(2, score);
    }

    // TODO: Missing in Dr. Scratch: motion_glidesecsto, ifonedgebounce, setdragmode, setrotationstyle
    @Override
    public void visit(SpriteMotionStmt node) {
        score = Math.max(1, score);
    }

    // TODO: Missing in Dr. Scratch: ChangeLayerBy, GoToLayer
    @Override
    public void visit(SpriteLookStmt node) {
        score = Math.max(1, score);
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        score = Math.max(1, score);
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        score = Math.max(1, score);
    }

    // TODO: Missing in Dr. Scratch
    @Override
    public void visit(ClearGraphicEffects node) {
        score = Math.max(1, score);
    }

    @Override
    public void visit(NextBackdrop node) {
        score = Math.max(1, score);
    }

    @Override
    public void visit(SwitchBackdrop node) {
        score = Math.max(1, score);
    }

    // TODO: Missing in Dr. Scratch
    @Override
    public void visit(SwitchBackdropAndWait node) {
        score = Math.max(1, score);
    }

    @Override
    public String getName() {
        return "ct_score_data_representation";
    }
}
