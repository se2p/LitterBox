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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class EmptySprite extends AbstractIssueFinder {
    public static final String NAME = "empty_sprite";
    private Set<Issue> issues = new LinkedHashSet<>();
    private Program program = null;

    @Override
    public Set<Issue> check(Program program) {
        issues = new LinkedHashSet<>();
        Preconditions.checkNotNull(program);
        this.program = program;
        program.accept(this);
        return issues;
    }

    @Override
    public void visit(ActorDefinition actor) {
        if (actor.getProcedureDefinitionList().getList().isEmpty() && actor.getScripts().getScriptList().isEmpty()
                && !actor.isStage()) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.HINT_SPRITE, actor.getIdent().getName());
            issues.add(new Issue(this, IssueSeverity.LOW, program, actor, null, null, null, hint));
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public void setIgnoreLooseBlocks(boolean value) {
        // Irrelevant for this finder
    }

    @Override
    public Collection<String> getHintKeys() {
        // Default: Only one key with the name of the finder
        return Arrays.asList(getName());
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            // Don't check against self
            return false;
        }

        if (first.getFinder() != other.getFinder()) {
            // Can only be a duplicate if it's the same finder
            return false;
        }

        //empty sprites are always the same as they have nothing that separates them
        return true;
    }
}
