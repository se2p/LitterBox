package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;
import scratch.newast.model.URI;
import scratch.newast.model.resource.ImageResource;
import scratch.newast.model.resource.Resource;
import scratch.newast.model.resource.SoundResource;
import scratch.newast.model.variable.Identifier;

public class ResourceParser {
    private final static String NAME = "name";
    private final static String MD5EXT ="md5ext";

    public static List<Resource> parseSound(JsonNode resourceNode) {
        Preconditions.checkNotNull(resourceNode);
        List<Resource> parsedRessources = new ArrayList<>();
        Iterator<JsonNode> iter= resourceNode.elements();
        while(iter.hasNext()){
            JsonNode node =iter.next();
            SoundResource res = new SoundResource(new Identifier(node.get(NAME).asText()),new URI(node.get(MD5EXT).asText()));
        }
        return parsedRessources;
    }

    public static List<Resource> parseCostume(JsonNode resourceNode) {
        Preconditions.checkNotNull(resourceNode);
        List<Resource> parsedRessources = new ArrayList<>();
        Iterator<JsonNode> iter= resourceNode.elements();
        while(iter.hasNext()){
            JsonNode node =iter.next();
            SoundResource res = new SoundResource(new Identifier(node.get(NAME).asText()),new URI(node.get(MD5EXT).asText()));
        }
        return parsedRessources;
    }
}
