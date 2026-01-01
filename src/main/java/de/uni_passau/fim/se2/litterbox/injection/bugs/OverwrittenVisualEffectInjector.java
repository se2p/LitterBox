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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.OverwrittenVisualEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ClearGraphicEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.NextBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.List;
import java.util.Set;

/**
 * Injects the {@link OverwrittenVisualEffect} bug.
 *
 * <p>
 * <b>Bug Pattern:</b> A visual effect is set and then immediately overwritten before
 * it can take effect.
 *
 * <p>
 * <b>Injection Strategy:</b> Inserts a duplicate visual effect statement immediately
 * after an existing one, making the first one redundant.
 *
 * <p>
 * <b>Supported Statement Types:</b>
 * <ul>
 *   <li>Costume: SwitchCostumeTo, NextCostume</li>
 *   <li>Backdrop: SwitchBackdrop, NextBackdrop</li>
 *   <li>Size: SetSizeTo</li>
 *   <li>Visibility: Show, Hide</li>
 *   <li>Layer: GoToLayer, ChangeLayerBy</li>
 *   <li>Bubble: Say, Think</li>
 *   <li>Graphic Effects: SetGraphicEffectTo, ClearGraphicEffects</li>
 * </ul>
 */
public class OverwrittenVisualEffectInjector extends AbstractBugInjector {

    private static final NumExpr RESET_VALUE = new NumberLiteral(0.0);

    /**
     * Statement types that represent visual effects that can be duplicated.
     */
    private static final Set<Class<? extends Stmt>> VISUAL_EFFECT_TYPES = Set.of(
            SwitchCostumeTo.class, NextCostume.class,
            SwitchBackdrop.class, NextBackdrop.class,
            SetSizeTo.class,
            Show.class, Hide.class,
            GoToLayer.class, ChangeLayerBy.class,
            Say.class, Think.class,
            SetGraphicEffectTo.class, ClearGraphicEffects.class
    );

    private final IssueFinder issueFinder = new OverwrittenVisualEffect();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(StmtList stmtList) {
        List<Stmt> statements = stmtList.getStmts();
        for (int i = 0; i < statements.size(); i++) {
            Stmt stmt = statements.get(i);
            if (isVisualEffectStatement(stmt)) {
                addDuplicateMutation(stmtList, i);
            }
        }
        super.visit(stmtList);
    }

    private boolean isVisualEffectStatement(Stmt stmt) {
        return VISUAL_EFFECT_TYPES.contains(stmt.getClass());
    }

    private void addDuplicateMutation(StmtList stmtList, int index) {
        modifyClonedNode(stmtList, clonedList -> {
            if (index < clonedList.getStmts().size()) {
                Stmt original = clonedList.getStmts().get(index);
                Stmt duplicate = createDuplicate(original);
                if (duplicate != null) {
                    clonedList.getStmts().add(index + 1, duplicate);
                    duplicate.setParentNode(clonedList);
                }
            }
        });
    }

    /**
     * Creates a duplicate statement for the given original.
     * For SetGraphicEffectTo, creates a reset-to-0 version.
     */
    private Stmt createDuplicate(Stmt original) {
        NonDataBlockMetadata emptyMeta = NonDataBlockMetadata.emptyNonBlockMetadata();

        return switch (original) {
            case SetGraphicEffectTo setEffect -> new SetGraphicEffectTo(setEffect.getEffect(), RESET_VALUE, emptyMeta);
            case SwitchCostumeTo switchCostume -> new SwitchCostumeTo(switchCostume.getCostumeChoice(), emptyMeta);
            case NextCostume ignored -> new NextCostume(emptyMeta);
            case SwitchBackdrop switchBackdrop -> new SwitchBackdrop(switchBackdrop.getElementChoice(), emptyMeta);
            case NextBackdrop ignored -> new NextBackdrop(emptyMeta);
            case SetSizeTo setSizeTo -> new SetSizeTo(setSizeTo.getPercent(), emptyMeta);
            case Show ignored -> new Show(emptyMeta);
            case Hide ignored -> new Hide(emptyMeta);
            case GoToLayer goToLayer -> new GoToLayer(goToLayer.getLayerChoice(), emptyMeta);
            case ChangeLayerBy changeLayerBy ->
                    new ChangeLayerBy(changeLayerBy.getNum(), changeLayerBy.getForwardBackwardChoice(), emptyMeta);
            case Say say -> new Say(say.getString(), emptyMeta);
            case Think think -> new Think(think.getThought(), emptyMeta);
            case ClearGraphicEffects ignored -> new ClearGraphicEffects(emptyMeta);
            default -> null;
        };
    }
}
