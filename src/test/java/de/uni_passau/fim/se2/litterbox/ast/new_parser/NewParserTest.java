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
package de.uni_passau.fim.se2.litterbox.ast.new_parser;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.ast.util.StructuralEquality;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class NewParserTest {
    private final NewParser parser = new NewParser();

    private final Scratch3Parser originalParser = new Scratch3Parser();

    @Test
    @Disabled("no longer AST-identical due to bugfixes")
    void parseFixtures() throws IOException {
        final Path fixtureDir = Path.of("src", "test", "fixtures");
        try (var files = Files.walk(fixtureDir).filter(p -> p.getFileName().toString().endsWith(".json"))) {
            assertAll(files.map(projectJson -> () -> parseProjectJson(projectJson)));
        }
    }

    private void parseProjectJson(Path projectJson) {
        final Program originalResult;
        try {
            originalResult = originalParser.parseFile(projectJson.toFile());
        } catch (Exception originalParserException) {
            // okay, both old and new parser fail
            return;
        }

        try {
            try {
                final Program project = parser.parseJsonFile(projectJson.toFile());
                assertNotNull(project);
                checkStructurallyEqual(originalResult, project);
            } catch (ParsingException e) {
                fail("Failed to parse project " + projectJson, e);
            }
        } catch (IOException e) {
            fail("Could not write result.", e);
        }
    }

    private void checkStructurallyEqual(final Program original, final Program fromNewParser) throws IOException {
        final boolean areEqual = StructuralEquality.areStructurallyEqual(original, fromNewParser);

        if (!areEqual) {
            System.out.println("Considering program " + original.getIdent().getName());

            for (final ActorDefinition actor : original.getActorDefinitionList().getDefinitions()) {
                final ActorDefinition actorNew = fromNewParser.getActorDefinitionList().getDefinitions().stream()
                        .filter(a -> a.getIdent().getName().equals(actor.getIdent().getName()))
                        .findFirst()
                        .orElseThrow();

                final boolean areEqualActors = StructuralEquality.areStructurallyEqual(actor, actorNew);
                if (!areEqualActors) {
                    System.out.printf("Actors %s are not equal\n", actorNew.getIdent().getName());

                    if (!StructuralEquality.areStructurallyEqual(actor.getDecls(), actorNew.getDecls())) {
                        System.out.println("diff in declarations");
                    }
                    if (!StructuralEquality.areStructurallyEqual(actor.getSetStmtList(), actorNew.getSetStmtList())) {
                        System.out.println("diff in set stmts");
                    }
                    if (!StructuralEquality.areStructurallyEqual(actor.getProcedureDefinitionList(), actorNew.getProcedureDefinitionList())) {
                        System.out.println("diff in procedure definitions");
                    }
                    if (!StructuralEquality.areStructurallyEqual(actor.getScripts(), actorNew.getScripts())) {
                        System.out.println("diff in scripts");
                    }
                }
            }
        }

        assertTrue(areEqual);
    }
}
