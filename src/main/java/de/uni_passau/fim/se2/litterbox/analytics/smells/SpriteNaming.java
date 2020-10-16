/*
 * Copyright (C) 2020 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.ArrayList;
import java.util.List;

/**
 * This finder looks if a sprite name has an uncommunicative name.
 * This is the case if the standard name from Scratch is used or Sprites are simply iterated.
 */
public class SpriteNaming extends AbstractIssueFinder {
    public static final String NAME = "sprite_naming";
    private List<String> visitedNames;

    private static final String[] SPRITE_LANGUAGES = {"Actor", "Ator", "Ciplun", "Duszek", "Figur", "Figura", "Gariņš",
            "Hahmo", "Kihusika", "Kukla", "Lik", "Nhân", "Objeto", "Parehe", "Personaj", "Personatge", "Pertsonaia",
            "Postava", "Pêlîstik", "Sprait", "Sprajt", "Sprayt", "Sprid", "Sprite", "Sprìd", "Szereplő", "Teikning",
            "Umlingisi", "Veikėjas", "Αντικείμενο", "Анагӡаҩ", "Дүрс", "Лик", "Спрайт", "Կերպար", "דמות", "الكائن",
            "تەن", "شکلک", "สไปรต์", "სპრაიტი", "ገፀ-ባህርይ", "តួអង្គ", "スプライト", "角色", "스프라이트"};

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
        String trimmedName = name;
        while (trimmedName.length() > 0 && (Character.isDigit(trimmedName.charAt(trimmedName.length() - 1))
                || Character.isWhitespace(trimmedName.charAt(trimmedName.length() - 1)))) {
            trimmedName = trimmedName.substring(0, trimmedName.length() - 1);
        }

        for (String standard : SPRITE_LANGUAGES) {
            if (trimmedName.equals(standard)) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.HINT_SPRITE, name);
                addIssueWithLooseComment(hint);
                visitedNames.add(trimmedName);
                return;
            }
        }
        for (String visitedName : visitedNames) {
            if (trimmedName.equals(visitedName)) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.HINT_SPRITE, name);
                addIssueWithLooseComment(hint);
                visitedNames.add(trimmedName);
                return;
            }
        }
        visitedNames.add(trimmedName);
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return "sprite_naming";
    }
}
