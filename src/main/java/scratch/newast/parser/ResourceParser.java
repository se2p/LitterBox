package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import scratch.newast.ParsingException;
import scratch.newast.model.URI;
import scratch.newast.model.resource.ImageResource;
import scratch.newast.model.resource.Resource;
import scratch.newast.model.resource.SoundResource;
import scratch.newast.model.variable.Identifier;

public class ResourceParser {
    private final static String NAME = "name";
    private final static String MD5EXT = "md5ext";

    public static List<Resource> parseSound(JsonNode resourceNode) {
        Preconditions.checkNotNull(resourceNode);
        List<Resource> parsedRessources = new ArrayList<>();
        Iterator<JsonNode> iter = resourceNode.elements();
        while (iter.hasNext()) {
            JsonNode node = iter.next();
            SoundResource res = new SoundResource(new Identifier(node.get(NAME).textValue()),
                    new URI(node.get(MD5EXT).textValue()));
            parsedRessources.add(res);
        }
        return parsedRessources;
    }

    public static List<Resource> parseCostume(JsonNode resourceNode) {
        Preconditions.checkNotNull(resourceNode);
        List<Resource> parsedRessources = new ArrayList<>();
        Iterator<JsonNode> iter = resourceNode.elements();
        while (iter.hasNext()) {
            JsonNode node = iter.next();
            ImageResource res = null;
            try {
                res = new ImageResource(new Identifier(node.get(NAME).textValue()),
                    getURI(node));
            } catch (ParsingException e) {
                Logger.getGlobal().warning(e.getMessage());
                continue;
            }
            parsedRessources.add(res);
        }
        return parsedRessources;
    }

    private static URI getURI(JsonNode node) throws ParsingException {
        //TODO this works but is not really beautiful
        if (node.has(MD5EXT)) {
            return new URI(node.get(MD5EXT).textValue());
        } else if (node.has("assetId") && node.has("dataFormat")) {
            String assetId = node.get("assetId").asText();
            String dataFormat = node.get("dataFormat").asText();
            String fileName = assetId + "." + dataFormat;
            return new URI(fileName);
        } else {
            throw new ParsingException("Cannot parse URI of resource node " + node.textValue());
        }
    }
}
