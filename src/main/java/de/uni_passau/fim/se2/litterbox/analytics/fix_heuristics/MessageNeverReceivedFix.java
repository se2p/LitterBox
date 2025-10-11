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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

public class MessageNeverReceivedFix extends AbstractIssueFinder {
    public static final String NAME = "message_never_received_fix";
    private final String bugLocationBlockId;
    private String message = null;
    private Map<String, Map<ActorDefinition, List<Script>>> actorScriptsPerMessage;

    public MessageNeverReceivedFix(String bugLocationBlockId) {
        this.bugLocationBlockId = bugLocationBlockId;
    }

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        actorScriptsPerMessage = new LinkedHashMap<>();
        program.accept(this);

        if (message != null) {
            Map<ActorDefinition, List<Script>> actorEvents = actorScriptsPerMessage.get(message);
            if (actorEvents != null) {
                Map.Entry<ActorDefinition, List<Script>> entry = actorEvents.entrySet().iterator().next();
                ActorDefinition actor = entry.getKey();
                List<Script> scripts = entry.getValue();
                currentActor = actor;
                Script script = scripts.getFirst();
                currentScript = script;
                addIssue(script.getEvent(), script.getEvent().getMetadata());
            }
        }
        return Collections.unmodifiableSet(issues);
    }

    @Override
    public void visit(Broadcast node) {
        if (AstNodeUtil.hasBlockId(node, bugLocationBlockId)) {
            if (node.getMessage().getMessage() instanceof StringLiteral text) {
                message = text.getText();
            }
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (AstNodeUtil.hasBlockId(node, bugLocationBlockId)) {
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
        assert (parentNode instanceof Script);
        return ((Script) parentNode).getStmtList().hasStatements();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.FIX;
    }
}
