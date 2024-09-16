/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlockId;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawTarget;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;

public class ProgramParserState {
    private final SymbolTable symbolTable;
    private final ProcedureDefinitionNameMapping procDefMap;

    private LocalIdentifier currentActor;
    private RawTarget currentTarget;

    public ProgramParserState() {
        symbolTable = new SymbolTable();
        this.procDefMap = new ProcedureDefinitionNameMapping();
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public ProcedureDefinitionNameMapping getProcDefMap() {
        return procDefMap;
    }

    public LocalIdentifier getCurrentActor() {
        return currentActor;
    }

    public RawTarget getCurrentTarget() {
        return currentTarget;
    }

    /**
     * Returns a block in the current target.
     *
     * @param id The identifier of the block.
     * @return The block in the current target, if one can be found. {@code null} otherwise.
     */
    public RawBlock getBlock(final RawBlockId id) {
        return currentTarget.blocks().get(id);
    }

    public void setCurrentActor(LocalIdentifier currentActor) {
        this.currentActor = currentActor;
    }

    public void setCurrentActor(final LocalIdentifier currentActor, final RawTarget currentTarget) {
        this.currentActor = currentActor;
        this.currentTarget = currentTarget;
    }
}
