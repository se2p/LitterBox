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
package de.uni_passau.fim.se2.litterbox.ast.util;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NodeNameUtilTest {

    @ParameterizedTest
    @MethodSource("getDefaultNameActors")
    void testHasDefaultName(final ActorDefinition actor, final boolean hasDefaultName) {
        assertEquals(
                hasDefaultName,
                NodeNameUtil.hasDefaultName(actor),
                () -> String.format(
                        "Expecting '%s' to be a default name: %b",
                        actor.getIdent().getName(),
                        hasDefaultName
                )
        );
    }

    private static Stream<Arguments> getDefaultNameActors() {
        return Stream.of(
                Arguments.of(buildActor("Sprite1"), true),
                Arguments.of(buildActor("Sprite13461278"), true),
                Arguments.of(buildActor("sprite_23"), false),
                Arguments.of(buildActor(""), false),
                Arguments.of(buildActor("Figur"), true),
                Arguments.of(buildActor("Figur2"), true),
                Arguments.of(buildActor("Αντικείμενο"), true),
                Arguments.of(buildActor("αντικείμενο"), true),
                Arguments.of(buildActor("αντικείμενο123"), true)
        );
    }

    private static ActorDefinition buildActor(final String name) {
        final var actorId = new StrId(name);
        final var decls = new DeclarationStmtList(Collections.emptyList());
        final var setStmts = new SetStmtList(Collections.emptyList());
        final var procDefs = new ProcedureDefinitionList(Collections.emptyList());
        final var scripts = new ScriptList(Collections.emptyList());
        final var metadata = new ActorMetadata(
                new CommentMetadataList(Collections.emptyList()),
                0,
                new ImageMetadataList(Collections.emptyList()),
                new SoundMetadataList(Collections.emptyList())
        );

        return new ActorDefinition(ActorType.getSprite(), actorId, decls, setStmts, procDefs, scripts, metadata);
    }

}
