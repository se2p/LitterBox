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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.*;

/**
 * Abstract base class for custom block bug injectors that target procedures
 * called in non-tail position.
 *
 * <p>
 * This base class handles:
 * <ul>
 *   <li>Collecting procedure definitions</li>
 *   <li>Identifying procedures called in non-tail position (candidates)</li>
 *   <li>Filtering candidates before applying mutations</li>
 * </ul>
 *
 * <p>
 * Subclasses implement the specific mutation to apply to candidate procedures.
 *
 * <p>
 * IMPORTANT: Both calls and definitions must be on the same actor for the bug
 * to be detectable, since the finder checks per-actor.
 */
public abstract class AbstractCustomBlockInjector extends AbstractBugInjector {

    protected Map<LocalIdentifier, ProcedureInfo> procMap;
    protected Program program;

    // Per-actor tracking
    private final Set<String> currentActorCandidates = new HashSet<>();
    private final Map<String, ProcedureDefinition> currentActorDefinitions = new HashMap<>();

    @Override
    public List<InjectionResult> inject(Program program) {
        this.program = program;
        return super.inject(program);
    }

    @Override
    public void visit(ActorDefinition node) {
        // Reset per-actor tracking
        currentActorCandidates.clear();
        currentActorDefinitions.clear();

        String actorName = node.getIdent().getName();
        procMap = program.getProcedureMapping().getProceduresForActor(actorName);

        // First pass: collect all info for this actor
        super.visit(node);

        // Apply mutations to candidate procedures in this actor
        for (Map.Entry<String, ProcedureDefinition> entry : currentActorDefinitions.entrySet()) {
            String proccode = entry.getKey();
            ProcedureDefinition def = entry.getValue();

            // Only mutate if this procedure is called in non-tail position within this actor
            if (currentActorCandidates.contains(proccode) && shouldMutate(def)) {
                final String finalProccode = proccode;
                modifyClonedNode(def, clonedDef -> applyMutation(clonedDef, finalProccode));
            }
        }
    }

    /**
     * Determines if this procedure definition should be mutated.
     * Subclasses can override to add additional filtering.
     *
     * @param def The procedure definition to check
     * @return true if mutation should be applied
     */
    protected boolean shouldMutate(ProcedureDefinition def) {
        return true;
    }

    /**
     * Applies the specific mutation to the cloned procedure definition.
     *
     * @param clonedDef The cloned procedure definition to mutate
     * @param proccode  The procedure code/name (e.g., "foo" or "my procedure %s")
     */
    protected abstract void applyMutation(ProcedureDefinition clonedDef, String proccode);

    @Override
    public void visit(StmtList node) {
        List<Stmt> stmts = node.getStmts();
        // Check non-tail calls (up to size-1)
        for (int i = 0; i < stmts.size() - 1; i++) {
            Stmt stmt = stmts.get(i);
            if (stmt instanceof CallStmt call) {
                // CallStmt.getIdent().getName() returns the proccode
                currentActorCandidates.add(call.getIdent().getName());
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        AstNodeUtil.findActor(node).ifPresent(actor -> {
            String proccode = getProcedureName(program, actor, node);
            currentActorDefinitions.put(proccode, node);
        });
        super.visit(node);
    }
}
