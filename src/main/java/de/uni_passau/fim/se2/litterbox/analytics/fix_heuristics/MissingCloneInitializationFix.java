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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.SpriteClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

public class MissingCloneInitializationFix extends AbstractIssueFinder {
    public static final String NAME = "missing_clone_initialization_fix";
    private final String bugLocationBlockId;
    private String clonedActor;
    private Map<String, Map<ActorDefinition, List<Script>>> actorScriptsPerActorName;

    public MissingCloneInitializationFix(String bugLocationBlockId) {
        this.bugLocationBlockId = bugLocationBlockId;
    }

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        actorScriptsPerActorName = new LinkedHashMap<>();
        program.accept(this);
        if (clonedActor != null) {
            Map<ActorDefinition, List<Script>> actorEvents = actorScriptsPerActorName.get(clonedActor);
            if (actorEvents != null) {
                Map.Entry<ActorDefinition, List<Script>> entry = actorEvents.entrySet().iterator().next();
                ActorDefinition actor = entry.getKey();
                List<Script> scripts = entry.getValue();
                currentActor = actor;
                Script script = scripts.get(0);
                currentScript = script;
                addIssue(script.getEvent(), script.getEvent().getMetadata());
            }
        }
        return Collections.unmodifiableSet(issues);
    }

    @Override
    public void visit(CreateCloneOf node) {
        if (AstNodeUtil.hasBlockId(node, bugLocationBlockId)) {
            if (node.getStringExpr() instanceof AsString asString && asString.getOperand1() instanceof StrId strId) {
                final String spriteName = strId.getName();
                if (spriteName.equals("_myself_")) {
                    clonedActor = currentActor.getIdent().getName();
                } else {
                    clonedActor = spriteName;
                }
            }
        }
    }

    @Override
    public void visit(StartedAsClone node) {
        if (scriptNotEmpty(node.getParentNode())) {
            addScriptToMap();
        }
    }

    private void addScriptToMap() {
        if (actorScriptsPerActorName.containsKey(currentActor.getIdent().getName())) {
            String actorName = currentActor.getIdent().getName();
            Map<ActorDefinition, List<Script>> actorScripts = actorScriptsPerActorName.get(actorName);
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
            actorScriptsPerActorName.put(currentActor.getIdent().getName(), actorScripts);
        }
    }

    @Override
    public void visit(SpriteClicked node) {
        addScriptToMap();
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
