import static org.junit.Assert.assertEquals;

import analytics.IssueReport;
import analytics.finder.LaggyMovement;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Sprite;
import scratch.structure.Stage;
import utils.Identifier;
import utils.Version;

public class LaggyMovementTest {

    @Test
    public void validateCheck() {
        Project project = new Project();
        project.setVersion(Version.SCRATCH3);
        List<Script> scripts = new ArrayList<>();
        List<ScBlock> blocks = new ArrayList<>();
        Script script = new Script();
        ScBlock block1 = new ScBlock();
        block1.setContent(Identifier.KEYPRESS.getValue());
        ScBlock block2 = new ScBlock();
        block2.setContent(Identifier.FORWARD.getValue());
        blocks.add(block1);
        blocks.add(block2);
        script.setBlocks(blocks);
        double[] pos = {1.0, 1.0};
        script.setPosition(pos);
        scripts.add(script);
        Stage stage = new Stage("Stage", scripts, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0, null);
        project.setStage(stage);
        Sprite sprite = new Sprite("Sprite1", scripts, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0, null, pos, 0, "90",1);
        List<Sprite> sprites = new ArrayList<>();
        sprites.add(sprite);
        project.setSprites(sprites);
        project.setPath("Test");
        LaggyMovement detector = new LaggyMovement();
        IssueReport iR = detector.check(project);

        assertEquals(1, iR.getCount());
    }
}
