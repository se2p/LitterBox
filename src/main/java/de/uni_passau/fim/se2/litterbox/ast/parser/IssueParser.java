package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueTool;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.ISSUES_KEY;

public class IssueParser {

    public Set<Issue> parseFile(File fileEntry) throws IOException, ParsingException {
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

    private Set<Issue> parseJsonFile(File fileEntry) throws IOException, ParsingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(fileEntry);
        Preconditions.checkNotNull(rootNode);
        if (!rootNode.has(ISSUES_KEY)) {
            throw new ParsingException("The JSON File does not have an issues field.");
        }
        Iterator<JsonNode> iterable = rootNode.get(ISSUES_KEY).iterator();
        Map<String, IssueFinder> issueFinders = IssueTool.generateBugFinders();
    }
}
