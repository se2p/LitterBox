package de.uni_passau.fim.se2.litterbox.analytics.pqGram;

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

public class PQGramProfileUtilTest implements JsonTest {
    @Test
    public void testPQGramCreationForEmpty() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/emptyProject.json");
        PQGramProfile profile = PQGramProfileUtil.createPQProfile(empty);
        List<Label> anc = new ArrayList<>();
        anc.add(new Label(PQGramProfileUtil.NULL_NODE));
        anc.add(new Label(Program.class.getSimpleName()));
        List<Label> sib = new ArrayList<>();
        sib.add(new Label(PQGramProfileUtil.NULL_NODE));
        sib.add(new Label(PQGramProfileUtil.NULL_NODE));
        sib.add(new Label(StrId.class.getSimpleName()));
        LabelTuple tuple = new LabelTuple(anc, sib);
        Assertions.assertTrue(profile.getTuples().contains(tuple));
    }

    @Test
    public void testPQGramCreationForMoveStmt() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/metrics/allMotionBlocks.json");
        PQGramProfile profile = PQGramProfileUtil.createPQProfile(empty);
        List<Label> anc = new ArrayList<>();
        anc.add(new Label(Script.class.getSimpleName()));
        anc.add(new Label(StmtList.class.getSimpleName()));
        List<Label> sib = new ArrayList<>();
        sib.add(new Label(PQGramProfileUtil.NULL_NODE));
        sib.add(new Label(PQGramProfileUtil.NULL_NODE));
        sib.add(new Label(MoveSteps.class.getSimpleName()));
        LabelTuple tuple = new LabelTuple(anc, sib);
        Assertions.assertTrue(profile.getTuples().contains(tuple));
        anc = new ArrayList<>();
        anc.add(new Label(ScriptList.class.getSimpleName()));
        anc.add(new Label(Script.class.getSimpleName()));
        sib = new ArrayList<>();
        sib.add(new Label(PQGramProfileUtil.NULL_NODE));
        sib.add(new Label(PQGramProfileUtil.NULL_NODE));
        sib.add(new Label(Never.class.getSimpleName()));
        tuple = new LabelTuple(anc, sib);
        Assertions.assertTrue(profile.getTuples().contains(tuple));
    }

    @Test
    public void testSameProgramDistance() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/metrics/allMotionBlocks.json");
        PQGramProfile profile = PQGramProfileUtil.createPQProfile(empty);
        Assertions.assertEquals(0, PQGramProfileUtil.calculateDistance(profile, profile));
    }

    @Test
    public void testDifferentScripts() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/metrics/allMotionBlocks.json");
        List<Script> scripts = empty.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList();
        PQGramProfile profile0 = PQGramProfileUtil.createPQProfile(scripts.get(0));
        PQGramProfile profile1 = PQGramProfileUtil.createPQProfile(scripts.get(1));
        Assertions.assertTrue(0.5 < PQGramProfileUtil.calculateDistance(profile0, profile1));
    }
}
