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
package de.uni_passau.fim.se2.litterbox.llm;

import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record ParsedLlmResponseCode(
        Map<String, Map<String, ScriptEntity>> scripts,
        Map<String, Map<String, String>> parseFailedScripts
) {
    /**
     * Returns all scripts for the given actor.
     *
     * @param actor The name of the sprite/actor.
     * @return All scripts of this actor.
     */
    @NonNull
    public Map<String, ScriptEntity> actor(final String actor) {
        return Objects.requireNonNullElse(scripts.get(actor), Collections.emptyMap());
    }

    /**
     * Finds a script in the set of parsed scripts.
     *
     * @param actor    The name of the sprite/actor the script is in.
     * @param scriptId The block ID of the head block of the script.
     * @return The script if it could be found, {@code null} otherwise.
     */
    @Nullable
    public ScriptEntity script(final String actor, final String scriptId) {
        return actor(actor).get(scriptId);
    }

    public ParsedLlmResponseCode merge(final ParsedLlmResponseCode other) {
        final Map<String, Map<String, ScriptEntity>> parsedScripts = deepClone(scripts());
        for (final var entry : other.scripts().entrySet()) {
            if (parsedScripts.containsKey(entry.getKey())) {
                parsedScripts.get(entry.getKey()).putAll(entry.getValue());
            } else {
                parsedScripts.put(entry.getKey(), entry.getValue());
            }
        }

        final Map<String, Map<String, String>> parseFailedScripts = deepClone(parseFailedScripts());
        for (final var entry : other.parseFailedScripts().entrySet()) {
            if (parseFailedScripts.containsKey(entry.getKey())) {
                parseFailedScripts.get(entry.getKey()).putAll(entry.getValue());
            } else {
                parseFailedScripts.put(entry.getKey(), entry.getValue());
            }
        }

        return new ParsedLlmResponseCode(parsedScripts, parseFailedScripts);
    }

    private static <A, B, C> Map<A, Map<B, C>> deepClone(final Map<A, Map<B, C>> map) {
        final Map<A, Map<B, C>> result = new HashMap<>();

        for (final var outerEntry : map.entrySet()) {
            result.put(outerEntry.getKey(), new HashMap<>(outerEntry.getValue()));
        }

        return result;
    }
}
