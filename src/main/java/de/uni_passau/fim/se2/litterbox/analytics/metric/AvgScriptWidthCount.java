package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class AvgScriptWidthCount<T extends ASTNode> implements ScratchVisitor, MetricExtractor<T> {
    private double count = 0;
    public static final String NAME = "Avg_script_width_count";

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        count = countScriptWidthCount(node);
        return count;
    }

    private double countScriptWidthCount(T node) {
        String scriptString = getScriptString(node);
        scriptString = scriptString.replace("[scratchblocks]\r\n", "");
        scriptString = scriptString.replace("[/scratchblocks]\r\n", "");
        scriptString = scriptString.replace("end\r\n", "");

        ArrayList<Integer> scriptWidths = new ArrayList<>();
        String[] scriptBlocks = scriptString.split("\r\n");
        for (String scriptBlock : scriptBlocks) {
            scriptWidths.add(scriptBlock.length());
        }
        double avgWidth = (double) scriptWidths.stream().mapToInt(Integer::intValue).sum() / scriptBlocks.length;
        return new BigDecimal(avgWidth).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private String getScriptString(T node) {
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor();
        visitor.begin();
        node.accept(visitor);
        visitor.end();
        return visitor.getScratchBlocks();
    }

    @Override
    public String getName() {
        return NAME;
    }
}
