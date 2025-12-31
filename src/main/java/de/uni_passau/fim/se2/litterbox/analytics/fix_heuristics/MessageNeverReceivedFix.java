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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageNeverReceived;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

import java.util.*;

/**
 * Fix heuristic for the MessageNeverReceived bug.
 *
 * <p>The bug is reported when a broadcast is sent but never received.
 * The bug is fixed if the message is now received.
 */
public class MessageNeverReceivedFix extends AbstractFixHeuristic {
    private String message = null;
    private Map<String, Map<ActorDefinition, List<Script>>> actorScriptsPerMessage;
    private ASTNode buggyBroadcast;

    private final IssueFinder relatedBugFinder = new MessageNeverReceived();

    public MessageNeverReceivedFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }


    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        actorScriptsPerMessage = new LinkedHashMap<>();
        // Check if block deleted
        if (bugLocationBlockId != null && de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder.find(program, bugLocationBlockId).isEmpty()) {
            addIssueWithLooseComment();
            return Collections.unmodifiableSet(issues);
        }

        program.accept(this);

        if (buggyBroadcast != null && message != null) {
            Map<ActorDefinition, List<Script>> actorEvents = actorScriptsPerMessage.get(message);
            if (actorEvents != null) {
                addIssue(buggyBroadcast, buggyBroadcast.getMetadata());
            }
        }
        return Collections.unmodifiableSet(issues);
    }

    @Override
    public void visit(Broadcast node) {
        if (matchesBugLocation(node)) {
            buggyBroadcast = node;
            if (node.getMessage().getMessage() instanceof StringLiteral text) {
                message = text.getText();
            }
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (matchesBugLocation(node)) {
            buggyBroadcast = node;
            if (node.getMessage().getMessage() instanceof StringLiteral text) {
                message = text.getText();
            }
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        if (node.getMsg().getMessage() instanceof StringLiteral text) {
            if (scriptNotEmpty(node.getParentNode())) {
                if (actorScriptsPerMessage.containsKey(text.getText())) {
                    Map<ActorDefinition, List<Script>> actorScripts = actorScriptsPerMessage.get(text.getText());
                    if (actorScripts.containsKey(currentActor)) {
                        actorScripts.get(currentActor).add(currentScript);
                    } else {
                        List<Script> scripts = new ArrayList<>();
                        scripts.add(currentScript);
                        actorScripts.put(currentActor, scripts);
                    }
                } else {
                    List<Script> scripts = new ArrayList<>();
                    scripts.add(currentScript);
                    Map<ActorDefinition, List<Script>> actorScripts = new LinkedHashMap<>();
                    actorScripts.put(currentActor, scripts);
                    actorScriptsPerMessage.put(text.getText(), actorScripts);
                }
            }
        }

        visitChildren(node);
    }

    private boolean scriptNotEmpty(ASTNode parentNode) {
        if (!(parentNode instanceof Script script)) {
            return false;
        }
        return script.getStmtList().hasStatements();
    }
}
