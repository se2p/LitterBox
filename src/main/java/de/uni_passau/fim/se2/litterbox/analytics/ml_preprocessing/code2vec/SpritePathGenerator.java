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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpritePathGenerator extends PathGenerator {


    public SpritePathGenerator(int maxPathLength, boolean includeStage, Program program) {
        super(maxPathLength, includeStage,  program);
    }

    private static final List<String> DEFAULT_SPRITE_NAMES = Stream.of(
            "Actor", "Ator", "Ciplun", "Duszek", "Figur", "Figura", "Gariņš", "Hahmo", "Kihusika", "Kukla", "Lik",
            "Nhân", "Objeto", "Parehe", "Personaj", "Personatge", "Pertsonaia", "Postava", "Pêlîstik", "Sprait",
            "Sprajt", "Sprayt", "Sprid", "Sprite", "Sprìd", "Szereplő", "Teikning", "Umlingisi", "Veikėjas",
            "Αντικείμενο", "Анагӡаҩ", "Дүрс", "Лик", "Спрайт", "Կերպար", "דמות", "الكائن", "تەن", "شکلک", "สไปรต์",
            "სპრაიტი", "ገፀ-ባህርይ", "តួអង្គ", "スプライト", "角色", "스프라이트"
    ).map(String::toLowerCase).collect(Collectors.toUnmodifiableList());


    @Override
    public void printLeafs() {
        System.out.println("Number of sprites: " + leafsMap.keySet().size());
        for (Map.Entry<ActorDefinition, List<ASTNode>> entry : leafsMap.entrySet()) {
            String actorName = entry.getKey().getIdent().getName();
            System.out.println("Actor Definition: " + actorName);
            System.out.println("Number of ASTLeafs for " + actorName + ": " + entry.getValue().size());
            int i = 0;
            for (ASTNode value : entry.getValue()) {
                System.out.println(i + " Leaf (Test): " + StringUtil.getToken(value));
                i++;
            }
        }
    }

    @Override
    public List<ProgramFeatures> generatePaths() {
        List<ProgramFeatures> spriteFeatures = new ArrayList<>();
        for (Map.Entry<ActorDefinition, List<ASTNode>> entry : leafsMap.entrySet()) {
            ActorDefinition actor = entry.getKey();
            List<ASTNode> leafs = entry.getValue();
            ProgramFeatures singleSpriteFeatures = generatePathsForSprite(actor, leafs);
            if (singleSpriteFeatures != null && !singleSpriteFeatures.isEmpty()) {
                spriteFeatures.add(singleSpriteFeatures);
            }
        }
        return spriteFeatures;
    }


    private ProgramFeatures generatePathsForSprite(final ActorDefinition sprite, final List<ASTNode> leafs) {
        String spriteName = normalizeSpriteName(sprite.getIdent().getName());
        if (spriteName == null) {
            return null;
        }
        return getProgramFeatures(spriteName, leafs);
    }

    private static String normalizeSpriteName(String spriteName) {
        String normalizedSpriteLabel = StringUtil.normalizeName(spriteName);
        if (normalizedSpriteLabel.isEmpty() || isDefaultName(normalizedSpriteLabel)) {
            return null;
        }

        List<String> splitNameParts = StringUtil.splitToSubtokens(spriteName);
        String splitName = normalizedSpriteLabel;
        if (!splitNameParts.isEmpty()) {
            splitName = String.join("|", splitNameParts);
        }

        return splitName;
    }

    private static boolean isDefaultName(String normalizedSpriteLabel) {
        return DEFAULT_SPRITE_NAMES.contains(normalizedSpriteLabel);
    }
}
