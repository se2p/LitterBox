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
package de.uni_passau.fim.se2.embedded_kittens.shared;

import java.util.Optional;
import java.util.function.Function;

import de.uni_passau.fim.se2.embedded_kittens.util.NodeNameUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;

@FunctionalInterface
public interface ActorNameNormalizer extends Function<ActorDefinition, Optional<String>> {

    Optional<String> normalizeName(ActorDefinition actor);

    @Override
    default Optional<String> apply(final ActorDefinition actor) {
        return normalizeName(actor);
    }

    static ActorNameNormalizer getDefault() {
        return NodeNameUtil::normalizeSpriteName;
    }
}
