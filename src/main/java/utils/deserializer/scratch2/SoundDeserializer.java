package utils.deserializer.scratch2;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.data.Sound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * More information about the JSON Scratch 2 file format and its JSON arrays and nodes:
 * https://en.scratch-wiki.info/wiki/Scratch_File_Format
 */
class SoundDeserializer {

    /**
     * Deserialize the JSON String and creating a List<Sound> with Sound objects
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<Sound> with Sound objects
     */

    static List<Sound> deserialize(JsonNode rootNode) {
        JsonNode globalVariables = rootNode.path("sounds");
        Iterator<JsonNode> elements = globalVariables.elements();
        List<Sound> sounds = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode variable = elements.next();
            Sound scSound = new Sound();
            scSound.setName(variable.get("soundName").asText());
            scSound.setAssetId(variable.get("soundID").asText());
            sounds.add(scSound);
        }
        return sounds;
    }
}
