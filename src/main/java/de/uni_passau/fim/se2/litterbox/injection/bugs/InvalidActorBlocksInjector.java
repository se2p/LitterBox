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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InvalidActorBlocks;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.SpriteClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StageClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects InvalidActorBlocks bug by copying scripts to the wrong actor type.
 *
 * <p>This injector finds scripts that contain actor-specific blocks and copies them
 * to the wrong actor type where they won't function properly:
 * <ul>
 *   <li>Scripts with sprite-only blocks (motion, look, clone, etc.) are copied to the stage</li>
 *   <li>Scripts with stage-only blocks (stage clicked, switch backdrop and wait) are copied to sprites</li>
 * </ul>
 */
public class InvalidActorBlocksInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new InvalidActorBlocks();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(Program program) {
        ActorDefinition stage = findStage(program);
        ActorDefinition firstSprite = findFirstSprite(program);

        // Inject sprite-only scripts to stage
        if (stage != null) {
            for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
                if (actor.isSprite()) {
                    for (Script script : actor.getScripts().getScriptList()) {
                        if (script.getEvent() instanceof Never) {
                            continue;
                        }
                        if (hasSpriteOnlyBlocks(script)) {
                            injectScriptToActor(script, stage);
                        }
                    }
                }
            }
        }

        // Inject stage-only scripts to sprite
        if (firstSprite != null && stage != null) {
            for (Script script : stage.getScripts().getScriptList()) {
                if (script.getEvent() instanceof Never) {
                    continue;
                }
                if (hasStageOnlyBlocks(script)) {
                    injectScriptToActor(script, firstSprite);
                }
            }
        }
    }

    private void injectScriptToActor(Script originalScript, ActorDefinition targetActor) {
        String scriptId = AstNodeUtil.getBlockId(originalScript);
        if (scriptId == null) {
            scriptId = "";
        }

        modifyClonedNode(originalScript, clonedScript -> {
            Program clonedProgram = findProgram(clonedScript);
            if (clonedProgram == null) {
                return;
            }

            // Find the target actor in the cloned program
            ActorDefinition clonedTarget = findActorByName(clonedProgram, targetActor.getIdent().getName());
            if (clonedTarget == null) {
                return;
            }

            // Find the source actor and remove the script from it
            ActorDefinition sourceActor = findContainingActor(clonedScript);
            if (sourceActor != null) {
                sourceActor.getScripts().getScriptList().remove(clonedScript);
            }

            // Add the script to the target actor (move, not copy)
            clonedTarget.getScripts().getScriptList().add(clonedScript);
        }, scriptId);
    }

    private ActorDefinition findContainingActor(Script script) {
        var node = script.getParentNode();
        while (node != null) {
            if (node instanceof ActorDefinition actor) {
                return actor;
            }
            node = node.getParentNode();
        }
        return null;
    }

    private boolean hasSpriteOnlyBlocks(Script script) {
        final boolean[] found = {false};

        class SpriteBlockVisitor implements ScratchVisitor, PenExtensionVisitor {
            @Override
            public void visit(SpriteMotionStmt node) {
                found[0] = true;
            }

            @Override
            public void visit(SpriteLookStmt node) {
                found[0] = true;
            }

            @Override
            public void visit(DeleteClone node) {
                found[0] = true;
            }

            @Override
            public void visit(SpriteClicked node) {
                found[0] = true;
            }

            @Override
            public void visit(StartedAsClone node) {
                found[0] = true;
            }

            @Override
            public void visit(Myself node) {
                found[0] = true;
            }

            @Override
            public void visit(PositionX node) {
                found[0] = true;
            }

            @Override
            public void visit(PositionY node) {
                found[0] = true;
            }

            @Override
            public void visit(Direction node) {
                found[0] = true;
            }

            @Override
            public void visit(Costume node) {
                found[0] = true;
            }

            @Override
            public void visit(Size node) {
                found[0] = true;
            }

            @Override
            public void visit(Touching node) {
                found[0] = true;
            }

            @Override
            public void visit(SpriteTouchingColor node) {
                found[0] = true;
            }

            @Override
            public void visit(ColorTouchingColor node) {
                found[0] = true;
            }

            @Override
            public void visit(DistanceTo node) {
                found[0] = true;
            }

            // Pen
            @Override
            public void visit(PenDownStmt node) {
                found[0] = true;
            }

            @Override
            public void visit(PenUpStmt node) {
                found[0] = true;
            }

            @Override
            public void visit(PenStampStmt node) {
                found[0] = true;
            }

            @Override
            public void visit(SetPenColorParamTo node) {
                found[0] = true;
            }

            @Override
            public void visit(SetPenColorToColorStmt node) {
                found[0] = true;
            }

            @Override
            public void visit(SetPenSizeTo node) {
                found[0] = true;
            }

            @Override
            public void visit(ChangePenColorParamBy node) {
                found[0] = true;
            }

            @Override
            public void visit(ChangePenSizeBy node) {
                found[0] = true;
            }
        }

        script.accept(new SpriteBlockVisitor());
        return found[0];
    }

    private boolean hasStageOnlyBlocks(Script script) {
        final boolean[] found = {false};
        script.accept(new ScratchVisitor() {
            @Override
            public void visit(StageClicked node) {
                found[0] = true;
            }

            @Override
            public void visit(SwitchBackdropAndWait node) {
                found[0] = true;
            }
        });
        return found[0];
    }

    private Program findProgram(Script script) {
        var node = script.getParentNode();
        while (node != null) {
            if (node instanceof Program program) {
                return program;
            }
            node = node.getParentNode();
        }
        return null;
    }

    private ActorDefinition findStage(Program program) {
        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            if (actor.isStage()) {
                return actor;
            }
        }
        return null;
    }

    private ActorDefinition findFirstSprite(Program program) {
        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            if (actor.isSprite()) {
                return actor;
            }
        }
        return null;
    }

    private ActorDefinition findActorByName(Program program, String name) {
        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            if (actor.getIdent().getName().equals(name)) {
                return actor;
            }
        }
        return null;
    }
}
