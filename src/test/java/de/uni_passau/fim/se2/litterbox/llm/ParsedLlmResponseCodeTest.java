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

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

class ParsedLlmResponseCodeTest {

    private static final Script DUMMY_SCRIPT = new Script(new GreenFlag(new NoBlockMetadata()), new StmtList());

    @Test
    void mergeSuccessfulScriptsDifferentActors() {
        final var scripts1 = getScripts(Map.of("actor1", List.of("script1", "script2")));
        final ParsedLlmResponseCode response1 = new ParsedLlmResponseCode(scripts1, Collections.emptyMap());
        final var scripts2 = getScripts(Map.of("actor2", List.of("script1", "script2")));
        final ParsedLlmResponseCode response2 = new ParsedLlmResponseCode(scripts2, Collections.emptyMap());

        final ParsedLlmResponseCode expected = new ParsedLlmResponseCode(
                getScripts(Map.of("actor1", List.of("script1", "script2"), "actor2", List.of("script1", "script2"))),
                Collections.emptyMap()
        );

        final ParsedLlmResponseCode merged = response1.merge(response2);
        assertThat(merged).isEqualTo(expected);
    }

    @Test
    void mergeSuccessfulScriptsSameActors() {
        final var scripts1 = getScripts(Map.of("actor1", List.of("script1", "script2")));
        final ParsedLlmResponseCode response1 = new ParsedLlmResponseCode(scripts1, Collections.emptyMap());
        final var scripts2 = getScripts(Map.of("actor1", List.of("script3", "script4")));
        final ParsedLlmResponseCode response2 = new ParsedLlmResponseCode(scripts2, Collections.emptyMap());

        final ParsedLlmResponseCode expected = new ParsedLlmResponseCode(
                getScripts(Map.of("actor1", List.of("script1", "script2", "script3", "script4"))),
                Collections.emptyMap()
        );

        final ParsedLlmResponseCode merged = response1.merge(response2);
        assertThat(merged).isEqualTo(expected);
    }

    @Test
    void mergeFailedScriptsDifferentActors() {
        final var scripts1 = getFailedScripts(Map.of("actor1", List.of("script1", "script2")));
        final ParsedLlmResponseCode response1 = new ParsedLlmResponseCode(Collections.emptyMap(), scripts1);
        final var scripts2 = getFailedScripts(Map.of("actor2", List.of("script1", "script2")));
        final ParsedLlmResponseCode response2 = new ParsedLlmResponseCode(Collections.emptyMap(), scripts2);

        final ParsedLlmResponseCode expected = new ParsedLlmResponseCode(
                Collections.emptyMap(),
                getFailedScripts(
                        Map.of("actor1", List.of("script1", "script2"), "actor2", List.of("script1", "script2"))
                )
        );

        final ParsedLlmResponseCode merged = response1.merge(response2);
        assertThat(merged).isEqualTo(expected);
    }

    @Test
    void mergeFailedScriptsSameActors() {
        final var scripts1 = getFailedScripts(Map.of("actor1", List.of("script1", "script2")));
        final ParsedLlmResponseCode response1 = new ParsedLlmResponseCode(Collections.emptyMap(), scripts1);
        final var scripts2 = getFailedScripts(Map.of("actor1", List.of("script3", "script4")));
        final ParsedLlmResponseCode response2 = new ParsedLlmResponseCode(Collections.emptyMap(), scripts2);

        final ParsedLlmResponseCode expected = new ParsedLlmResponseCode(
                Collections.emptyMap(),
                getFailedScripts(Map.of("actor1", List.of("script1", "script2", "script3", "script4")))
        );

        final ParsedLlmResponseCode merged = response1.merge(response2);
        assertThat(merged).isEqualTo(expected);
    }

    private Map<String, Map<String, ScriptEntity>> getScripts(final Map<String, List<String>> actorsAndScriptIds) {
        final Map<String, Map<String, ScriptEntity>> result = new HashMap<>();

        for (final var entry : actorsAndScriptIds.entrySet()) {
            final Map<String, ScriptEntity> actorMap = new HashMap<>();

            for (final var scriptId : entry.getValue()) {
                actorMap.put(scriptId, DUMMY_SCRIPT);
            }

            result.put(entry.getKey(), actorMap);
        }

        return result;
    }

    private Map<String, Map<String, String>> getFailedScripts(final Map<String, List<String>> actorsAndScriptIds) {
        final Map<String, Map<String, String>> result = new HashMap<>();

        for (final var entry : actorsAndScriptIds.entrySet()) {
            final Map<String, String> actorMap = new HashMap<>();

            for (final var scriptId : entry.getValue()) {
                actorMap.put(scriptId, scriptId);
            }

            result.put(entry.getKey(), actorMap);
        }

        return result;
    }
}
