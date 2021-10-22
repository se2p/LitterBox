/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class ControlStmtParserTest implements JsonTest {

    @Test
    public void testProgramStructure() throws IOException, ParsingException {
        assertNumberActorDefinitions(2, "src/test/fixtures/stmtParser/controlStmts.json");
    }

    @Test
    public void testStmtsInSprite() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/stmtParser/controlStmts.json");
        ActorDefinitionList list = program.getActorDefinitionList();
        ActorDefinition sprite = list.getDefinitions().get(1);

        Script script = sprite.getScripts().getScriptList().get(0);
        List<Stmt> listOfStmt = script.getStmtList().getStmts();

        Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(IfThenStmt.class);
        IfThenStmt ifthen = (IfThenStmt) listOfStmt.get(0);
        Truth.assertThat(ifthen.getMetadata().getClass()).isEqualTo(NonDataBlockMetadata.class);

        Truth.assertThat(listOfStmt.get(1).getClass()).isEqualTo(IfElseStmt.class);
        IfElseStmt ifelse = (IfElseStmt) listOfStmt.get(1);
        Truth.assertThat(ifelse.getMetadata().getClass()).isEqualTo(NonDataBlockMetadata.class);

        Truth.assertThat(listOfStmt.get(2).getClass()).isEqualTo(RepeatTimesStmt.class);
        RepeatTimesStmt times = (RepeatTimesStmt) listOfStmt.get(2);
        Truth.assertThat(times.getMetadata().getClass()).isEqualTo(NonDataBlockMetadata.class);

        Truth.assertThat(listOfStmt.get(3).getClass()).isEqualTo(UntilStmt.class);
        UntilStmt until = (UntilStmt) listOfStmt.get(3);
        Truth.assertThat(until.getMetadata().getClass()).isEqualTo(NonDataBlockMetadata.class);

        Truth.assertThat(listOfStmt.get(4).getClass()).isEqualTo(RepeatForeverStmt.class);
        RepeatForeverStmt forever = (RepeatForeverStmt) listOfStmt.get(4);
        Truth.assertThat(forever.getMetadata().getClass()).isEqualTo(NonDataBlockMetadata.class);
    }
}
