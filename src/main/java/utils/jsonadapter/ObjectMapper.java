package utils.jsonadapter;

public interface ObjectMapper {

    /**
     * Parse a given JSON string into a corresponding
     * tree of JsonNode objects.
     *
     * @param content
     * @return  the tree of JsonNode objects
     */
    JsonNode readTree(String content);

}
