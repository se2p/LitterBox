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
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        visitedNames.add(node.getIdent().getName());
    }

    private void checkName(String name) {
        for (String standard : SPRITE_LANGUAGES){
            if (name.startsWith(standard)) {
                addIssueWithLooseComment();
                return;
            }
        }
        for (String visitedName : visitedNames) {
            if (name.startsWith(visitedName) && Character.isDigit(name.charAt(name.length() - 1))) {
                addIssueWithLooseComment();
                return;
            }
        }
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
