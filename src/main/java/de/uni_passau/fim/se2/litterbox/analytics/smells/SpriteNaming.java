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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.*;

/**
 * This finder looks if a sprite name has an uncommunicative name.
 * This is the case if the standard name from Scratch is used or Sprites are simply iterated.
 */
public class SpriteNaming extends AbstractIssueFinder {
    public static final String NAME = "sprite_naming";
    public static final String HINT_DEFAULT = "sprite_naming_default";
    private List<String> visitedNames;

    @Override
    public void visit(Program node) {
        visitedNames = new ArrayList<>();
        super.visit(node);
    }

    @Override
    public void visit(ActorDefinition node) {
        currentActor = node;
        checkName(node.getIdent().getName());
    }

    private void checkName(String name) {
        final String trimmedName = trimName(name);

        boolean isDefaultName = Constants.DEFAULT_SPRITE_NAMES.contains(trimmedName.toLowerCase(Locale.ROOT));
        if (isDefaultName) {
            Hint hint = Hint.fromKey(HINT_DEFAULT);
            hint.setParameter(Hint.HINT_SPRITE, name);
            addIssueWithLooseComment(hint);
            visitedNames.add(trimmedName);
            return;
        }

        for (String visitedName : visitedNames) {
            if (trimmedName.equals(visitedName)) {
                Hint hint = Hint.fromKey(getName());
                hint.setParameter(Hint.HINT_SPRITE, name);
                addIssueWithLooseComment(hint);
                visitedNames.add(trimmedName);
                return;
            }
        }
        visitedNames.add(trimmedName);
    }

    /**
     * Removes leading whitespace, and trailing whitespace/number combinations.
     *
     * @param name A sprite name.
     * @return The trimmed sprite name.
     */
    private String trimName(String name) {
        return name.trim().replaceAll("[\\d\\s]+$", "");
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

        String firstName = first.getActorName();
        String secondName = other.getActorName();

        return trimName(firstName).equals(trimName(secondName));
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return "sprite_naming";
    }

    @Override
    public Collection<String> getHintKeys() {
        return Arrays.asList(NAME, HINT_DEFAULT);
    }
}
