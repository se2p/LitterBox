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
package de.uni_passau.fim.se2.litterbox.export;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Base class for exporting LitterBox AST to text-based formats.
 *
 * <p>Provides common utilities for:
 * <ul>
 *   <li>Text emission with indentation support</li>
 *   <li>Context tracking (current actor, program)</li>
 *   <li>Output stream management</li>
 * </ul>
 *
 * <p>Subclasses should implement format-specific visit methods to produce
 * their target output format.
 *
 * @see de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor
 * @see de.uni_passau.fim.se2.litterbox.ast.visitor.LeilaVisitor
 */
public abstract class AbstractExportVisitor implements ScratchVisitor {

    protected static final String INDENT = "    ";

    protected final ByteArrayOutputStream byteStream;
    protected final PrintStream printStream;
    protected int indentLevel;

    protected Program program;
    protected ActorDefinition currentActor;

    /**
     * Creates a new export visitor with internal buffering.
     */
    protected AbstractExportVisitor() {
        this.byteStream = new ByteArrayOutputStream();
        this.printStream = new PrintStream(byteStream, true, StandardCharsets.UTF_8);
        this.indentLevel = 0;
    }

    /**
     * Creates a new export visitor writing to the given stream.
     *
     * @param printStream The stream to write output to
     */
    protected AbstractExportVisitor(PrintStream printStream) {
        this.byteStream = null;
        this.printStream = printStream;
        this.indentLevel = 0;
    }

    // ---------------------------------------------------------------
    // Output Methods
    // ---------------------------------------------------------------

    /**
     * Emits a token followed by a space.
     */
    protected void emitToken(String string) {
        emitNoSpace(string);
        emitNoSpace(" ");
    }

    /**
     * Emits text without trailing space.
     */
    protected void emitNoSpace(String string) {
        printStream.append(string);
    }

    /**
     * Emits a newline character.
     */
    protected void newLine() {
        emitNoSpace("\n");
    }

    /**
     * Emits a newline followed by indentation.
     */
    protected void newIndentedLine() {
        newLine();
        appendIndentation();
    }

    /**
     * Appends current indentation.
     */
    protected void appendIndentation() {
        emitNoSpace(INDENT.repeat(indentLevel));
    }

    /**
     * Increases indentation level.
     */
    protected void beginIndentation() {
        indentLevel++;
    }

    /**
     * Decreases indentation level.
     */
    protected void endIndentation() {
        indentLevel--;
    }

    // ---------------------------------------------------------------
    // Context Management
    // ---------------------------------------------------------------

    /**
     * Sets the current program context.
     */
    public void setProgram(Program program) {
        this.program = program;
    }

    /**
     * Gets the current program context.
     */
    public Program getProgram() {
        return program;
    }

    /**
     * Sets the current actor context.
     */
    public void setCurrentActor(ActorDefinition actor) {
        this.currentActor = actor;
    }

    /**
     * Gets the current actor context.
     */
    public ActorDefinition getCurrentActor() {
        return currentActor;
    }

    // ---------------------------------------------------------------
    // Output Retrieval
    // ---------------------------------------------------------------

    /**
     * Returns the exported content as a string.
     *
     * @return The exported content, or empty string if using external stream
     */
    public String getOutput() {
        if (byteStream != null) {
            return byteStream.toString(StandardCharsets.UTF_8);
        }
        return "";
    }

    /**
     * Resets the internal buffer for reuse.
     */
    public void reset() {
        if (byteStream != null) {
            byteStream.reset();
        }
        indentLevel = 0;
        program = null;
        currentActor = null;
    }
}
