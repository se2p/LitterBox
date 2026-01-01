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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingCloneCall;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.WithCloneExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.util.ActorTrackingVisitor;

import java.util.*;

/**
 * Injects the MissingCloneCall bug by removing ALL CreateCloneOf statements
 * for actors that have StartedAsClone event handlers.
 *
 * <p>
 * <b>Bug Pattern:</b> An actor has a "when I start as a clone" handler but is never cloned.
 *
 * <p>
 * <b>Injection Strategy:</b>
 * <ol>
 *   <li>Precondition: The target actor must have a StartedAsClone handler</li>
 *   <li>Remove ALL CreateCloneOf statements that clone that actor (not just one)</li>
 * </ol>
 */
public class MissingCloneCallInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new MissingCloneCall();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(Program node) {
        // Collect all actors with StartedAsClone handlers (precondition)
        Set<String> actorsWithCloneHandlers = collectActorsWithCloneHandlers(node);
        if (actorsWithCloneHandlers.isEmpty()) {
            return;
        }

        // Collect all CreateCloneOf statements grouped by target actor name
        Map<String, List<CreateCloneOf>> clonesByTarget = collectClonesByTarget(node);

        // For each actor that has a StartedAsClone handler AND is cloned,
        // create ONE mutation that removes ALL clone calls for that actor
        for (String actorName : actorsWithCloneHandlers) {
            List<CreateCloneOf> cloneCalls = clonesByTarget.get(actorName.toLowerCase());
            if (cloneCalls != null && !cloneCalls.isEmpty()) {
                modifyClonedNode(cloneCalls.getFirst(), cloned -> removeAllCloneCalls(cloned, actorName));
            }
        }
    }

    private Set<String> collectActorsWithCloneHandlers(Program program) {
        Set<String> actors = new HashSet<>();
        program.accept(new ActorTrackingVisitor() {
            @Override
            public void visit(Script node) {
                if (node.getEvent() instanceof StartedAsClone && !node.getStmtList().getStmts().isEmpty()) {
                    String actorName = getCurrentActorName();
                    if (actorName != null) {
                        actors.add(actorName);
                    }
                }
            }
        });
        return actors;
    }

    private Map<String, List<CreateCloneOf>> collectClonesByTarget(Program program) {
        Map<String, List<CreateCloneOf>> clonesByTarget = new HashMap<>();
        program.accept(new ActorTrackingVisitor() {
            @Override
            public void visit(CreateCloneOf node) {
                String targetName = getCloneTargetName(node, getCurrentActorName());
                if (targetName != null) {
                    clonesByTarget.computeIfAbsent(targetName.toLowerCase(), k -> new ArrayList<>()).add(node);
                }
            }
        });
        return clonesByTarget;
    }

    private String getCloneTargetName(CreateCloneOf node, String currentActorName) {
        if (node.getCloneChoice() instanceof Myself) {
            return currentActorName;
        } else if (node.getCloneChoice() instanceof WithCloneExpr expr
                && expr.getExpression() instanceof StrId strId) {
            String name = strId.getName();
            if ("myself".equalsIgnoreCase(name)) {
                return currentActorName;
            }
            return name;
        }
        return null;
    }

    private void removeAllCloneCalls(CreateCloneOf clonedFirst, String targetActorName) {
        Program clonedProgram = findProgramRoot(clonedFirst);
        if (clonedProgram == null) {
            return;
        }

        clonedProgram.accept(new ActorTrackingVisitor() {
            @Override
            public void visit(StmtList node) {
                List<Stmt> stmts = node.getStmts();
                stmts.removeIf(stmt -> {
                    if (stmt instanceof CreateCloneOf clone) {
                        String target = getCloneTargetName(clone, getCurrentActorName());
                        return target != null && target.equalsIgnoreCase(targetActorName);
                    }
                    return false;
                });
                visitChildren(node);
            }
        });
    }

    @Override
    protected ASTNode findContextNode(ASTNode node) {
        // Force ActorDefinition context to show full scope of changes (which might be cross-script)
        // and to avoid issues with empty scripts.
        ActorDefinition actor = findContainingActor(node);
        if (actor != null) {
            return actor;
        }
        return super.findContextNode(node);
    }
}
