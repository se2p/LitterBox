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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.StopMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ClearSoundEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StartSound;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StopAllSounds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class ActorSoundStmtParserTest implements JsonTest {

    @Test
    public void testProgramStructure() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stmtParser/actorSoundStmts.json");
        ActorDefinitionList list = program.getActorDefinitionList();
        Truth.assertThat(list.getDefinitions().size()).isEqualTo(2);
    }

    @Test
    public void testStmtsInSprite() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stmtParser/actorSoundStmts.json");
        ActorDefinitionList list = program.getActorDefinitionList();
        ActorDefinition sprite = list.getDefinitions().get(1);

        Script script = sprite.getScripts().getScriptList().get(0);
        List<Stmt> listOfStmt = script.getStmtList().getStmts();

        Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(PlaySoundUntilDone.class);
        PlaySoundUntilDone playSoundUntilDone = (PlaySoundUntilDone) listOfStmt.get(0);
        Truth.assertThat(playSoundUntilDone.getMetadata().getClass()).isEqualTo(NonDataBlockMetadata.class);
        Truth.assertThat(playSoundUntilDone.getElementChoice().getClass()).isEqualTo(WithExpr.class);
        WithExpr expr = (WithExpr) playSoundUntilDone.getElementChoice();
        Truth.assertThat(expr.getMetadata().getClass()).isEqualTo(NonDataBlockMetadata.class);
        Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(PlaySoundUntilDone.class);
        Truth.assertThat(listOfStmt.get(1).getClass()).isEqualTo(StartSound.class);
        Truth.assertThat(listOfStmt.get(2).getClass()).isEqualTo(ClearSoundEffects.class);
        Truth.assertThat(listOfStmt.get(3).getClass()).isEqualTo(StopAllSounds.class);
        Truth.assertThat(listOfStmt.get(4).getClass()).isEqualTo(StopAll.class);
        StopAll stop = (StopAll) listOfStmt.get(4);
        Truth.assertThat(stop.getMetadata().getClass()).isEqualTo(NonDataBlockMetadata.class);
        Truth.assertThat(((NonDataBlockMetadata) stop.getMetadata()).getMutation() instanceof StopMutationMetadata);
    }

    @Test
    public void testPlaySoundTilDone() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stmtParser/actorSoundStmts.json");
        ActorDefinitionList list = program.getActorDefinitionList();
        ActorDefinition sprite = list.getDefinitions().get(1);

        Script script = sprite.getScripts().getScriptList().get(0);
        List<Stmt> listOfStmt = script.getStmtList().getStmts();

        Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(PlaySoundUntilDone.class);

        PlaySoundUntilDone playSoundUntilDone = (PlaySoundUntilDone) listOfStmt.get(0);
        WithExpr elementChoice = (WithExpr) playSoundUntilDone.getElementChoice();
        StrId strid = (StrId) elementChoice.getExpression();
        Truth.assertThat(strid.getName()).isEqualTo("Meow");
    }

    @Test
    public void testStartSound() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stmtParser/actorSoundStmts.json");
        ActorDefinitionList list = program.getActorDefinitionList();
        ActorDefinition sprite = list.getDefinitions().get(1);

        Script script = sprite.getScripts().getScriptList().get(0);
        List<Stmt> listOfStmt = script.getStmtList().getStmts();

        Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(PlaySoundUntilDone.class);

        StartSound startSound = (StartSound) listOfStmt.get(1);
        WithExpr elementChoice = (WithExpr) startSound.getElementChoice();
        StrId strid = (StrId) elementChoice.getExpression();
        Truth.assertThat(strid.getName()).isEqualTo("Meow");
    }
}
