import static org.junit.Assert.assertEquals;

import analytics.IssueReport;
import analytics.finder.LongScript;
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

public class LongScriptTest {

    @Test
    public void validateCheck() {
        Project project = new Project();
        project.setVersion(Version.SCRATCH3);
        List<Script> scripts = new ArrayList<>();
        List<ScBlock> blocks = new ArrayList<>();
        Script script = new Script();
        ScBlock block1 = new ScBlock();
        block1.setContent(Identifier.SENSE.getValue());
        for (int i = 0; i < 12; i++) {
            blocks.add(block1);
        }
        script.setBlocks(blocks);
        double[] pos = {1.0, 1.0};
        script.setPosition(pos);
        scripts.add(script);
        Stage stage = new Stage("Stage", scripts, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0, null);
        project.setStage(stage);
        List<Sprite> sprites = new ArrayList<>();
        project.setSprites(sprites);
        project.setPath("Test");
        LongScript detector = new LongScript();
        IssueReport iR = detector.check(project);

        assertEquals(1, iR.getCount());
    }
}
