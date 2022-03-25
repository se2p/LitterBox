/*
 * Copyright (C) 2019-2022 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class JSONReportGenerator extends JSONGenerator implements ReportGenerator {

    private OutputStream outputStream;

    private boolean closeStream = false;

    public JSONReportGenerator(String fileName) throws IOException {
        outputStream = new FileOutputStream(fileName);
        closeStream = true;
    }

    public JSONReportGenerator(OutputStream stream) {
        this.outputStream = stream;
    }

    private void addDuplicateIDs(ArrayNode jsonNode, Issue theIssue, Collection<Issue> issues) {
        issues.stream().filter(issue -> issue != theIssue)
                .filter(theIssue::isDuplicateOf)
                .map(Issue::getId)
                .forEach(jsonNode::add);
    }

    private void addSubsumingIssueIDs(ArrayNode jsonNode, Issue theIssue, Collection<Issue> issues) {
        issues.stream().filter(issue -> issue != theIssue)
                .filter(theIssue::isSubsumedBy)
                .map(Issue::getId)
                .forEach(jsonNode::add);
    }

    private void addCoupledIssueIDs(ArrayNode jsonNode, Issue theIssue, Collection<Issue> issues) {
        issues.stream().filter(issue -> issue != theIssue)
                .filter(theIssue::areCoupled)
                .map(Issue::getId)
                .forEach(jsonNode::add);
    }

    private void addSimilarIssueIDs(ObjectMapper mapper, ArrayNode jsonNode, Issue theIssue, Collection<Issue> issues) {
        Map<Issue, Integer> issuesWithDistance = new HashMap<>();
        issues.stream().filter(issue -> issue != theIssue)
                .filter(issue -> theIssue.getFinder() == issue.getFinder())
                .forEach(issue -> issuesWithDistance.put(issue, theIssue.getDistanceTo(issue))
                );

        issuesWithDistance.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .limit(10)
                .forEach(issue -> {
                    ObjectNode childNode = mapper.createObjectNode();
                    childNode.put("id", issue.getId());
                    childNode.put("distance", theIssue.getDistanceTo(issue));
                    jsonNode.add(childNode);
                });
    }

    @Override
    public void generateReport(Program program, Collection<Issue> issues) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode issueNode = mapper.createArrayNode();
        ObjectNode metricNode = mapper.createObjectNode();

        addMetrics(metricNode, program);
        rootNode.set("metrics", metricNode);

        for (Issue issue : issues) {
            ObjectNode childNode = mapper.createObjectNode();
            childNode.put("id", issue.getId());
            childNode.put("finder", issue.getFinderName());
            childNode.put("name", issue.getTranslatedFinderName());
            childNode.put("type", issue.getIssueType().toString());
            childNode.put("severity", issue.getSeverity().getSeverityLevel());
            childNode.put("sprite", issue.getActorName());

            ArrayNode duplicateNode = childNode.putArray("duplicate-of");
            addDuplicateIDs(duplicateNode, issue, issues);

            ArrayNode subsumedNode = childNode.putArray("subsumed-by");
            addSubsumingIssueIDs(subsumedNode, issue, issues);

            ArrayNode coupledNode = childNode.putArray("coupled-to");
            addCoupledIssueIDs(coupledNode, issue, issues);

            ArrayNode similarNode = childNode.putArray("similar-to");
            addSimilarIssueIDs(mapper, similarNode, issue, issues);

            childNode.put("hint", issue.getHint());
            ArrayNode arrayNode = childNode.putArray("costumes");
            ActorMetadata actorMetadata = issue.getActor().getActorMetadata();
            for (ImageMetadata image : actorMetadata.getCostumes().getList()) {
                arrayNode.add(image.getAssetId());
            }
            childNode.put("currentCostume", actorMetadata.getCurrentCostume());

            ASTNode location = issue.getScriptOrProcedureDefinition();
            if (location == null) {
                String emptyScript = ScratchBlocksVisitor.SCRATCHBLOCKS_START + System.lineSeparator()
                        + ScratchBlocksVisitor.SCRATCHBLOCKS_END + System.lineSeparator();
                childNode.put("code", emptyScript);
            } else {
                ScratchBlocksVisitor blockVisitor = new ScratchBlocksVisitor(issue);
                blockVisitor.begin();
                location.accept(blockVisitor);
                blockVisitor.end();
                String scratchBlockCode = blockVisitor.getScratchBlocks();
                childNode.put("code", scratchBlockCode);
            }
            issueNode.add(childNode);
        }
        rootNode.set("issues", issueNode);

        String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        final PrintStream printStream = new PrintStream(outputStream);
        printStream.print(jsonString);
        if (closeStream) {
            outputStream.close();
        }
    }
}
