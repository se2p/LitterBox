/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
