/*
 * Copyright (C) 2020 LitterBox contributors
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;

public class JSONReportGenerator implements ReportGenerator {

    private OutputStream outputStream = null;

    private boolean closeStream = false;

    public JSONReportGenerator(String fileName) throws IOException {
        outputStream = new FileOutputStream(fileName);
        closeStream = true;
    }

    public JSONReportGenerator(OutputStream stream) throws IOException {
        this.outputStream = stream;
    }

    @Override
    public void generateReport(Program program, Collection<Issue> issues) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createArrayNode();

        for (Issue issue : issues) {
            JsonNode childNode = mapper.createObjectNode();
            ((ObjectNode) childNode).put("finder", issue.getTranslatedFinderName());
            ((ObjectNode) childNode).put("type", issue.getFinderType());
            ((ObjectNode) childNode).put("sprite", issue.getActorName());
            ((ObjectNode) childNode).put("hint", issue.getHint());
            ArrayNode arrayNode = ((ObjectNode) childNode).putArray("costumes");
            ActorMetadata actorMetadata = issue.getActor().getActorMetadata();
            for (ImageMetadata image : actorMetadata.getCostumes().getList()) {
                arrayNode.add(image.getAssetId());
            }
            ((ObjectNode) childNode).put("currentCostume", actorMetadata.getCurrentCostume());

            ASTNode location = issue.getScriptOrProcedureDefinition();
            if (location == null) {
                String emptyScript = ScratchBlocksVisitor.SCRATCHBLOCKS_START + System.lineSeparator() +
                        ScratchBlocksVisitor.SCRATCHBLOCKS_END + System.lineSeparator();
                ((ObjectNode) childNode).put("code", emptyScript);
            } else {
                ScratchBlocksVisitor blockVisitor = new ScratchBlocksVisitor(issue);
                blockVisitor.begin();
                location.accept(blockVisitor);
                blockVisitor.end();
                String scratchBlockCode = blockVisitor.getScratchBlocks();
                ((ObjectNode) childNode).put("code", scratchBlockCode);
            }
            ((ArrayNode) rootNode).add(childNode);
        }

        String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        final PrintStream printStream = new PrintStream(outputStream);
        printStream.print(jsonString);
        if (closeStream) {
            outputStream.close();
        }
    }
}
