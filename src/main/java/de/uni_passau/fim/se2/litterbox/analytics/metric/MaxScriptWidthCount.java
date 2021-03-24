package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.Collections;

public class MaxScriptWidthCount<T extends ASTNode> implements ScratchVisitor, MetricExtractor<T> {
    private double count = 0;
    public static final String NAME = "Max_script_width_count";

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        count = countMaxScriptWidthCount(node);
        return count;
    }

    private double countMaxScriptWidthCount(T node) {
        String scriptString = getScriptString(node);
        scriptString = scriptString.replace("[scratchblocks]\r\n", "");
        scriptString = scriptString.replace("[/scratchblocks]\r\n", "");
        scriptString = scriptString.replace("end\r\n", "");

        ArrayList<Integer> scriptWidths = new ArrayList<>();
        String[] scriptBlocks = scriptString.split("\r\n");
        for (String scriptBlock : scriptBlocks) {
            scriptWidths.add(scriptBlock.length());
        }

        return Collections.max(scriptWidths);
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
