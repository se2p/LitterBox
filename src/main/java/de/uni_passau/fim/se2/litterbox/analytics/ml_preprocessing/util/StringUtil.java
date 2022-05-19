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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec.TokenVisitor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {
    private StringUtil() {
    }

    public static String normalizeName(String original) {
        original = original.toLowerCase().replaceAll("\\\\n", "") // escaped new lines
                .replaceAll("//s+", "") // whitespaces
                .replaceAll("[\"',]", "") // quotes, apostrophies, commas
                .replaceAll("\\P{Print}", ""); // unicode weird characters
        String stripped = original.replaceAll("[^A-Za-z]", "");
        if (stripped.length() == 0) {
            return "";
        } else {
            return stripped;
        }
    }

    public static List<String> splitToSubtokens(String str1) {
        String str2 = str1.trim();
        return Stream.of(str2.split("(?<=[a-z])(?=[A-Z])|_|-|[0-9]|(?<=[A-Z])(?=[A-Z][a-z])|\\s+"))
                .filter(s -> s.length() > 0).map(StringUtil::normalizeName)
                .filter(s -> s.length() > 0).collect(Collectors.toCollection(ArrayList::new));
    }

    public static String replaceSpecialCharacters(final String label) {
        if (label == null || label.isBlank()) {
            return "blank";
        } else {
            return label.replaceAll("[^a-zA-Z0-9\\s|]", "|").trim();
        }
    }

    /**
     * Retrieve the actual literal represented by a node.
     *
     * @param node A node of the AST.
     * @return The literal value of the given node.
     */
    public static String getToken(final ASTNode node) {
        TokenVisitor visitor = new TokenVisitor();
        node.accept(visitor);
        return visitor.getToken();
    }
}
