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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;

import java.util.List;

public class DuplicatedScriptsCovering extends AbstractIssueFinder {
    private static final String NAME = "duplicated_scripts_covering";

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
        List<Script> scripts;
        if (ignoreLooseBlocks) {
            scripts = node.getScriptList().stream().filter(s -> !(s.getEvent() instanceof Never)).toList();
        } else {
            scripts = node.getScriptList();
        }
        for (int i = 0; i < scripts.size() - 1; i++) {
            currentScript = scripts.get(i);
            for (int j = i + 1; j < scripts.size(); j++) {
                Script script2 = scripts.get(j);

                //Todo: a change of equals/hash would break this
                if (currentScript.equals(script2)) {
                    ASTNode topBlockCurrent;
                    ASTNode topBlockOther;
                    if (currentScript.getEvent() instanceof Never) {
                        topBlockCurrent = currentScript.getStmtList().getStmts().get(0);
                        topBlockOther = script2.getStmtList().getStmts().get(0);
                    } else {
                        topBlockCurrent = currentScript.getEvent();
                        topBlockOther = script2.getEvent();
                    }
                    if (!(topBlockCurrent.getMetadata() instanceof TopNonDataBlockMetadata metaCurrent)
                            || !(topBlockOther.getMetadata() instanceof TopNonDataBlockMetadata metaOther)) {
                        continue;
                    }
                    double currentX = metaCurrent.getXPos();
                    double currentY = metaCurrent.getYPos();
                    double otherX = metaOther.getXPos();
                    double otherY = metaOther.getYPos();
                    if (Math.sqrt((currentX - otherX) * (currentX - otherX) + (currentY - otherY) * (currentY - otherY)) < 2) {
                        addIssue(topBlockCurrent, metaCurrent);
                    }
                }
            }
        }
    }
}
