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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.ProgramMetricAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.metric.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JSONReportGenerator implements ReportGenerator {

    private final OutputStream outputStream;

    private final boolean closeStream;

    private final ObjectMapper mapper = new ObjectMapper();

    private final JsonGenerator jsonGenerator;

    private static final boolean INCLUDE_SIMILARITY = PropertyLoader.getSystemBoolProperty("json.similarity");

    private static final boolean INCLUDE_SUBSUMPTION = PropertyLoader.getSystemBoolProperty("json.subsumption");

    private static final boolean INCLUDE_DUPLICATES = PropertyLoader.getSystemBoolProperty("json.duplicates");

    private static final boolean INCLUDE_COUPLING = PropertyLoader.getSystemBoolProperty("json.coupling");

    public JSONReportGenerator(Path fileName) throws IOException {
        this(Files.newOutputStream(fileName), true);
    }

    public JSONReportGenerator(OutputStream stream) throws IOException {
        this(stream, false);
    }

    private JSONReportGenerator(final OutputStream stream, final boolean closeStream) throws IOException {
        this.outputStream = stream;
        jsonGenerator = mapper.createGenerator(outputStream, JsonEncoding.UTF8);
        jsonGenerator.useDefaultPrettyPrinter();
        this.closeStream = closeStream;
    }

    @Override
    public void generateReport(Program program, Collection<Issue> issues) throws IOException {
        final ReportDTO report = buildReport(program, issues);
        mapper.writeValue(jsonGenerator, report);

        if (closeStream) {
            outputStream.close();
        }
    }

    private ReportDTO buildReport(final Program program, final Collection<Issue> issues) {
        final Map<String, Double> metrics = getMetrics(program);
        final List<IssueDTO> issueDTOs = getIssues(issues);

        return new ReportDTO(metrics, issueDTOs);
    }

    private Map<String, Double> getMetrics(final Program program) {
        ProgramMetricAnalyzer tool = new ProgramMetricAnalyzer();

        return tool
                .getAnalyzers()
                .stream()
                .collect(
                        Collectors.toUnmodifiableMap(
                                MetricExtractor::getName,
                                metric -> metric.calculateMetric(program)
                        )
                );
    }

    private List<IssueDTO> getIssues(final Collection<Issue> issues) {
        return issues.stream().map(issue -> buildIssueDTO(issues, issue)).toList();
    }

    private IssueDTO buildIssueDTO(final Collection<Issue> issues, final Issue issue) {
        final String issueLocation = getIssueLocation(issue);
        final List<String> costumes = issue.getActor().getActorMetadata().getCostumes().getList()
                .stream()
                .map(ImageMetadata::getAssetId)
                .toList();
        final String scratchBlocksCode = getScratchBlocksCode(issue, issue.getScriptOrProcedureDefinition());
        final String refactoringCode = getScratchBlocksCode(issue, issue.getRefactoredScriptOrProcedureDefinition());

        return new IssueDTO(
                issue.getId(),
                issue.getFinderName(),
                issue.getTranslatedFinderName(),
                issue.getIssueType(),
                issue.getSeverity().getSeverityLevel(),
                issue.getActorName(),
                issueLocation,
                INCLUDE_DUPLICATES ? getDuplicateIds(issues, issue) : null,
                INCLUDE_SUBSUMPTION ? getSubsumingIssueIds(issues, issue) : null,
                INCLUDE_COUPLING ? getCoupledIssueIds(issues, issue) : null,
                INCLUDE_SIMILARITY ? getSimilarIssues(issues, issue) : null,
                issue.getHint(),
                costumes,
                issue.getActor().getActorMetadata().getCurrentCostume(),
                scratchBlocksCode,
                refactoringCode
        );
    }

    private String getIssueLocation(final Issue issue) {
        if (issue.getCodeLocation() == null) {
            return null;
        } else {
            return AstNodeUtil.getBlockId(issue.getCodeLocation());
        }
    }

    private Set<Integer> getRelatedIds(
            final Collection<Issue> issues, final Issue theIssue, final Predicate<Issue> isRelated
    ) {
        return issues.stream()
                .filter(issue -> issue != theIssue)
                .filter(isRelated)
                .map(Issue::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<Integer> getDuplicateIds(final Collection<Issue> issues, final Issue theIssue) {
        return getRelatedIds(issues, theIssue, theIssue::isDuplicateOf);
    }

    private Set<Integer> getSubsumingIssueIds(final Collection<Issue> issues, final Issue theIssue) {
        return getRelatedIds(issues, theIssue, theIssue::isSubsumedBy);
    }

    private Set<Integer> getCoupledIssueIds(final Collection<Issue> issues, final Issue theIssue) {
        return getRelatedIds(issues, theIssue, theIssue::areCoupled);
    }

    private List<SimilarIssue> getSimilarIssues(final Collection<Issue> issues, final Issue theIssue) {
        return issues.stream()
                .filter(issue -> issue != theIssue)
                .filter(issue -> theIssue.getFinder() == issue.getFinder())
                .collect(Collectors.toMap(issue -> issue, theIssue::getDistanceTo))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .map(issue -> new SimilarIssue(issue.getId(), theIssue.getDistanceTo(issue)))
                .toList();
    }

    private String getScratchBlocksCode(final Issue issue, final ScriptEntity location) {
        if (location == null) {
            return ScratchBlocksVisitor.SCRATCHBLOCKS_START + System.lineSeparator()
                    + ScratchBlocksVisitor.SCRATCHBLOCKS_END + System.lineSeparator();
        } else {
            final ScratchBlocksVisitor blockVisitor = new ScratchBlocksVisitor(issue);
            blockVisitor.begin();
            location.accept(blockVisitor);
            blockVisitor.end();

            return blockVisitor.getScratchBlocks();
        }
    }

    private record ReportDTO(
            Map<String, Double> metrics,
            List<IssueDTO> issues
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record IssueDTO(
            int id,
            String finder,
            String name,
            IssueType type,
            int severity,
            String sprite,
            String issueLocationBlockId,
            @JsonProperty("duplicate-of")
            Set<Integer> duplicateOf,
            @JsonProperty("subsumed-by")
            Set<Integer> subsumedBy,
            @JsonProperty("coupled-to")
            Set<Integer> coupledTo,
            @JsonProperty("similar-to")
            List<SimilarIssue> similarTo,
            String hint,
            List<String> costumes,
            int currentCostume,
            @JsonProperty("code")
            String scratchBlocksCode,
            @JsonProperty("refactoring")
            String refactoringScratchBlocksCode
    ) {
    }

    private record SimilarIssue(int id, double distance) {
    }
}
