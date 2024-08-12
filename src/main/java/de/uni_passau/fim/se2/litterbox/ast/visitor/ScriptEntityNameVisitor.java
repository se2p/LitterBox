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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.Optional;

/**
 * Generates a unique id for a {@link ScriptEntity} that stays the same across multiple runs.
 *
 * <p>This is achieved by returning the first block ID of its statements.
 * A blockID should be unique within a Scratch program.
 *
 * <p>In some rare cases where a JSON file might have been edited manually, the uniqueness cannot be ensured.
 */
public class ScriptEntityNameVisitor implements ScratchVisitor {

    private String name;
    private boolean foundFirstBlockId;

    private ScriptEntityNameVisitor() {
        // intentionally empty to hide public default constructor
    }

    /**
     * Generates a unique id that is the same across multiple runs.
     *
     * @param scriptEntity The script or procedure for which to determine a name.
     * @return The scriptEntity entity name. Empty if the script has no blocks from which an ID can be extracted.
     */
    public static Optional<String> getScriptName(final ScriptEntity scriptEntity) {
        final ScriptEntityNameVisitor nameVisitor = new ScriptEntityNameVisitor();
        return Optional.ofNullable(nameVisitor.getName(scriptEntity));
    }

    public String getName(ScriptEntity node) {
        visitChildren(node);
        if (name != null) {
            final int id = name.hashCode();
            if (node instanceof Script) {
                return "scriptId_" + id;
            } else if (node instanceof ProcedureDefinition) {
                return "procedureId_" + id;
            } else {
                return "unknownId_" + id;
            }
        }
        return null;
    }

    @Override
    public void visit(NonDataBlockMetadata node) {
        if (!foundFirstBlockId) {
            this.name = node.getBlockId();
            foundFirstBlockId = true;
        }
    }

    @Override
    public void visit(DataBlockMetadata node) {
        if (!foundFirstBlockId) {
            this.name = node.getBlockId();
            foundFirstBlockId = true;
        }
    }
}
