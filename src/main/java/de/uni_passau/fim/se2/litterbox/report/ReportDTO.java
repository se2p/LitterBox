package de.uni_passau.fim.se2.litterbox.report;

import java.util.List;
import java.util.Map;

public record ReportDTO(
        Map<String, Double> metrics,
        List<IssueDTO> issues
) {
}
