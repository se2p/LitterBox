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

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class JSONReportGenerator extends JSONGenerator implements ReportGenerator {

    private OutputStream outputStream;

    private boolean closeStream = false;

    private JsonFactory jfactory = new JsonFactory();

    private JsonGenerator jsonGenerator;

    private static final boolean INCLUDE_SIMILARITY = PropertyLoader.getSystemBoolProperty("json.similarity");

    private static final boolean INCLUDE_SUBSUMPTION = PropertyLoader.getSystemBoolProperty("json.subsumption");

    private static final boolean INCLUDE_DUPLICATES = PropertyLoader.getSystemBoolProperty("json.duplicates");

    private static final boolean INCLUDE_COUPLING = PropertyLoader.getSystemBoolProperty("json.coupling");

    public JSONReportGenerator(String fileName) throws IOException {
        outputStream = new FileOutputStream(fileName);
        jsonGenerator = jfactory.createGenerator(outputStream, JsonEncoding.UTF8);
        jsonGenerator.useDefaultPrettyPrinter();
        closeStream = true;
    }

    public JSONReportGenerator(OutputStream stream) throws IOException {
        this.outputStream = stream;
        jsonGenerator = jfactory.createGenerator(outputStream, JsonEncoding.UTF8);
        jsonGenerator.useDefaultPrettyPrinter();
    }

    private void addDuplicateIDs(Issue theIssue, Collection<Issue> issues) {
        issues.stream().filter(issue -> issue != theIssue)
                .filter(theIssue::isDuplicateOf)
                .map(Issue::getId)
                .forEach(id -> {try { jsonGenerator.writeNumber(id); } catch (IOException e) { throw new RuntimeException(e); }});
    }

    private void addSubsumingIssueIDs(Issue theIssue, Collection<Issue> issues) {
        issues.stream().filter(issue -> issue != theIssue)
                .filter(theIssue::isSubsumedBy)
                .map(Issue::getId)
                .forEach(id -> {try { jsonGenerator.writeNumber(id); } catch (IOException e) { throw new RuntimeException(e); }});
    }

    private void addCoupledIssueIDs(Issue theIssue, Collection<Issue> issues) {
        issues.stream().filter(issue -> issue != theIssue)
                .filter(theIssue::areCoupled)
                .map(Issue::getId)
                .forEach(id -> {try { jsonGenerator.writeNumber(id); } catch (IOException e) { throw new RuntimeException(e);}});
    }

    private void addSimilarIssueIDs(Issue theIssue, Collection<Issue> issues) {
        issues.stream().filter(issue -> issue != theIssue)
                .filter(issue -> theIssue.getFinder() == issue.getFinder())
                .collect(Collectors.toMap(issue -> issue, theIssue::getDistanceTo))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .forEach(issue -> {
                    try {
                        jsonGenerator.writeStartObject();
                        jsonGenerator.writeNumberField("id", issue.getId());
                        jsonGenerator.writeNumberField("distance", theIssue.getDistanceTo(issue));
                        jsonGenerator.writeEndObject();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public void generateReport(Program program, Collection<Issue> issues) throws IOException {

        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName("metrics");
        jsonGenerator.writeStartObject();
        addMetrics(jsonGenerator, program);
        jsonGenerator.writeEndObject();

        jsonGenerator.writeFieldName("issues");
        jsonGenerator.writeStartArray();

        for (Issue issue : issues) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberField("id", issue.getId());
            jsonGenerator.writeStringField("finder", issue.getFinderName());
            jsonGenerator.writeStringField("name", issue.getTranslatedFinderName());
            jsonGenerator.writeStringField("type", issue.getIssueType().toString());
            jsonGenerator.writeNumberField("severity", issue.getSeverity().getSeverityLevel());
            jsonGenerator.writeStringField("sprite", issue.getActorName());

            if (INCLUDE_DUPLICATES) {
                jsonGenerator.writeFieldName("duplicate-of");
                jsonGenerator.writeStartArray();
                addDuplicateIDs(issue, issues);
                jsonGenerator.writeEndArray();
            }

            if (INCLUDE_SUBSUMPTION) {
                jsonGenerator.writeFieldName("subsumed-by");
                jsonGenerator.writeStartArray();
                addSubsumingIssueIDs(issue, issues);
                jsonGenerator.writeEndArray();
            }

            if (INCLUDE_COUPLING) {
                jsonGenerator.writeFieldName("coupled-to");
                jsonGenerator.writeStartArray();
                addCoupledIssueIDs(issue, issues);
                jsonGenerator.writeEndArray();
            }

            if (INCLUDE_SIMILARITY) {
                jsonGenerator.writeFieldName("similar-to");
                jsonGenerator.writeStartArray();
                addSimilarIssueIDs(issue, issues);
                jsonGenerator.writeEndArray();
            }

            jsonGenerator.writeStringField("hint", issue.getHint());

            jsonGenerator.writeFieldName("costumes");
            jsonGenerator.writeStartArray();

            ActorMetadata actorMetadata = issue.getActor().getActorMetadata();
            for (ImageMetadata image : actorMetadata.getCostumes().getList()) {
                jsonGenerator.writeString(image.getAssetId());
            }
            jsonGenerator.writeEndArray();

            jsonGenerator.writeNumberField("currentCostume", actorMetadata.getCurrentCostume());

            addCodeEntry(issue.getScriptOrProcedureDefinition(), issue, "code");
            addCodeEntry(issue.getRefactoredScriptOrProcedureDefinition(), issue, "refactoring");

            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
        jsonGenerator.close();
        if (closeStream) {
            outputStream.close();
        }
    }

    private void addCodeEntry(ScriptEntity location, Issue issue, String title) throws IOException {
        if (location == null) {
            String emptyScript = ScratchBlocksVisitor.SCRATCHBLOCKS_START + System.lineSeparator()
                    + ScratchBlocksVisitor.SCRATCHBLOCKS_END + System.lineSeparator();
            jsonGenerator.writeStringField(title, emptyScript);
        } else {
            ScratchBlocksVisitor blockVisitor = new ScratchBlocksVisitor(issue);
            blockVisitor.begin();
            location.accept(blockVisitor);
            blockVisitor.end();
            String scratchBlockCode = blockVisitor.getScratchBlocks();
            jsonGenerator.writeStringField(title, scratchBlockCode);
        }
    }
}
