package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.FeatureExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class AvgScriptWidthCount implements ScratchVisitor, FeatureExtractor {
    private double count = 0;
    public static final String NAME = "Avg_script_width_count";

    @Override
    public double calculateMetric(Script script) {
        Preconditions.checkNotNull(script);
        count = 0;
        count = countScriptWidthCount(script);
        return count;
    }

    private double countScriptWidthCount(Script script) {
        String scriptString = getScriptString(script);
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

    private String getScriptString(Script script) {
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor();
        visitor.begin();
        script.accept(visitor);
        visitor.end();
        return visitor.getScratchBlocks();
    }

    @Override
    public String getName() {
        return NAME;
    }
}
