import analytics.IssueReport;
import analytics.finder.AttributeModification;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Stage;
import utils.Identifier;
import utils.Version;

import java.util.*;

public class AttributeModificationTest {

    @Test
    public void validateCheck() {
        Project project = new Project();
        project.setVersion(Version.SCRATCH3);
        List<Script> scripts = new ArrayList<>();
        List<ScBlock> blocks = new ArrayList<>();
        Script script = new Script();
        ScBlock block1 = new ScBlock();
        ScBlock block2 = new ScBlock();
        block1.setContent(Identifier.CHANGE_VAR.getValue());
        block2.setContent(Identifier.CHANGE_VAR.getValue());
        Map<String, List<String>> fields = new HashMap<>();
        fields.put(Identifier.FIELD_VARIABLE.getValue(), Collections.singletonList("variable1"));
        block1.setFields(fields);
        block2.setFields(fields);
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
        AttributeModification detector = new AttributeModification();
        IssueReport iR = detector.check(project);

        assertEquals(1, iR.getCount());
        assertEquals("Stage at [1.0, 1.0]", iR.getPosition().get(0));
    }
}
