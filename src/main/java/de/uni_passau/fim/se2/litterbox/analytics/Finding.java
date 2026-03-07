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

import de.uni_passau.fim.se2.litterbox.analytics.clonedetection.NormalizationVisitor;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for findings identified in Scratch projects.
 * A finding can be either an {@link Issue} (bug, smell, perfume) or a
 * {@link de.uni_passau.fim.se2.litterbox.analytics.questions.Question}.
 */
public abstract class Finding {

    private static final AtomicInteger globalFindingCount = new AtomicInteger(0);

    protected final Program program;
    protected final ActorDefinition actor;
    protected final ScriptEntity script;
    protected final ScriptEntity normalizedScript;
    protected final ASTNode node;
    protected final ASTNode normalizedNode;
    protected final Metadata metadata;
    protected final int id;

    /**
     * Constructs a Finding with all common fields.
     *
     * @param program  the program in which this finding was identified
     * @param actor    the actor in which this finding was identified
     * @param script   the script or procedure in which this finding was identified
     * @param node     the AST node closest to the finding's origin
     * @param metadata metadata containing references for comments
     * @param id       optional explicit ID; if null, auto-generated
     */
    protected Finding(Program program, ActorDefinition actor, ScriptEntity script,
                      ASTNode node, Metadata metadata, Integer id) {
        this.program = program;
        this.actor = actor;
        this.script = script;
        this.normalizedScript = normalize(script);
        this.node = node;
        this.normalizedNode = normalize(node);
        this.metadata = metadata;
        this.id = (id != null) ? id : globalFindingCount.getAndIncrement();
    }

    /**
     * Returns the name of the finder that created this finding.
     *
     * @return the finder name
     */
    public abstract String getFinderName();

    /**
     * Returns the program in which this finding was identified.
     *
     * @return the program
     */
    public Program getProgram() {
        return program;
    }

    /**
     * Returns the actor in which this finding was identified.
     *
     * @return the actor definition
     */
    public ActorDefinition getActor() {
        return actor;
    }

    /**
     * Returns the name of the actor in which this finding was identified.
     *
     * @return the actor name
     */
    public String getActorName() {
        return actor != null ? actor.getIdent().getName() : null;
    }

    /**
     * Returns the script in which this finding was identified, or null if it's in a procedure.
     *
     * @return the script, or null
     */
    public Script getScript() {
        return script instanceof Script s ? s : null;
    }

    /**
     * Returns the procedure in which this finding was identified, or null if it's in a script.
     *
     * @return the procedure definition, or null
     */
    public ProcedureDefinition getProcedure() {
        return script instanceof ProcedureDefinition p ? p : null;
    }

    /**
     * Returns the script or procedure definition in which this finding was identified.
     *
     * @return the script entity (script or procedure)
     */
    public ScriptEntity getScriptOrProcedureDefinition() {
        return script;
    }

    /**
     * Returns the normalized script or procedure definition.
     *
     * @return the normalized script entity
     */
    public ScriptEntity getNormalizedScriptOrProcedureDefinition() {
        return normalizedScript;
    }

    /**
     * Returns the AST node closest to the finding's origin.
     *
     * @return the code location node
     */
    public ASTNode getCodeLocation() {
        return node;
    }

    /**
     * Returns the normalized code location.
     *
     * @return the normalized AST node
     */
    public ASTNode getNormalizedCodeLocation() {
        return normalizedNode;
    }

    /**
     * Checks if the given node is the code location of this finding.
     *
     * @param node the node to check
     * @return true if it matches
     */
    public boolean isCodeLocation(ASTNode node) {
        return this.node == node;
    }

    /**
     * Returns the metadata associated with this finding.
     *
     * @return the metadata
     */
    public Metadata getCodeMetadata() {
        return metadata;
    }

    /**
     * Returns the unique identifier of this finding.
     *
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Normalizes an AST node for comparison purposes.
     *
     * @param node the node to normalize
     * @param <T>  the node type
     * @return the normalized node, or null if input is null
     */
    @SuppressWarnings("unchecked")
    protected <T extends ASTNode> T normalize(T node) {
        if (node == null) {
            return null;
        }
        NormalizationVisitor visitor = new NormalizationVisitor();
        return (T) node.accept(visitor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Finding finding)) {
            return false;
        }
        return id == finding.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
