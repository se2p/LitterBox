package de.uni_passau.fim.se2.litterbox.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;

import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record IssueDTO(
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
        List<SimilarIssueDTO> similarTo,
        String hint,
        List<String> costumes,
        int currentCostume,
        @JsonProperty("code")
        String scratchBlocksCode,
        @JsonProperty("refactoring")
        String refactoringScratchBlocksCode
) {
}
