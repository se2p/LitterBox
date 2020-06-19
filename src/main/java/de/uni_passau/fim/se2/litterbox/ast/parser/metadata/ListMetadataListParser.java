package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ListMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.ListMetadataList;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.DECLARATION_LIST_NAME_POS;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.DECLARATION_LIST_VALUES_POS;

public class ListMetadataListParser {
    public static ListMetadataList parse(JsonNode listsNode) {
        List<ListMetadata> listMetadataList = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> entries = listsNode.fields();
        while (entries.hasNext()) {
            Map.Entry<String, JsonNode> current = entries.next();
            JsonNode valuesNode = current.getValue().get(DECLARATION_LIST_VALUES_POS);
            Preconditions.checkArgument(valuesNode instanceof ArrayNode);
            ArrayNode valuesArray = (ArrayNode) valuesNode;
            List<String> valuesList = new ArrayList<>();
            for (int i = 0; i < valuesArray.size(); i++) {
                valuesList.add(valuesArray.get(i).asText());
            }
            listMetadataList.add(new ListMetadata(current.getKey(),
                    current.getValue().get(DECLARATION_LIST_NAME_POS).asText(),
                    valuesList));
        }
        return new ListMetadataList(listMetadataList);
    }
}
