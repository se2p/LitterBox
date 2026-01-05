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
package de.uni_passau.fim.se2.litterbox.ast.builder;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for constructing {@link ProcedureDefinition} nodes.
 * Simplifies procedure creation and modification in bug injectors.
 */
public class ProcedureBuilder {

    private LocalIdentifier ident;
    private ParameterDefinitionList parameterDefinitionList;
    private StmtList body;
    private ProcedureMetadata metadata;

    public ProcedureBuilder() {
        this.parameterDefinitionList = new ParameterDefinitionList(new ArrayList<>());
        this.body = new StmtList(new ArrayList<>());
    }

    /**
     * Initializes the builder from an existing procedure, copying all fields.
     */
    public ProcedureBuilder from(ProcedureDefinition existing) {
        this.ident = existing.getIdent();
        this.parameterDefinitionList = existing.getParameterDefinitionList();
        this.body = existing.getStmtList();
        this.metadata = existing.getMetadata();
        return this;
    }

    public ProcedureBuilder withIdent(LocalIdentifier ident) {
        this.ident = ident;
        return this;
    }

    public ProcedureBuilder withIdent(String name) {
        this.ident = new StrId(name);
        return this;
    }

    public ProcedureBuilder withParameters(ParameterDefinitionList params) {
        this.parameterDefinitionList = params;
        return this;
    }

    public ProcedureBuilder withParameters(List<ParameterDefinition> params) {
        this.parameterDefinitionList = new ParameterDefinitionList(params);
        return this;
    }

    public ProcedureBuilder withBody(StmtList body) {
        this.body = body;
        return this;
    }

    public ProcedureBuilder withBody(List<Stmt> stmts) {
        this.body = new StmtList(stmts);
        return this;
    }

    public ProcedureBuilder withBody(Stmt... stmts) {
        this.body = new StmtList(stmts);
        return this;
    }

    public ProcedureBuilder withMetadata(ProcedureMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Clears the current metadata so that fresh block IDs will be generated during build.
     * Useful when duplicating existing procedures to avoid ID collisions.
     */
    public ProcedureBuilder withFreshMetadata() {
        this.metadata = null;
        return this;
    }

    /**
     * Builds the ProcedureDefinition with parent pointers set.
     */
    public ProcedureDefinition build() {
        if (metadata == null) {
            metadata = new ProcedureMetadata(
                    TopNonDataBlockMetadata.emptyTopNonBlockMetadata(),
                    NonDataBlockMetadata.createCallMetadata(false)
            );
        }
        ProcedureDefinition proc = new ProcedureDefinition(ident, parameterDefinitionList, body, metadata);
        if (ident != null) {
            ident.setParentNode(proc);
        }
        if (parameterDefinitionList != null) {
            parameterDefinitionList.setParentNode(proc);
        }
        if (body != null) {
            body.setParentNode(proc);
        }
        return proc;
    }

    // ========== Static utility methods ==========

    /**
     * Creates a CallStmt that calls the given procedure with empty string arguments.
     *
     * <p>
     * Warning: This method uses the procedure's internal identifier, which may not match
     * what bug finders expect. For injectors that need detection by finders, use
     * {@link #createCallTo(ProcedureDefinition, String)} with the proccode instead.
     */
    public static CallStmt createCallTo(ProcedureDefinition proc) {
        return createCallTo(proc.getIdent(), proc.getParameterDefinitionList().getParameterDefinitions().size());
    }

    /**
     * Creates a CallStmt that calls the given procedure with empty string arguments,
     * using the specified proccode as the identifier.
     *
     * <p>
     * This is the preferred method for injectors as it ensures the CallStmt identifier
     * matches what bug finders expect (the human-readable proccode like "foo" or "write %s").
     *
     * @param proc     The procedure definition to call
     * @param proccode The procedure code/name (e.g., "my procedure %s") from procMap
     * @return A CallStmt with the proccode as identifier
     */
    public static CallStmt createCallTo(ProcedureDefinition proc, String proccode) {
        StrId callId = new StrId(proccode);
        return createCallTo(callId, proc.getParameterDefinitionList().getParameterDefinitions().size());
    }

    /**
     * Creates a CallStmt with the given identifier and empty string arguments for each parameter.
     */
    public static CallStmt createCallTo(LocalIdentifier ident, int paramCount) {
        List<Expression> args = new ArrayList<>();
        for (int i = 0; i < paramCount; i++) {
            args.add(new StringLiteral(""));
        }
        ExpressionList exprList = new ExpressionList(args);
        for (Expression e : args) {
            e.setParentNode(exprList);
        }
        CallStmt call = new CallStmt(ident, exprList, NonDataBlockMetadata.createCallMetadata(false));
        exprList.setParentNode(call);
        return call;
    }

    /**
     * Replaces a procedure in a ProcedureDefinitionList.
     * Also sets the parent node of the replacement.
     *
     * @return true if replacement was successful
     */
    public static boolean replaceIn(ProcedureDefinitionList list, ProcedureDefinition old,
                                    ProcedureDefinition replacement) {
        List<ProcedureDefinition> procedures = list.getList();
        int index = procedures.indexOf(old);
        if (index != -1) {
            procedures.set(index, replacement);
            replacement.setParentNode(list);
            return true;
        }
        return false;
    }
}
