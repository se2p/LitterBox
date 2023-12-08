/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.SpriteClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class MissingCloneInitializationFix extends AbstractIssueFinder {
    public static final String NAME = "missing_clone_initialization_fix";
    private final String bugLocationBlockId;
    private boolean firstRun = false;
    private String clonedActor;
    private boolean insideClonedActor;
    private boolean alreadyFound;

    public MissingCloneInitializationFix(String bugLocationBlockId) {
        this.bugLocationBlockId = bugLocationBlockId;
    }

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        firstRun = true;
        program.accept(this);
        firstRun = false;
        if (clonedActor != null) {
            program.accept(this);
        }
        return Collections.unmodifiableSet(issues);
    }

    @Override
    public void visit(ActorDefinition node) {
        if (!firstRun && node.getIdent().getName().equals(clonedActor)) {
            insideClonedActor = true;
        }
        super.visit(node);
        insideClonedActor = false;
    }

    @Override
    public void visit(CreateCloneOf node) {
        if (firstRun && Objects.equals(AstNodeUtil.getBlockId(node), bugLocationBlockId)) {
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
        if (!firstRun && insideClonedActor && !alreadyFound) {
            alreadyFound = true;
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(SpriteClicked node) {
        if (!firstRun && insideClonedActor && !alreadyFound) {
            alreadyFound = true;
            addIssue(node, node.getMetadata());
        }
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
