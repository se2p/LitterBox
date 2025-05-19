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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ChangeGraphicEffectBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ChangeSoundEffectBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetSoundEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class CommonStmtParserTest implements JsonTest {

    @Test
    public void testProgramStructure() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stmtParser/commonStmts.json");
        ActorDefinitionList list = program.getActorDefinitionList();
        Truth.assertThat(list.getDefinitions().size()).isEqualTo(2);
    }

    @Test
    public void testStmtsInSprite() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stmtParser/commonStmts.json");
        ActorDefinitionList list = program.getActorDefinitionList();
        ActorDefinition sprite = list.getDefinitions().get(1);

        Script script = sprite.getScripts().getScriptList().get(0);
        List<Stmt> listOfStmt = script.getStmtList().getStmts();

        Truth.assertThat(listOfStmt.get(0)).isInstanceOf(WaitSeconds.class);
        Truth.assertThat(listOfStmt.get(0).getMetadata()).isInstanceOf(NonDataBlockMetadata.class);
        Truth.assertThat(listOfStmt.get(1)).isInstanceOf(WaitUntil.class);
        Truth.assertThat(listOfStmt.get(1).getMetadata()).isInstanceOf(NonDataBlockMetadata.class);
        Truth.assertThat(listOfStmt.get(2)).isInstanceOf(StopOtherScriptsInSprite.class);
        Truth.assertThat(listOfStmt.get(2).getMetadata()).isInstanceOf(NonDataBlockMetadata.class);
        Truth.assertThat(listOfStmt.get(3)).isInstanceOf(CreateCloneOf.class);
        Truth.assertThat(listOfStmt.get(3).getMetadata()).isInstanceOf(NonDataBlockMetadata.class);
        Truth.assertThat(listOfStmt.get(4)).isInstanceOf(Broadcast.class);
        Truth.assertThat(listOfStmt.get(4).getMetadata()).isInstanceOf(NonDataBlockMetadata.class);
        Truth.assertThat(listOfStmt.get(5)).isInstanceOf(BroadcastAndWait.class);
        Truth.assertThat(listOfStmt.get(5).getMetadata()).isInstanceOf(NonDataBlockMetadata.class);
        Truth.assertThat(listOfStmt.get(6)).isInstanceOf(ResetTimer.class);
        Truth.assertThat(listOfStmt.get(6).getMetadata()).isInstanceOf(NonDataBlockMetadata.class);
        Truth.assertThat(listOfStmt.get(7)).isInstanceOf(ChangeVariableBy.class);
        Truth.assertThat(listOfStmt.get(7).getMetadata()).isInstanceOf(NonDataBlockMetadata.class);
        Truth.assertThat(listOfStmt.get(8)).isInstanceOf(ChangeSoundEffectBy.class);
        Truth.assertThat(listOfStmt.get(8).getMetadata()).isInstanceOf(NonDataBlockMetadata.class);
        Truth.assertThat(listOfStmt.get(9)).isInstanceOf(SetSoundEffectTo.class);
        Truth.assertThat(listOfStmt.get(9).getMetadata()).isInstanceOf(NonDataBlockMetadata.class);
        Truth.assertThat(listOfStmt.get(10)).isInstanceOf(ChangeGraphicEffectBy.class);
        Truth.assertThat(listOfStmt.get(10).getMetadata()).isInstanceOf(NonDataBlockMetadata.class);
        Truth.assertThat(listOfStmt.get(11)).isInstanceOf(DeleteClone.class);
        Truth.assertThat(listOfStmt.get(11).getMetadata()).isInstanceOf(NonDataBlockMetadata.class);
    }
}
