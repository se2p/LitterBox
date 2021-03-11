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
package de.uni_passau.fim.se2.litterbox.analytics.pqgram;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PQGramProfileTest implements JsonTest {
    @Test
    public void testPQGramCreationForEmpty() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/emptyProject.json");
        PQGramProfile profile = new PQGramProfile(empty);
        List<Label> anc = new ArrayList<>();
        anc.add(new Label(PQGramProfile.NULL_NODE));
        anc.add(new Label(Program.class.getSimpleName()));
        List<Label> sib = new ArrayList<>();
        sib.add(new Label(PQGramProfile.NULL_NODE));
        sib.add(new Label(PQGramProfile.NULL_NODE));
        sib.add(new Label(StrId.class.getSimpleName()));
        LabelTuple tuple = new LabelTuple(anc, sib);
        Assertions.assertTrue(profile.getTuples().contains(tuple));
    }

    @Test
    public void testPQGramCreationForMoveStmt() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/metrics/allMotionBlocks.json");
        PQGramProfile profile = new PQGramProfile(empty);
        List<Label> anc = new ArrayList<>();
        anc.add(new Label(Script.class.getSimpleName()));
        anc.add(new Label(StmtList.class.getSimpleName()));
        List<Label> sib = new ArrayList<>();
        sib.add(new Label(PQGramProfile.NULL_NODE));
        sib.add(new Label(PQGramProfile.NULL_NODE));
        sib.add(new Label(MoveSteps.class.getSimpleName()));
        LabelTuple tuple = new LabelTuple(anc, sib);
        Assertions.assertTrue(profile.getTuples().contains(tuple));
        anc = new ArrayList<>();
        anc.add(new Label(ScriptList.class.getSimpleName()));
        anc.add(new Label(Script.class.getSimpleName()));
        sib = new ArrayList<>();
        sib.add(new Label(PQGramProfile.NULL_NODE));
        sib.add(new Label(PQGramProfile.NULL_NODE));
        sib.add(new Label(Never.class.getSimpleName()));
        tuple = new LabelTuple(anc, sib);
        Assertions.assertTrue(profile.getTuples().contains(tuple));
    }

    @Test
    public void testSameProgramDistance() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/metrics/allMotionBlocks.json");
        PQGramProfile profile = new PQGramProfile(empty);
        Assertions.assertEquals(0, profile.calculateDistanceTo(profile));
    }

    @Test
    public void testDifferentScripts() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/metrics/allMotionBlocks.json");
        List<Script> scripts = empty.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList();
        PQGramProfile profile0 = new PQGramProfile(scripts.get(0));
        PQGramProfile profile1 = new PQGramProfile(scripts.get(1));
        Assertions.assertTrue(0.5 < profile0.calculateDistanceTo(profile1));
    }
}
