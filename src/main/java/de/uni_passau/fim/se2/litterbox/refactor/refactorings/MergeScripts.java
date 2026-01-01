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
package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MergeScripts extends AbstractMergeRefactoring {

    public static final String NAME = "merge_scripts";

    public MergeScripts(Script script1, Script script2) {
        super(script1, script2, createReplacementScript(script1, script2));
        Preconditions.checkArgument(script1.getEvent().equals(script2.getEvent()));
    }

    private static Script createReplacementScript(Script script1, Script script2) {
        List<Stmt> mergedStatements = new ArrayList<>();
        mergedStatements.addAll(OnlyCodeCloneVisitor.copy(script1.getStmtList()).getStmts());
        mergedStatements.addAll(OnlyCodeCloneVisitor.copy(script2.getStmtList()).getStmts());

        return new Script(OnlyCodeCloneVisitor.copy(script1.getEvent()), new StmtList(mergedStatements));
    }

    public Script getMergedScript() {
        return replacementScript;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MergeScripts that)) {
            return false;
        }
        return Objects.equals(script1, that.script1)
                && Objects.equals(script2, that.script2)
                && Objects.equals(replacementScript, that.replacementScript);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script1, script2, replacementScript);
    }

    @Override
    public String getDescription() {
        return String.format("""
                %s
                Mergins
                %s
                and
                %s
                to:
                %s
                """,
                NAME,
                ScratchBlocksVisitor.of(script1),
                ScratchBlocksVisitor.of(script2),
                ScratchBlocksVisitor.of(replacementScript)
        );
    }
}
