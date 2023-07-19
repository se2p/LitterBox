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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.tokenizer;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;

import java.util.List;

public final class TokenSequenceBuilder {
    private TokenSequenceBuilder() {
        throw new IllegalCallerException("utility class");
    }

    public static TokenSequence build(final String label, final List<List<String>> tokens) {
        final List<String> labelSubtokens = StringUtil.splitToNormalisedSubtokens(label, "|");
        final List<List<String>> subTokens = tokens.stream().map(TokenSequenceBuilder::asSubTokenSequence).toList();
        return new TokenSequence(label, labelSubtokens, tokens, subTokens);
    }

    private static List<String> asSubTokenSequence(final List<String> tokenSequence) {
        return tokenSequence
                .stream()
                .flatMap(tokens -> StringUtil.splitToNormalisedSubtokenStream(tokens, "_"))
                .toList();
    }
}
