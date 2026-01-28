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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

/**
 * Abstract builder for creating {@link Finding} instances.
 * Provides common builder methods for fields shared by all findings.
 *
 * @param <T> the concrete finding type being built
 * @param <B> the concrete builder type (for fluent chaining)
 */
public abstract class FindingBuilder<T extends Finding, B extends FindingBuilder<T, B>> {

    protected Program program;
    protected ActorDefinition actor;
    protected ScriptEntity script;
    protected ASTNode currentNode;
    protected Metadata metadata;
    protected Integer id;

    /**
     * Returns this builder cast to the concrete builder type.
     * Used for fluent method chaining in subclasses.
     *
     * @return this builder
     */
    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    /**
     * Sets the program in which the finding was identified.
     *
     * @param program the program
     * @return this builder
     */
    public B withProgram(Program program) {
        this.program = program;
        return self();
    }

    /**
     * Sets the actor in which the finding was identified.
     *
     * @param actor the actor definition
     * @return this builder
     */
    public B withActor(ActorDefinition actor) {
        this.actor = actor;
        return self();
    }

    /**
     * Sets the script or procedure in which the finding was identified.
     *
     * @param script the script entity
     * @return this builder
     */
    public B withScriptOrProcedure(ScriptEntity script) {
        this.script = script;
        return self();
    }

    /**
     * Sets the script in which the finding was identified.
     *
     * @param script the script
     * @return this builder
     */
    public B withScript(Script script) {
        this.script = script;
        return self();
    }

    /**
     * Sets the procedure in which the finding was identified.
     *
     * @param procedure the procedure definition
     * @return this builder
     */
    public B withProcedure(ProcedureDefinition procedure) {
        this.script = procedure;
        return self();
    }

    /**
     * Sets the AST node closest to the finding's origin.
     *
     * @param currentNode the current node
     * @return this builder
     */
    public B withCurrentNode(ASTNode currentNode) {
        this.currentNode = currentNode;
        return self();
    }

    /**
     * Sets the metadata for this finding.
     *
     * @param metadata the metadata
     * @return this builder
     */
    public B withMetadata(Metadata metadata) {
        this.metadata = metadata;
        return self();
    }

    /**
     * Sets an explicit ID for this finding.
     *
     * @param id the ID
     * @return this builder
     */
    public B withId(int id) {
        this.id = id;
        return self();
    }

    // Getters for subclasses

    public Program getProgram() {
        return program;
    }

    public ActorDefinition getActor() {
        return actor;
    }

    public ScriptEntity getScript() {
        return script;
    }

    public ASTNode getCurrentNode() {
        return currentNode;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public Integer getId() {
        return id;
    }

    /**
     * Builds the finding instance.
     *
     * @return the constructed finding
     */
    public abstract T build();
}
