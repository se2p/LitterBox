import static org.junit.Assert.assertEquals;

import analytics.IssueReport;
import analytics.finder.RaceCondition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Stage;
import utils.Identifier;
import utils.Version;

public class RaceConditionTest {

    @Test
    public void validateCheck() {
        Project project = new Project();
        project.setVersion(Version.SCRATCH3);
        List<Script> scripts = new ArrayList<>();
        List<ScBlock> blocks = new ArrayList<>();
        Script script = new Script();
        Script script2 = new Script();
        ScBlock block1 = new ScBlock();
        ScBlock block2 = new ScBlock();
        block1.setContent(Identifier.GREEN_FLAG.getValue());
        block2.setContent(Identifier.SET_VAR.getValue());
        Map<String, List<String>> fields = new HashMap<>();
        fields.put(Identifier.FIELD_VARIABLE.getValue(), Collections.singletonList("variable1"));
        block2.setFields(fields);
        blocks.add(block1);
        blocks.add(block2);
        script.setBlocks(blocks);
        script2.setBlocks(blocks);
        double[] pos = {1.0, 1.0};
        double[] pos2 = {2.0, 2.0};
        script.setPosition(pos);
        script2.setPosition(pos2);
        scripts.add(script);
        scripts.add(script2);
        Stage stage = new Stage("Stage", scripts, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0, null);
        project.setStage(stage);
        project.setSprites(new ArrayList<>());
        project.setPath("Test");
        RaceCondition detector = new RaceCondition();
        IssueReport iR = detector.check(project);

        assertEquals(1, iR.getCount());
    }
}
