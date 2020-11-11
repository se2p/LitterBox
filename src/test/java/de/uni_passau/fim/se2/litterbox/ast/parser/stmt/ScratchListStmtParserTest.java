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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.UnspecifiedId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ScratchListStmtParserTest implements JsonTest {

    private Program program;

    @BeforeEach
    public void setup() throws IOException, ParsingException {
        program = getAST("src/test/fixtures/listBlocks.json");
    }

    @Test
    public void testAddToGlobal() {
        final ActorDefinition sprite = program.getActorDefinitionList().getDefinitions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(0);

        final Stmt stmt = script.getStmtList().getStmts().get(0);
        Truth.assertThat(stmt).isInstanceOf(AddTo.class);

        final AddTo addTo = (AddTo) stmt;
        Truth.assertThat(((StringLiteral) addTo.getString()).getText()).isEqualTo("thing");
        Truth.assertThat(((Qualified) addTo.getIdentifier()).getFirst().getName()).isEqualTo("Stage");
        Truth.assertThat(((Qualified) addTo.getIdentifier()).getSecond().getName().getName()).isEqualTo("TestList");
    }

    @Test
    public void testAddToWithMissingListId() throws Exception {
        Program program = getAST("src/test/fixtures/stmtParser/missingId.json");
        final ActorDefinition actorDefinition = program.getActorDefinitionList().getDefinitions().get(1);
        final Script script = actorDefinition.getScripts().getScriptList().get(0);
        Stmt stmt = script.getStmtList().getStmts().get(0);
        Truth.assertThat(stmt).isInstanceOf(AddTo.class);
        Identifier identifier = ((AddTo) stmt).getIdentifier();
        Truth.assertThat(identifier).isInstanceOf(UnspecifiedId.class);
    }

    @Test
    public void testAddToLocal() {
        final ActorDefinition sprite = program.getActorDefinitionList().getDefinitions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(1);

        final Stmt stmt = script.getStmtList().getStmts().get(1);
        Truth.assertThat(stmt).isInstanceOf(AddTo.class);

        final AddTo addTo = (AddTo) stmt;
        Truth.assertThat(((StringLiteral) addTo.getString()).getText()).isEqualTo("localThing");
        Truth.assertThat(((Qualified) addTo.getIdentifier()).getFirst().getName()).isEqualTo("Sprite1");
        Truth.assertThat(((Qualified) addTo.getIdentifier()).getSecond().getName().getName()).isEqualTo("TestListLocal");
    }

    @Test
    public void testInsertGlobal() {
        final ActorDefinition sprite = program.getActorDefinitionList().getDefinitions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(0);

        final Stmt stmt = script.getStmtList().getStmts().get(3);
        Truth.assertThat(stmt).isInstanceOf(InsertAt.class);

        final InsertAt insertAt = (InsertAt) stmt;
        Truth.assertThat(((StringLiteral) insertAt.getString()).getText()).isEqualTo("thing");
        Truth.assertThat(((Qualified) insertAt.getIdentifier()).getFirst().getName()).isEqualTo("Stage");
        Truth.assertThat(((Qualified) insertAt.getIdentifier()).getSecond().getName().getName()).isEqualTo(
                "TestList");
    }

    @Test
    public void testReplaceItem() {
        final ActorDefinition sprite = program.getActorDefinitionList().getDefinitions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(0);

        final Stmt stmt = script.getStmtList().getStmts().get(4);
        Truth.assertThat(stmt).isInstanceOf(ReplaceItem.class);

        final ReplaceItem insertAt = (ReplaceItem) stmt;
        Truth.assertThat(((StringLiteral) insertAt.getString()).getText()).isEqualTo("thing2");
        Truth.assertThat(((NumberLiteral) insertAt.getIndex()).getValue()).isEqualTo(1);
        Truth.assertThat(((Qualified) insertAt.getIdentifier()).getFirst().getName()).isEqualTo("Stage");
        Truth.assertThat(((Qualified) insertAt.getIdentifier()).getSecond().getName().getName()).isEqualTo("TestList");
    }

    @Test
    public void testDeleteOf() {
        final ActorDefinition sprite = program.getActorDefinitionList().getDefinitions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(0);

        final Stmt stmt = script.getStmtList().getStmts().get(5);
        Truth.assertThat(stmt).isInstanceOf(DeleteOf.class);

        final DeleteOf deleteOf = (DeleteOf) stmt;
        Truth.assertThat(((NumberLiteral) deleteOf.getNum()).getValue()).isEqualTo(1);
        Truth.assertThat(((Qualified) deleteOf.getIdentifier()).getFirst().getName()).isEqualTo("Stage");
        Truth.assertThat(((Qualified) deleteOf.getIdentifier()).getSecond().getName().getName()).isEqualTo("TestList");
    }

    @Test
    public void testDeleteAll() {
        final ActorDefinition sprite = program.getActorDefinitionList().getDefinitions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(0);

        final Stmt stmt = script.getStmtList().getStmts().get(6);
        Truth.assertThat(stmt).isInstanceOf(DeleteAllOf.class);

        final DeleteAllOf deleteAllOf = (DeleteAllOf) stmt;
        Truth.assertThat(((Qualified) deleteAllOf.getIdentifier()).getFirst().getName()).isEqualTo("Stage");
        Truth.assertThat(((Qualified) deleteAllOf.getIdentifier()).getSecond().getName().getName()).isEqualTo("TestList");
    }

    @Test
    public void testAddInvalid() throws Exception {
        Program program = getAST("src/test/fixtures/stmtParser/invalidList.json");
        final ActorDefinition actorDefinition = program.getActorDefinitionList().getDefinitions().get(1);
        final Script script = actorDefinition.getScripts().getScriptList().get(0);
        Stmt stmt = script.getStmtList().getStmts().get(0);
        Truth.assertThat(stmt).isInstanceOf(AddTo.class);
        Identifier identifier = ((AddTo) stmt).getIdentifier();
        Truth.assertThat(identifier).isInstanceOf(UnspecifiedId.class);
    }

    @Test
    public void testDeleteInvalid() throws Exception {
        Program program = getAST("src/test/fixtures/stmtParser/invalidList.json");
        final ActorDefinition actorDefinition = program.getActorDefinitionList().getDefinitions().get(1);
        final Script script = actorDefinition.getScripts().getScriptList().get(0);
        Stmt stmt = script.getStmtList().getStmts().get(2);
        Truth.assertThat(stmt).isInstanceOf(DeleteAllOf.class);
        Identifier identifier = ((DeleteAllOf) stmt).getIdentifier();
        Truth.assertThat(identifier).isInstanceOf(UnspecifiedId.class);
    }

    @Test
    public void testInsertInvalid() throws Exception {
        Program program = getAST("src/test/fixtures/stmtParser/invalidList.json");
        final ActorDefinition actorDefinition = program.getActorDefinitionList().getDefinitions().get(1);
        final Script script = actorDefinition.getScripts().getScriptList().get(0);
        Stmt stmt = script.getStmtList().getStmts().get(3);
        Truth.assertThat(stmt).isInstanceOf(InsertAt.class);
        Identifier identifier = ((InsertAt) stmt).getIdentifier();
        Truth.assertThat(identifier).isInstanceOf(UnspecifiedId.class);
    }
}
