/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

/**
 * When an equals comparison is used as check for an until loop or a wait until, it can occur that
 * the condition is never met exactly since scratch allows floating point values. Distances to other sprites or
 * mouse positions have to match exactly the value in the comparison, otherwise the loop will run endlessly. This is
 * considered a bug since the blocks following the until / wait until will never be reached and
 * executed. This strict version only counts PEC when it is not in dead code.
 */
public class PositionEqualsCheckStrict extends PositionEqualsCheck {
    public static final String NAME = "position_equals_check_strict";
    private static boolean inScriptOrProcedure;

    @Override
    public void visit(Script script) {
        currentScript = script;
        if (!(script.getEvent() instanceof Never)) {
            inScriptOrProcedure = true;
        }
        currentProcedure = null;
        visitChildren(script);
        inScriptOrProcedure = false;
    }

    @Override
    public void visit(ProcedureDefinition procedure) {
        currentProcedure = procedure;
        inScriptOrProcedure = true;
        currentScript = null;
        visitChildren(procedure);
        inScriptOrProcedure = false;
    }

    @Override
    public void visit(Equals node) {
        if (inCondition && inScriptOrProcedure) {
            if (!checkEquals(node)) {
                addIssue(node, node.getMetadata());
            }
        }
    }
}
