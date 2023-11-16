package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class IssueParser {

    public Map<String, List<String>> parseFile(File fileEntry) throws IOException, ParsingException {
        String fileName = fileEntry.getName();
        if (PropertyLoader.getSystemBooleanProperty("parser.log_file_name")) {
            Logger.getGlobal().info("Now parsing issue report: " + fileName);
        }

        if ((FilenameUtils.getExtension(fileName)).equalsIgnoreCase("json")) {
            return parseJsonFile(fileEntry);
        } else {
            throw new ParsingException("This file type is not supported.");
        }
    }

    private Map<String, List<String>> parseJsonFile(File fileEntry) throws IOException, ParsingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(fileEntry);
        Preconditions.checkNotNull(rootNode);
        if (!rootNode.has(ISSUES_KEY)) {
            throw new ParsingException("The JSON File does not have an issues field.");
        }
        Iterator<JsonNode> iterable = rootNode.get(ISSUES_KEY).iterator();
        Map<String, List<String>> issues = new LinkedHashMap<>();
        while (iterable.hasNext()) {
            JsonNode currentIssue = iterable.next();
            if (currentIssue.get(ISSUE_TYPE_KEY).asText().equals(IssueType.BUG.toString())) {
                String name = currentIssue.get(NAME_KEY).asText();
                String block_id = currentIssue.get(ISSUE_BLOCK_ID).asText();
                if (issues.containsKey(name)) {
                    issues.get(name).add(block_id);
                } else {
                    List<String> newBlockIdList = new ArrayList<>();
                    newBlockIdList.add(block_id);
                    issues.put(name, newBlockIdList);
                }
            }
        }
        return issues;
    }
}
