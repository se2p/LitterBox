package utils.jsonadapter;

import java.util.Iterator;
import java.util.Map;

public interface JsonNode {

    int asInt();

    String asText();

    JsonNode get(int var1);

    JsonNode get(String fieldName);

    Iterator<Map.Entry<String, JsonNode>> fields();
}
