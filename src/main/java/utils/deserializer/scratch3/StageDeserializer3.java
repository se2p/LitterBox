package utils.deserializer.scratch3;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import scratch.data.Comment;
import scratch.data.Costume;
import scratch.data.ScList;
import scratch.data.ScVariable;
import scratch.data.Script;
import scratch.data.Sound;
import scratch.structure.Stage;

public class StageDeserializer3 {

    /**
     * Deserialize the JSON String and creating a Stage object
     * @param rootNode the JsonNode to deserialize
     * @return a Stage object
     */
    public static Stage deserialize(JsonNode rootNode) {
        String name = rootNode.get("name").asText();
        List<Script> scripts = ScriptDeserializer3.deserialize(rootNode);
        List<Comment> comments = CommentDeserializer3.deserialize(rootNode);
        List<ScVariable> variables = VariableListDeserializer3.deserialize(rootNode);
        List<ScList> lists = ListDeserializer3.deserialize(rootNode);
        List<Costume> costumes = CostumeDeserializer3.deserialize(rootNode);
        List<Sound> sounds = SoundDeserializer3.deserialize(rootNode);
        int initCostume = rootNode.get("currentCostume").asInt();

        return new Stage(name, scripts, comments, variables, lists, costumes, sounds, initCostume, rootNode.get("blocks"));
    }
}
