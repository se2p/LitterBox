/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.embedded_kittens.tokenizer;

import de.uni_passau.fim.se2.embedded_kittens.util.StringUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TokenSequenceBuilder {

    private static final Set<String> SPECIAL_TOKENS = Token.getSpecialTokens()
            .stream()
            .map(Token::getStrRep)
            .collect(Collectors.toUnmodifiableSet());

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
            .flatMap(TokenSequenceBuilder::splitToken)
            .toList();
    }

    private static Stream<String> splitToken(final String token) {
        if (SPECIAL_TOKENS.contains(token)) {
            return Stream.of(token);
        } else {
            return StringUtil.splitToNormalisedSubtokenStream(token, "_");
        }
    }
}
