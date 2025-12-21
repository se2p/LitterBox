package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract base class for custom block bug injectors that target procedures
 * called in non-tail position.
 * <p>
 * This base class handles:
 * <ul>
 *   <li>Collecting procedure definitions</li>
 *   <li>Identifying procedures called in non-tail position (candidates)</li>
 *   <li>Filtering candidates before applying mutations</li>
 * </ul>
 * <p>
 * Subclasses implement the specific mutation to apply to candidate procedures.
 */
public abstract class AbstractCustomBlockInjector extends AbstractBugInjector {

    protected final Set<String> candidateProcedures = new HashSet<>();
    protected final Set<ProcedureDefinition> definitions = new HashSet<>();

    @Override
    public List<Program> inject(Program program) {
        candidateProcedures.clear();
        definitions.clear();

        // Pass 1: Visit to collect candidates and definitions
        List<Program> results = super.inject(program);

        // Pass 2: Apply mutations to candidate procedures
        for (ProcedureDefinition def : definitions) {
            if (candidateProcedures.contains(def.getIdent().getName())) {
                if (shouldMutate(def)) {
                    addMutation(def, this::applyMutation);
                }
            }
        }

        return results;
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
     */
    protected abstract void applyMutation(ProcedureDefinition clonedDef);

    @Override
    public void visit(StmtList node) {
        List<Stmt> stmts = node.getStmts();
        // Check non-tail calls (up to size-1)
        for (int i = 0; i < stmts.size() - 1; i++) {
            Stmt stmt = stmts.get(i);
            if (stmt instanceof CallStmt call) {
                candidateProcedures.add(call.getIdent().getName());
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        definitions.add(node);
        super.visit(node);
    }
}
