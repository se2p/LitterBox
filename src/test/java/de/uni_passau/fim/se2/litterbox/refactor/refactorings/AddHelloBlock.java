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

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class AddHelloBlock implements Refactoring {

    private final Script script;
    private final StmtList stmtList;
    private static final String NAME = "add_hello_block";

    public AddHelloBlock(Script script) {
        this.script = Preconditions.checkNotNull(script);
        this.stmtList = script.getStmtList();
    }

    @Override
    public Program apply(Program program) {
        Say helloBlock = new Say(new StringLiteral("Hello!"), new NoBlockMetadata());
        stmtList.getStmts().add(helloBlock);
        return program;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + "(" + script.getUniqueName() + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AddHelloBlock)) {
            return false;
        }
       return stmtList == ((AddHelloBlock) other).stmtList;
    }

    @Override
    public int hashCode() {
        return stmtList.hashCode();
    }

}
