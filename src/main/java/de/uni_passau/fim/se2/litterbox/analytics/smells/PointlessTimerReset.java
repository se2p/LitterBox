/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Timer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ResetTimer;

import java.util.*;

/**
 * Checks if the "reset timer" block is used but the "timer" block is never used.
 */
public class PointlessTimerReset extends AbstractIssueFinder {

    public static final String NAME = "pointless_timer_reset";

    private record ResetContext(ResetTimer node, ActorDefinition actor, ScriptEntity script) {
    }

    private final List<ResetContext> resets = new ArrayList<>();
    private boolean isTimerUsed = false;

    @Override
    public Set<Issue> check(Program program) {
        this.program = program;
        this.issues = new LinkedHashSet<>();
        this.resets.clear();
        this.isTimerUsed = false;

        program.accept(this);

        if (!isTimerUsed && !resets.isEmpty()) {
            for (ResetContext ctx : resets) {
                createIssue(ctx);
            }
        }

        return this.issues;
    }

    private void createIssue(ResetContext ctx) {
        IssueBuilder builder = new IssueBuilder()
                .withFinder(this)
                .withProgram(program)
                .withActor(ctx.actor())
                .withScriptOrProcedure(ctx.script())
                .withCurrentNode(ctx.node())
                .withMetadata(ctx.node().getMetadata())
                .withHint(NAME)
                .withSeverity(IssueSeverity.LOW);

        addIssue(builder);
    }

    @Override
    public void visit(ResetTimer node) {
        resets.add(new ResetContext(node, currentActor, getCurrentScriptEntity()));
        super.visit(node);
    }

    @Override
    public void visit(Timer node) {
        isTimerUsed = true;
        super.visit(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public Collection<String> getHintKeys() {
        return List.of(NAME);
    }
}
