package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import scratch.newast.model.resource.Resource;

public class ResourceParser {
    private final static String NAME = "name";
    private final static String ND5EXT ="md5ext";
    public static List<Resource> parse(JsonNode resourceNode) {
        List<Resource> parsedRessources = new ArrayList<>();
        Iterator<JsonNode> iter= resourceNode.elements();
        while(iter.hasNext()){
            
        }
        throw new RuntimeException("NotImplemented");
    }
}
