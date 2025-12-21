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
import java.util.Set;

/**
 * Injects the {@link MissingInitialization} bug.
 * <p>
 * <b>Bug Pattern:</b> A sprite/stage lacks proper initialization at the start of the program,
 * causing state to persist between runs.
 * <p>
 * <b>Injection Strategy:</b> Identifies initialization blocks at the beginning of
 * {@link GreenFlag} scripts and removes them one at a time.
 * <p>
 * <b>Initialization Blocks:</b>
 * <ul>
 *   <li>Motion: GoToPosXY, GoToPos, SetXTo, SetYTo, PointInDirection, PointTowards, SetRotationStyle, SetDragMode</li>
 *   <li>Sprite Look: Hide, Show, SwitchCostumeTo, SetSizeTo, GoToLayer</li>
 *   <li>Actor Look: SwitchBackdrop, SetGraphicEffectTo, ClearGraphicEffects</li>
 *   <li>Sound: StopAllSounds, ClearSoundEffects, SetSoundEffectTo, SetVolumeTo</li>
 *   <li>Common: SetVariableTo, ResetTimer</li>
 * </ul>
 */
public class MissingInitializationInjector extends AbstractBugInjector {

    /** Statement types that represent initialization operations. */
    private static final Set<Class<? extends Stmt>> INITIALIZATION_TYPES = Set.of(
            // Motion blocks
            GoToPosXY.class, GoToPos.class, SetXTo.class, SetYTo.class,
            PointInDirection.class, PointTowards.class, SetRotationStyle.class, SetDragMode.class,
            // Sprite look blocks
            Hide.class, Show.class, SwitchCostumeTo.class, SetSizeTo.class, GoToLayer.class,
            // Actor look blocks
            SwitchBackdrop.class, SetGraphicEffectTo.class, ClearGraphicEffects.class,
            // Sound blocks
            StopAllSounds.class, ClearSoundEffects.class, SetSoundEffectTo.class, SetVolumeTo.class,
            // Common blocks
            SetVariableTo.class, ResetTimer.class
    );

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
                    mutateClone(script.getStmtList(), clonedStmtList -> {
                        clonedStmtList.getStmts().remove(index);
                    });
                } else {
                    break; // Stop at the first non-initialization statement
                }
            }
        }
        super.visit(script);
    }

    private boolean isInitializationStatement(Stmt stmt) {
        return INITIALIZATION_TYPES.contains(stmt.getClass());
    }
}
