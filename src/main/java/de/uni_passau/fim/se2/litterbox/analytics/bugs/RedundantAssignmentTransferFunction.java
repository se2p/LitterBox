/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugs;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.GraphicEffect.GraphicEffectType;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY;
import de.uni_passau.fim.se2.litterbox.cfg.Attribute;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.Definition;
import de.uni_passau.fim.se2.litterbox.cfg.Use;
import de.uni_passau.fim.se2.litterbox.dataflow.ForwardFlowDirection;
import de.uni_passau.fim.se2.litterbox.dataflow.LivenessTransferFunction;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom Transfer Function for Redundant Assignment analysis.
 * Treats all variables/attributes defined in this script as "Live" at exit nodes,
 * ensuring that an assignment is only redundant if it is overwritten before any use *and* before the end of the script.
 */
public class RedundantAssignmentTransferFunction extends LivenessTransferFunction {
    private final ControlFlowGraph cfg;

    public RedundantAssignmentTransferFunction(ControlFlowGraph cfg) {
        this.cfg = cfg;
    }

    @Override
    public Set<Use> apply(CFGNode node, Set<Use> inFacts) {
        Set<Use> extraUses = getHaltingUses(node);

        // Check if node is an Exit Node (no forward successors)
        if (isExitNode(node)) {
            // It is an exit node.
            // Augment inFacts with All Variables as Uses.
            Set<Use> augmented = new HashSet<>(inFacts);
            augmented.addAll(getDefinedVariablesAsUses(node));
            augmented.addAll(extraUses);
            return super.apply(node, augmented);
        }

        // Not exit node, but may have extra uses (halting)
        if (!extraUses.isEmpty()) {
            Set<Use> result = super.apply(node, inFacts);
            result.addAll(extraUses);
            return result;
        }

        return super.apply(node, inFacts);
    }

    private boolean isExitNode(CFGNode node) {
        return new ForwardFlowDirection().getOutNodes(cfg, node).isEmpty();
    }

    /**
     * Collects implicit uses of visual attributes for halting blocks.
     * <p>
     * Blocks that halt execution (like {@code wait}, {@code say for secs}, {@code glide}) keep the actor
     * in its current state for a duration of time. During this time, the actor's visual appearance
     * (costume, effects, position) is "consumed" by the user observing the stage.
     * <p>
     * Without this, an assignment to a visual attribute followed by a wait would be flagged as redundant
     * if it is locally overwritten after the wait.
     * E.g.:
     * <pre>
     * set [color] effect to (50)
     * wait (1) secs  <-- Implicitly "uses" the color effect (keeps it visible)
     * set [color] effect to (0)
     * </pre>
     *
     * @param node The CFG node to check.
     * @return A set of implicit uses if the node contains a halting block.
     */
    private Set<Use> getHaltingUses(CFGNode node) {
        Set<Use> extraUses = new HashSet<>();
        // Handle Halting Blocks -> Treat as Use of Visibility (and potentially Costume/Backdrop)
        if (containsHaltingNode(node.getASTNode()) && node.getActor() != null) {
            extraUses.add(new Use(node, Attribute.visibilityOf(node.getActor().getIdent())));
            extraUses.add(new Use(node, Attribute.costumeOf(node.getActor().getIdent())));
            // Backdrops technically are stage, but attributes handles this.
            extraUses.add(new Use(node, Attribute.backdropOf(node.getActor().getIdent())));

            // Add Graphic Effects
            for (GraphicEffectType effectType : GraphicEffectType.values()) {
                extraUses.add(
                        new Use(node, Attribute.graphicEffectOf(node.getActor().getIdent(), effectType.getToken()))
                );
            }
            extraUses.add(new Use(node, Attribute.graphicEffectOf(node.getActor().getIdent())));

            // Add other visual attributes
            extraUses.add(
                    new Use(node, new Attribute(node.getActor().getIdent(), Attribute.AttributeType.POSITION_X))
            );
            extraUses.add(
                    new Use(node, new Attribute(node.getActor().getIdent(), Attribute.AttributeType.POSITION_Y))
            );
            extraUses.add(
                    new Use(node, new Attribute(node.getActor().getIdent(), Attribute.AttributeType.ROTATION))
            );
            extraUses.add(
                    new Use(node, new Attribute(node.getActor().getIdent(), Attribute.AttributeType.SIZE))
            );
        }
        return extraUses;
    }

    /**
     * Collects all variables that are defined ANYWHERE in this CFG.
     * <p>
     * If a variable is assigned in this script, we assume it is "Live" at the exit,
     * to prevent flagging the last assignment as a dead store.
     *
     * @param exitNode The exit node.
     * @return A set of Uses for all variables defined in this CFG.
     */
    private Set<Use> getDefinedVariablesAsUses(CFGNode exitNode) {
        Set<Use> uses = new HashSet<>();
        for (CFGNode cfgNode : cfg.getNodes()) {
            for (Definition def : cfgNode.getDefinitions()) {
                uses.add(new Use(exitNode, def.getDefinable()));
            }
        }
        return uses;
    }

    private boolean containsHaltingNode(ASTNode node) {
        if (node instanceof Stmt stmt && isHalting(stmt)) {
            return true;
        }
        if (node != null && node.getChildren() != null) {
            for (ASTNode child : node.getChildren()) {
                if (containsHaltingNode(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isHalting(Stmt stmt) {
        return stmt instanceof WaitSeconds
                || stmt instanceof ThinkForSecs
                || stmt instanceof SayForSecs
                || stmt instanceof GlideSecsToXY
                || stmt instanceof GlideSecsTo
                || stmt instanceof PlaySoundUntilDone
                || stmt instanceof Speak;
    }
}
