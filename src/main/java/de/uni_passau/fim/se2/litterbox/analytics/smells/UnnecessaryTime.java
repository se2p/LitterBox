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

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.TimedStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPosXY;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementDeletionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This finder looks if a wait block waits for 0 seconds and thus is unnecessary.
 */
public class UnnecessaryTime extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_time";
    public static final String WAIT = "unnecessary_wait";
    public static final String GLIDE = "unnecessary_glide";
    public static final String GLIDE_XY = "unnecessary_glide_xy";
    public static final String THINK = "unnecessary_think";
    public static final String SAY = "unnecessary_say";

    private void createIssue(TimedStmt node, String hintKey) {
        IssueBuilder builder = prepareIssueBuilder(node)
                .withHint(hintKey)
                .withSeverity(IssueSeverity.HIGH)
                .withRefactoring(new StatementDeletionVisitor(node).apply(getCurrentScriptEntity()));
        addIssue(builder);
    }

    @Override
    public void visit(WaitSeconds node) {
        if (checkTime(node.getSeconds())) {
            createIssue(node, WAIT);
        }
    }

    @Override
    public void visit(ThinkForSecs node) {
        if (checkTime(node.getSecs())) {
            createIssue(node, THINK);
        }
    }

    @Override
    public void visit(SayForSecs node) {
        if (checkTime(node.getSecs())) {
            createIssue(node, SAY);
        }
    }

    @Override
    public void visit(GlideSecsTo node) {
        if (checkTime(node.getSecs())) {
            IssueBuilder builder = prepareIssueBuilder(node)
                    .withHint(GLIDE)
                    .withSeverity(IssueSeverity.HIGH)
                    .withRefactoring(new StatementReplacementVisitor(node, new GoToPos(node.getPosition(), node.getMetadata())).apply(getCurrentScriptEntity()));
            addIssue(builder);
        }
    }

    @Override
    public void visit(GlideSecsToXY node) {
        if (checkTime(node.getSecs())) {
            IssueBuilder builder = prepareIssueBuilder(node)
                    .withHint(GLIDE_XY)
                    .withSeverity(IssueSeverity.HIGH)
                    .withRefactoring(new StatementReplacementVisitor(node, new GoToPosXY(node.getX(), node.getY(), node.getMetadata())).apply(getCurrentScriptEntity()));
            addIssue(builder);
        }
    }

    private boolean checkTime(NumExpr node) {
        if (node instanceof NumberLiteral num) {
            return num.getValue() <= 0;
        } else if (node instanceof AsNumber num) {
            if (num.getOperand1() instanceof StringLiteral stringLiteral) {
                return stringLiteral.getText().equals("");
            }
        }
        return false;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(WAIT);
        keys.add(SAY);
        keys.add(THINK);
        keys.add(GLIDE);
        keys.add(GLIDE_XY);
        return keys;
    }
}
