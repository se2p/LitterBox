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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageNeverReceived;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

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

    private final MessageNeverReceived relatedBugFinder = new MessageNeverReceived();

    public MessageNeverReceivedFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    protected void resetState() {
        message = null;
        actorScriptsPerMessage = new LinkedHashMap<>();
        buggyBroadcast = null;
    }

    @Override
    public Set<FixResult> check(Program program) {
        Set<FixResult> results = super.check(program);
        if (!results.isEmpty()) {
            return results;
        }

        if (buggyBroadcast != null && message != null) {
            if (actorScriptsPerMessage.containsKey(message)) {
                addFix(buggyBroadcast, buggyBroadcast.getMetadata());
            }
        }
        return Collections.unmodifiableSet(fixes);
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
            String msg = text.getText();
            if (actorScriptsPerMessage.containsKey(msg)) {
                Map<ActorDefinition, List<Script>> actorScripts = actorScriptsPerMessage.get(msg);
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
                actorScriptsPerMessage.put(msg, actorScripts);
            }
        }

        visitChildren(node);
    }
}
