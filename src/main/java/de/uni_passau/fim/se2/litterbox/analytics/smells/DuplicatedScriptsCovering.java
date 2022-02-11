package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DuplicatedScriptsCovering extends AbstractIssueFinder {
    private static final String NAME = "duplicated_script_covering";

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ScriptList node) {
        Set<Script> checked = new HashSet<>();
        List<Script> scripts;
        if (ignoreLooseBlocks) {
            scripts = node.getScriptList().stream().filter(s -> !(s.getEvent() instanceof Never)).collect(Collectors.toList());
        } else {
            scripts = node.getScriptList();
        }
        for (Script s : scripts) {
            currentScript = s;

            for (Script other : scripts) {
                if (s == other || checked.contains(other)) {
                    continue;
                }

                if (s.equals(other)) {
                    ASTNode topBlockCurrent;
                    ASTNode topBlockOther;
                    if (!(s.getEvent() instanceof Never)) {
                        topBlockCurrent = s.getEvent();
                        topBlockOther = other.getEvent();
                    } else {
                        topBlockCurrent = s.getStmtList().getStmts().get(0);
                        topBlockOther = other.getStmtList().getStmts().get(0);
                    }
                    TopNonDataBlockMetadata metaCurrent = (TopNonDataBlockMetadata) topBlockCurrent.getMetadata();
                    TopNonDataBlockMetadata metaOther = (TopNonDataBlockMetadata) topBlockOther.getMetadata();
                    double currentX = metaCurrent.getXPos();
                    double currentY = metaCurrent.getYPos();
                    double otherX = metaOther.getXPos();
                    double otherY = metaOther.getYPos();
                    if (Math.sqrt((currentX - otherX) * (currentX - otherX) + (currentY - otherY) * (currentY - otherY)) < 2) {
                        addIssue(topBlockCurrent, metaCurrent);
                    }
                }
            }
            checked.add(s);
        }
    }
}
