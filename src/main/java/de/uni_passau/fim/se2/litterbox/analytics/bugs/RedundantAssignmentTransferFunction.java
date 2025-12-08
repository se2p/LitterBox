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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.GraphicEffect.GraphicEffectType;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SoundEffect.SoundEffectType;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.cfg.Attribute;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ListVariable;
import de.uni_passau.fim.se2.litterbox.cfg.Parameter;
import de.uni_passau.fim.se2.litterbox.cfg.RobotAttribute;
import de.uni_passau.fim.se2.litterbox.cfg.Use;
import de.uni_passau.fim.se2.litterbox.cfg.Variable;
import de.uni_passau.fim.se2.litterbox.dataflow.ForwardFlowDirection;
import de.uni_passau.fim.se2.litterbox.dataflow.LivenessTransferFunction;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Custom Transfer Function for Redundant Assignment analysis.
 * Treats all global/sprite variables and parameters as "Live" at exit nodes,
 * ensuring that an assignment is only redundant if it is overwritten before any use *and* before the end of the script.
 */
public class RedundantAssignmentTransferFunction extends LivenessTransferFunction {
    private final Program program;
    private final ControlFlowGraph cfg;

    public RedundantAssignmentTransferFunction(Program program, ControlFlowGraph cfg) {
        this.program = program;
        this.cfg = cfg;
    }

    @Override
    public Set<Use> apply(CFGNode node, Set<Use> inFacts) {
        // Handle Halting Blocks -> Treat as Use of Visibility (and potentially Costume/Backdrop)
        Set<Use> extraUses = new HashSet<>();
        if (containsHaltingNode(node.getASTNode())) {
            // For current actor
            if (node.getActor() != null) {
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
        }

        // Check if node is an Exit Node (no forward successors)
        if (new ForwardFlowDirection().getOutNodes(cfg, node).isEmpty()) {
            // It is an exit node.
            // Augment inFacts with All Variables as Uses.
            Set<Use> augmented = new HashSet<>(inFacts);

            // Collect Globals and Sprite Variables
            Map<String, VariableInfo> vars = program.getSymbolTable().getVariables();
            for (VariableInfo varInfo : vars.values()) {
                // Create artificial Use
                // Construct Qualified Identifier
                Qualified qualified = new Qualified(
                        new StrId(varInfo.actor()),
                        new de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable(
                                new StrId(varInfo.variableName())
                        )
                );
                // Wrap in CFG Variable
                Variable cfgVar = new Variable(qualified);

                augmented.add(new Use(node, cfgVar));

                // Also add Unqualified version (StrId) to support unresolved ASTs
                Variable unqualifiedVar = new Variable(new StrId(varInfo.variableName()));
                augmented.add(new Use(node, unqualifiedVar));
            }

            // Collect Parameters if inside Procedure
            ASTNode context = node.getScriptOrProcedure();
            if (context instanceof ProcedureDefinition proc) {
                String scopeId = AstNodeUtil.getBlockId(context);
                proc.getParameterDefinitionList().getParameterDefinitions().forEach(
                        paramDef -> {
                            Parameter param = new Parameter(paramDef.getIdent(), scopeId);
                            augmented.add(new Use(node, param));
                        }
                );
            }

            // Collect Attributes for all Actors
            for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
                for (Attribute.AttributeType type : Attribute.AttributeType.values()) {
                    if (type == Attribute.AttributeType.GRAPHIC_EFFECT) {
                        // Add specific effects
                        for (GraphicEffectType effectType : GraphicEffectType.values()) {
                            augmented.add(
                                    new Use(node, Attribute.graphicEffectOf(actor.getIdent(), effectType.getToken()))
                            );
                        }
                        // Also add generic
                        augmented.add(new Use(node, Attribute.graphicEffectOf(actor.getIdent())));
                    } else if (type == Attribute.AttributeType.SOUND_EFFECT) {
                        for (SoundEffectType effectType : SoundEffectType.values()) {
                            augmented.add(
                                    new Use(node, Attribute.soundEffectOf(actor.getIdent(), effectType.getToken()))
                            );
                        }
                        augmented.add(new Use(node, Attribute.soundEffectOf(actor.getIdent())));
                    } else {
                        augmented.add(new Use(node, new Attribute(actor.getIdent(), type)));
                    }
                }
            }

            // Collect Lists
            Map<String, ExpressionListInfo> lists = program.getSymbolTable().getLists();
            for (ExpressionListInfo listInfo : lists.values()) {
                ListVariable listVar = new ListVariable(new StrId(listInfo.variableName()));
                augmented.add(new Use(node, listVar));
            }

            // Collect Robot Attributes for all Actors (if present)
            // Robot attributes behave like standard attributes (global side effects)
            for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
                for (RobotAttribute.AttributeType type : RobotAttribute.AttributeType.values()) {
                    augmented.add(new Use(node, new RobotAttribute(actor.getIdent(), type)));
                }
            }

            augmented.addAll(extraUses);
            return super.apply(node, augmented);
        }

        // Not exit node, but may have extra uses (halting)
        if (!extraUses.isEmpty()) {
            Set<Use> augmented = new HashSet<>(inFacts);
            augmented.addAll(extraUses);
            // We need to apply standard transfer logic (out - def + use), but since we can't easily modify
            // 'use' inside 'super.apply', and we know 'super.apply' returns 'out', and this is backward analysis...
            // Wait, apply(node, in) -> out.
            // In Backward analysis: in = out of successors.
            // Liveness: in - def + use.
            // If we calculate super.apply(node, in), we get Liveness without our extra uses.
            // Then we simply ADD our extra uses to the result.
            Set<Use> result = super.apply(node, inFacts);
            result.addAll(extraUses);
            return result;
        }

        return super.apply(node, inFacts);
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
