/**
 * Copyright (C) 2019 LitterBox contributors
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
import analytics.IssueReport;
import analytics.finder.BroadcastSync;
import analytics.finder.VariableScope;
import org.junit.Test;
import scratch.data.ScBlock;
import scratch.data.ScVariable;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Sprite;
import scratch.structure.Stage;
import utils.Identifier;
import utils.Version;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class VariableScopeTest {

    @Test
    public void validateCheck() {
        Project project = new Project();
        project.setVersion(Version.SCRATCH3);
        List<Script> scripts = new ArrayList<>();
        List<ScBlock> blocks = new ArrayList<>();
        Script script = new Script();
        ScBlock block1 = new ScBlock();
        ScBlock block2 = new ScBlock();
        block1.setContent(Identifier.RECEIVE.getValue());
        block2.setContent(Identifier.BROADCAST.getValue());
        Map<String, List<String>> fields = new HashMap<>();
        fields.put(Identifier.FIELD_VARIABLE.getValue(), Collections.singletonList("variable1"));
        block1.setFields(fields);
        Map<String, List<String>> inputs = new HashMap<>();
        List<String> in = new ArrayList<>();
        in.add("blank");
        in.add("blank");
        in.add("variable2");
        inputs.put(Identifier.FIELD_BROADCAST.getValue(), in);
        block2.setInputs(inputs);
        blocks.add(block1);
        blocks.add(block2);
        script.setBlocks(blocks);
        double[] pos = {1.0, 1.0};
        script.setPosition(pos);
        scripts.add(script);
        Stage stage = new Stage("Stage", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0, null);
        ScVariable var = new ScVariable();
        var.setName("variable1");
        stage.setVariables(Collections.singletonList(var));
        project.setStage(stage);
        Sprite sprite = new Sprite("Sprite1", scripts, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0, null, pos, 0, "90",1);
        List<Sprite> sprites = new ArrayList<>();
        sprites.add(sprite);
        project.setSprites(sprites);
        project.setPath("Test");
        VariableScope detector = new VariableScope();
        IssueReport iR = detector.check(project);

        assertEquals(1, iR.getCount());
    }
}
