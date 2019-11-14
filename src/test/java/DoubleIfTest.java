import static org.junit.Assert.assertEquals;

import analytics.IssueReport;
import analytics.finder.DoubleIf;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Stage;
import utils.Identifier;
import utils.Version;

public class DoubleIfTest {

    @Test
    public void validateCheck() {
        Project project = new Project();
        project.setVersion(Version.SCRATCH3);
        List<Script> scripts = new ArrayList<>();
        List<ScBlock> blocks = new ArrayList<>();
        Script script = new Script();
        ScBlock block1 = new ScBlock();
        ScBlock block2 = new ScBlock();
        block1.setContent(Identifier.IF.getValue());
        block2.setContent(Identifier.IF.getValue());
        block1.setCondition("condition");
        block2.setCondition("condition");
        blocks.add(block1);
        blocks.add(block2);
        script.setBlocks(blocks);
        double[] pos = {1.0, 1.0};
        script.setPosition(pos);
        scripts.add(script);
        Stage stage = new Stage("Stage", scripts, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0, null);
        project.setStage(stage);
        project.setSprites(new ArrayList<>());
        project.setPath("Test");
        DoubleIf detector = new DoubleIf();
        IssueReport iR = detector.check(project);

        assertEquals(1, iR.getCount());
        assertEquals("Stage at [1.0, 1.0]", iR.getPosition().get(0));
    }
}
