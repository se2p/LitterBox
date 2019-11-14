package utils.deserializer.scratch3;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import scratch.data.Sound;

class SoundDeserializer3 {

    /**
     * Deserialize the JSON String and creating a List<Sound> with Sound objects
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<Sound> with Sound objects
     */

    static List<Sound> deserialize(JsonNode rootNode) {
        JsonNode globalSounds = rootNode.path("sounds");
        Iterator<JsonNode> elements = globalSounds.elements();
        List<Sound> sounds = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode sound = elements.next();
            Sound scSound = new Sound();
            scSound.setName(sound.get("name").asText());
            scSound.setAssetId(sound.get("assetId").asText());
            scSound.setDataFormat(sound.get("dataFormat").asText());
            sounds.add(scSound);
        }
        return sounds;
    }
}
