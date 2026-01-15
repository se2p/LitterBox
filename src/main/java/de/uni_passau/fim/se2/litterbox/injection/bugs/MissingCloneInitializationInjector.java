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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingCloneInitialization;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.WithCloneExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.event.SpriteClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Injects the MissingCloneInitialization bug by removing StartedAsClone and SpriteClicked
 * handlers from sprites that are cloned.
 *
 * <p>
 * The MissingCloneInitialization bug occurs when a sprite is cloned but has no
 * StartedAsClone or SpriteClicked event handlers to initialize the clone.
 *
 * <p>
 * Precondition: The sprite must actually be cloned (have a CreateCloneOf targeting it).
 */
public class MissingCloneInitializationInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new MissingCloneInitialization();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(Program node) {
        // Collect all cloned actor names (precondition)
        Set<String> clonedActors = collectClonedActors(node);
        if (clonedActors.isEmpty()) {
            return;
        }

        // Visit actors to find those that are cloned AND have clone handlers
        node.accept(new ScratchVisitor() {
            @Override
            public void visit(ActorDefinition actor) {
                String actorName = actor.getIdent().getName();
                if (!clonedActors.contains(actorName.toLowerCase())) {
                    return; // Not cloned, skip
                }

                boolean hasCloneHandlers = actor.getScripts().getScriptList().stream()
                        .anyMatch(script -> script.getEvent() instanceof StartedAsClone
                                || script.getEvent() instanceof SpriteClicked);

                if (hasCloneHandlers) {
                    // Find first clone handler script to blame
                    String blameScriptId = "";
                    for (Script s : actor.getScripts().getScriptList()) {
                        if (s.getEvent() instanceof StartedAsClone || s.getEvent() instanceof SpriteClicked) {
                            String id = AstNodeUtil.getBlockId(s);
                            if (id != null) {
                                blameScriptId = id;
                                break;
                            }
                        }
                    }
                    modifyClonedNode(actor, MissingCloneInitializationInjector.this::removeCloneHandlers,
                            blameScriptId);
                }
            }
        });
    }

    /**
     * Collects all actor names that are cloned (case-insensitive).
     */
    private Set<String> collectClonedActors(Program program) {
        Set<String> cloned = new HashSet<>();
        program.accept(new ScratchVisitor() {
            private String currentActorName = null;

            @Override
            public void visit(ActorDefinition node) {
                currentActorName = node.getIdent().getName();
                visitChildren(node);
                currentActorName = null;
            }

            @Override
            public void visit(CreateCloneOf node) {
                if (node.getCloneChoice() instanceof Myself && currentActorName != null) {
                    cloned.add(currentActorName.toLowerCase());
                } else if (node.getCloneChoice() instanceof WithCloneExpr expr
                        && expr.getExpression() instanceof StrId strId) {
                    cloned.add(strId.getName().toLowerCase());
                }
            }
        });
        return cloned;
    }

    /**
     * Removes all StartedAsClone and SpriteClicked handlers by replacing them with
     * unreachable ReceptionOfMessage handlers.
     */
    private void removeCloneHandlers(ActorDefinition clonedActor) {
        List<Script> scripts = clonedActor.getScripts().getScriptList();
        for (int i = 0; i < scripts.size(); i++) {
            Script script = scripts.get(i);
            if (script.getEvent() instanceof StartedAsClone || script.getEvent() instanceof SpriteClicked) {
                // Replace with unreachable message handler
                ReceptionOfMessage newEvent = new ReceptionOfMessage(
                        new Message(
                                new StringLiteral(
                                        "_dead_code_" + UUID.randomUUID()
                                )
                        ),
                        TopNonDataBlockMetadata.emptyTopNonBlockMetadata()
                );
                Script newScript = new Script(newEvent, script.getStmtList());
                scripts.set(i, newScript);
            }
        }
    }
}

