/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExtractEventsFromForever extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "extract_event_handler";

    private RepeatForeverStmt loop;
    private ScriptList scriptList;
    private Script script;
    private ArrayList<Script> eventScripts = new ArrayList<>();

    public ExtractEventsFromForever(ScriptList scriptList, Script script, RepeatForeverStmt loop) {
        this.loop = Preconditions.checkNotNull(loop);
        this.scriptList = Preconditions.checkNotNull(scriptList);
        this.script = Preconditions.checkNotNull(script);

        // New Script for each if then with KeyPressed Event
        for (Stmt stmt : this.loop.getStmtList().getStmts()) {
            IfThenStmt ifThenStmt = (IfThenStmt) stmt;
            BoolExpr expr = ifThenStmt.getBoolExpr();
            Event keyPressedEvent = new KeyPressed(apply(((IsKeyPressed) expr).getKey()), apply(script.getEvent().getMetadata()));
            Script eventScript = new Script(keyPressedEvent, apply(ifThenStmt.getThenStmts()));
            eventScripts.add(eventScript);
        }
    }

    @Override
    public ASTNode visit(ScriptList node) {

        if (node != this.scriptList) {
            return new ScriptList(applyList(node.getScriptList()));
        }

        List<Script> scripts = new ArrayList<>();
        for (Script currentScript : node.getScriptList()) {
            if (currentScript != this.script) {
                scripts.add(apply(currentScript));
            }
        }

        // Add all scripts
        for (Script currentScript : eventScripts) {
            scripts.add(currentScript);
        }

        return new ScriptList(scripts);
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(this);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtractEventsFromForever)) return false;
        ExtractEventsFromForever that = (ExtractEventsFromForever) o;
        return Objects.equals(loop, that.loop) && Objects.equals(scriptList, that.scriptList) && Objects.equals(script, that.script) && Objects.equals(eventScripts, that.eventScripts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loop, scriptList, script, eventScripts);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Script script : eventScripts) {
            sb.append(System.lineSeparator());
            sb.append(script.getScratchBlocks());
            sb.append(" and ");
        }
        sb.delete(sb.length() - 6, sb.length() - 1);
        return NAME + System.lineSeparator() + "Extracting" + loop.getScratchBlocks() +  System.lineSeparator()
                + " to:" + System.lineSeparator() + sb +  System.lineSeparator();
    }

}
