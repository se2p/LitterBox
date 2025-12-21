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

/**
 * Injects the {@link OverwrittenVisualEffect} bug.
 * <p>
 * This injector inserts a duplicate visual effect statement immediately after
 * an existing one, creating an overwritten visual effect bug.
 * <p>
 * Supported statement types (matching the bug finder):
 * <ul>
 *   <li>Costume: {@link SwitchCostumeTo}, {@link NextCostume}</li>
 *   <li>Backdrop: {@link SwitchBackdrop}, {@link NextBackdrop}</li>
 *   <li>Size: {@link SetSizeTo}</li>
 *   <li>Visibility: {@link Show}, {@link Hide}</li>
 *   <li>Layer: {@link GoToLayer}, {@link ChangeLayerBy}</li>
 *   <li>Bubble: {@link Say}, {@link Think}</li>
 *   <li>Graphic Effects: {@link SetGraphicEffectTo}</li>
 *   <li>Clear Effects: {@link ClearGraphicEffects}</li>
 * </ul>
 */
public class OverwrittenVisualEffectInjector extends AbstractBugInjector {

    private static final NumExpr RESET_VALUE = new NumberLiteral(0.0);

    @Override
    public IssueFinder getIssueFinder() {
        return new OverwrittenVisualEffect();
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

    /**
     * Checks if a statement is a visual effect statement that can be duplicated.
     */
    private boolean isVisualEffectStatement(Stmt stmt) {
        return stmt instanceof SwitchCostumeTo
                || stmt instanceof NextCostume
                || stmt instanceof SwitchBackdrop
                || stmt instanceof NextBackdrop
                || stmt instanceof SetSizeTo
                || stmt instanceof Show
                || stmt instanceof Hide
                || stmt instanceof GoToLayer
                || stmt instanceof ChangeLayerBy
                || stmt instanceof Say
                || stmt instanceof Think
                || stmt instanceof SetGraphicEffectTo
                || stmt instanceof ClearGraphicEffects;
    }

    /**
     * Inserts a duplicate statement that overwrites the original.
     * For SetGraphicEffectTo, inserts a reset to 0.
     * For other statements, duplicates the same statement.
     */
    private void addDuplicateMutation(StmtList stmtList, int index) {
        addMutation(stmtList, clonedList -> {
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
     * For other statements, creates a copy with empty metadata.
     */
    private Stmt createDuplicate(Stmt original) {
        NonDataBlockMetadata emptyMeta = NonDataBlockMetadata.emptyNonBlockMetadata();

        if (original instanceof SetGraphicEffectTo setEffect) {
            // Reset effect to 0
            return new SetGraphicEffectTo(setEffect.getEffect(), RESET_VALUE, emptyMeta);
        } else if (original instanceof SwitchCostumeTo switchCostume) {
            return new SwitchCostumeTo(switchCostume.getCostumeChoice(), emptyMeta);
        } else if (original instanceof NextCostume) {
            return new NextCostume(emptyMeta);
        } else if (original instanceof SwitchBackdrop switchBackdrop) {
            return new SwitchBackdrop(switchBackdrop.getElementChoice(), emptyMeta);
        } else if (original instanceof NextBackdrop) {
            return new NextBackdrop(emptyMeta);
        } else if (original instanceof SetSizeTo setSizeTo) {
            return new SetSizeTo(setSizeTo.getPercent(), emptyMeta);
        } else if (original instanceof Show) {
            return new Show(emptyMeta);
        } else if (original instanceof Hide) {
            return new Hide(emptyMeta);
        } else if (original instanceof GoToLayer goToLayer) {
            return new GoToLayer(goToLayer.getLayerChoice(), emptyMeta);
        } else if (original instanceof ChangeLayerBy changeLayerBy) {
            return new ChangeLayerBy(changeLayerBy.getNum(), changeLayerBy.getForwardBackwardChoice(), emptyMeta);
        } else if (original instanceof Say say) {
            return new Say(say.getString(), emptyMeta);
        } else if (original instanceof Think think) {
            return new Think(think.getThought(), emptyMeta);
        } else if (original instanceof ClearGraphicEffects) {
            return new ClearGraphicEffects(emptyMeta);
        }
        return null;
    }
}
