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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingInitialization;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ClearGraphicEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ClearSoundEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetSoundEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetVolumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StopAllSounds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ResetTimer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.List;

/**
 * Injector for the {@link MissingInitialization} bug.
 * <p>
 * This injector identifies initialization blocks at the beginning of {@link GreenFlag} scripts
 * and removes them one at a time, creating one mutation per initialization statement.
 * <p>
 * Initialization blocks include:
 * <ul>
 *   <li>Motion: {@link GoToPosXY}, {@link GoToPos}, {@link SetXTo}, {@link SetYTo},
 *       {@link PointInDirection}, {@link PointTowards}, {@link SetRotationStyle}, {@link SetDragMode}</li>
 *   <li>Sprite Look: {@link Hide}, {@link Show}, {@link SwitchCostumeTo}, {@link SetSizeTo}, {@link GoToLayer}</li>
 *   <li>Actor Look: {@link SwitchBackdrop}, {@link SetGraphicEffectTo}, {@link ClearGraphicEffects}</li>
 *   <li>Sound: {@link StopAllSounds}, {@link ClearSoundEffects}, {@link SetSoundEffectTo}, {@link SetVolumeTo}</li>
 *   <li>Common: {@link SetVariableTo}, {@link ResetTimer}</li>
 * </ul>
 */
public class MissingInitializationInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new MissingInitialization();
    }

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof GreenFlag) {
            List<Stmt> statements = script.getStmtList().getStmts();

            // Find contiguous initialization statements at start and create one mutation per statement
            for (int i = 0; i < statements.size(); i++) {
                Stmt stmt = statements.get(i);
                if (isInitializationStatement(stmt)) {
                    final int index = i;
                    addMutation(script.getStmtList(), clonedStmtList -> {
                        // Remove only this specific initialization statement
                        clonedStmtList.getStmts().remove(index);
                    });
                } else {
                    // Stop at the first non-initialization statement
                    break;
                }
            }
        }
        super.visit(script);
    }

    /**
     * Determines if a statement is an initialization statement.
     */
    private boolean isInitializationStatement(Stmt stmt) {
        return stmt instanceof SetVariableTo
                // Motion blocks
                || stmt instanceof GoToPosXY
                || stmt instanceof GoToPos
                || stmt instanceof SetXTo
                || stmt instanceof SetYTo
                || stmt instanceof PointInDirection
                || stmt instanceof PointTowards
                || stmt instanceof SetRotationStyle
                || stmt instanceof SetDragMode
                // Sprite look blocks
                || stmt instanceof Hide
                || stmt instanceof Show
                || stmt instanceof SwitchCostumeTo
                || stmt instanceof SetSizeTo
                || stmt instanceof GoToLayer
                // Actor look blocks
                || stmt instanceof SwitchBackdrop
                || stmt instanceof SetGraphicEffectTo
                || stmt instanceof ClearGraphicEffects
                // Sound blocks
                || stmt instanceof StopAllSounds
                || stmt instanceof ClearSoundEffects
                || stmt instanceof SetSoundEffectTo
                || stmt instanceof SetVolumeTo
                // Common blocks
                || stmt instanceof ResetTimer;
    }
}
