import analytics.IssueReport;
import analytics.finder.DuplicatedScript;
import analytics.finder.DuplicatedSprite;
import org.junit.Test;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Sprite;
import scratch.structure.Stage;
import utils.Identifier;
import utils.Version;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class DuplicatedSpriteTest {

    @Test
    public void validateCheck() {
        Project project = new Project();
        project.setVersion(Version.SCRATCH3);
        List<Script> scripts = new ArrayList<>();
        List<ScBlock> blocks = new ArrayList<>();
        Script script = new Script();
        ScBlock block1 = new ScBlock();
        block1.setContent(Identifier.RECEIVE.getValue());
        Map<String, List<String>> fields = new HashMap<>();
        fields.put(Identifier.FIELD_RECEIVE.getValue(), Collections.singletonList("variable1"));
        block1.setFields(fields);
        blocks.add(block1);
        blocks.add(block1);
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
        DuplicatedSprite detector = new DuplicatedSprite();
        IssueReport iR = detector.check(project);

        assertEquals(1, iR.getCount());
    }
}
