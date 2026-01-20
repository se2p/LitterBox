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
package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.WithCloneExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Timer;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.MusicBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.TextToSpeechBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.TranslateBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.DataExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.VariableReferenceCollector;
import de.uni_passau.fim.se2.litterbox.export.AbstractExportVisitor;
import de.uni_passau.fim.se2.litterbox.export.ScratchKeyCode;
import de.uni_passau.fim.se2.litterbox.export.StringExprNameVisitor;

import java.io.File;
import java.io.PrintStream;
import java.util.*;

/**
 * Exports LitterBox AST to PyStage Python format.
 *
 * <p>PyStage is a Python library that bridges the gap between Scratch and Python,
 * enabling Scratch-like programming with Python syntax.
 *
 * <p>Example output:
 * <pre>{@code
 * from pystage.en import Stage
 *
 * stage = Stage()
 * sprite1 = stage.add_a_sprite()
 *
 * def sprite1_on_start(self):
 *     self.say("Hello!")
 *     self.move(10)
 *
 * sprite1.when_program_starts(sprite1_on_start)
 *
 * stage.play()
 * }</pre>
 *
 * @see <a href="https://github.com/pystage/pystage">PyStage Documentation</a>
 */
public class PyStageVisitor extends AbstractExportVisitor {

    // ---------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------

    private static final String STAGE_VAR_NAME = "stage";
    private static final String SELF = "self";
    private static final String CORE_ACCESSOR = "._core";
    private static final String IMAGES_DIR = "images" + File.separator;

    private static final Set<String> PYTHON_KEYWORDS = Set.of(
            "False", "None", "True", "and", "as", "assert", "async", "await", "break",
            "class", "continue", "def", "del", "elif", "else", "except", "finally",
            "for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal",
            "not", "or", "pass", "raise", "return", "try", "while", "with", "yield"
    );

    // ---------------------------------------------------------------
    // State Fields
    // ---------------------------------------------------------------

    private int handlerCounter = 0;
    private String currentSpritePyName = "";
    private final List<String> eventRegistrations = new ArrayList<>();
    private final Set<String> inferredGlobals = new HashSet<>();
    private boolean inHandler = false;
    private final Map<String, String> parameterNameMapping = new HashMap<>();

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    /**
     * Creates a new PyStage visitor with internal buffering.
     */
    public PyStageVisitor() {
        super();
    }

    /**
     * Creates a new PyStage visitor writing to the given stream.
     *
     * @param printStream The stream to write output to
     */
    public PyStageVisitor(PrintStream printStream) {
        super(printStream);
    }

    /**
     * Converts the given AST node to PyStage Python format.
     *
     * @param node Some node of the AST
     * @return The PyStage Python representation
     */
    public static String of(ASTNode node) {
        PyStageVisitor visitor = new PyStageVisitor();
        visitor.setProgram(AstNodeUtil.findParent(node, Program.class));
        ActorDefinition actor = AstNodeUtil.findParent(node, ActorDefinition.class);
        visitor.setCurrentActor(actor);

        if (actor != null) {
            if (actor.isStage()) {
                visitor.currentSpritePyName = STAGE_VAR_NAME;
            } else {
                visitor.currentSpritePyName = visitor.toPythonIdentifier(actor.getIdent().getName());
            }
        }

        // Set inHandler = true for statements and events since they are always inside handlers.
        // This ensures emitSimpleCall and similar methods use 'self' instead of the sprite name.
        if (node instanceof Stmt || node instanceof Event) {
            visitor.inHandler = true;
        }

        node.accept(visitor);
        return visitor.getOutput();
    }

    // ---------------------------------------------------------------
    // Emission Helper Methods
    // ---------------------------------------------------------------

    /**
     * Returns the appropriate object reference ("self" in handlers, sprite name otherwise).
     */
    private String getSelfOrSprite() {
        return inHandler ? SELF : currentSpritePyName;
    }

    /**
     * Checks if the current actor is the stage.
     */
    private boolean isCurrentActorStage() {
        return currentActor != null && currentActor.isStage();
    }

    /**
     * Emits a simple method call with no arguments and a newline.
     * Example: self.show()
     */
    private void emitSimpleCall(String methodName) {
        emitNoSpace(getSelfOrSprite() + "." + methodName + "()");
        newLine();
    }

    /**
     * Returns the wait method name appropriate for the current actor type.
     * Stage uses wait_seconds, sprites use wait.
     */
    private String getWaitMethodName() {
        return isCurrentActorStage() ? "wait_seconds" : "wait";
    }

    /**
     * Emits a yield/wait(0) call appropriate for the current actor type.
     * Includes indentation and newline.
     */
    private void emitYield() {
        appendIndentation();
        emitNoSpace(SELF + "." + getWaitMethodName() + "(0)");
        newLine();
    }

    /**
     * Emits a method call on self followed by newline.
     * The caller provides the arguments by accepting the expression emitter.
     */
    private void emitSelfMethodCall(String methodName, Runnable argEmitter) {
        emitNoSpace(SELF + "." + methodName + "(");
        argEmitter.run();
        emitNoSpace(")");
        newLine();
    }

    /**
     * Emits a core method call on self.
     * Example: self._core.motion_setx(...)
     */
    private void emitCoreMethodCall(String methodName, Runnable argEmitter) {
        emitNoSpace(SELF + CORE_ACCESSOR + "." + methodName + "(");
        argEmitter.run();
        emitNoSpace(")");
        newLine();
    }

    /**
     * Builds the Python image path for a costume/backdrop.
     */
    private String buildImagePath(String name, String md5ext) {
        String extension = getOutputExtension(md5ext);
        return IMAGES_DIR + name + extension;
    }

    // ---------------------------------------------------------------
    // Default Behavior Override
    // ---------------------------------------------------------------

    @Override
    public void visit(ASTNode node) {
        // Handle unhandled expression nodes by emitting neutral values.
        // Comments cannot appear in the middle of Python expressions.
        if (node instanceof NumExpr || node instanceof ComparableExpr) {
            emitNoSpace("0");  // Neutral numeric value
        } else if (node instanceof BoolExpr) {
            emitNoSpace("False");  // Neutral boolean value
        } else if (node instanceof StringExpr) {
            emitNoSpace("\"\"");  // Neutral string value
        } else if (node instanceof Stmt) {
            // For statements, emit a comment and pass to prevent indentation errors
            emitNoSpace("# Unhandled node: " + node.getClass().getSimpleName());
            newLine();
            appendIndentation();
            emitNoSpace("pass");
            newLine();
        } else {
            // For other nodes (events, metadata, etc.), emit a comment
            emitNoSpace("# Unhandled node: " + node.getClass().getSimpleName());
            newLine();
        }
    }

    // ---------------------------------------------------------------
    // Program Structure
    // ---------------------------------------------------------------

    @Override
    public void visit(Program program) {
        this.program = program;

        emitProgramHeader();
        analyzeAndEmitInferredGlobals(program);

        // Process all actors
        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            actor.accept(this);
        }

        emitEventRegistrations();
        emitInitialBackdrop(program);
        emitProgramFooter();
    }

    private void emitProgramHeader() {
        // Import statements
        emitNoSpace("from pystage.en import Stage");
        newLine();
        emitNoSpace("import pygame");
        newLine();
        emitNoSpace("import math");
        newLine();
        newLine();

        // Helper function for safe number conversion (Scratch-like behavior)
        emitToNumHelper();
        emitEqualsHelper();
        emitIndexHelper();

        // Create stage
        emitNoSpace(STAGE_VAR_NAME + " = Stage()");
        newLine();
    }

    private void emitToNumHelper() {
        emitNoSpace("""
                def _to_num(v):
                    try:
                        return float(v)
                    except (ValueError, TypeError):
                        return 0
                """);
        newLine();
    }

    private void emitEqualsHelper() {
        emitNoSpace("""
                def _equals(v1, v2):
                    try:
                        return float(v1) == float(v2)
                    except (ValueError, TypeError):
                        return str(v1).lower() == str(v2).lower()
                """);
        newLine();
    }

    private void emitIndexHelper() {
        emitNoSpace("""
                def _index_of(l, item):
                    for i, x in enumerate(l):
                        if _equals(x, item):
                            return i + 1
                    return 0
                """);
        newLine();
    }

    private void analyzeAndEmitInferredGlobals(Program program) {
        inferredGlobals.clear();
        Map<String, Set<ActorDefinition>> missingVarUsage = new HashMap<>();
        ActorDefinition stageActor = null;

        // Collect used variables per actor
        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            if (actor.isStage()) {
                stageActor = actor;
            }
            Set<String> usedVars = collectUsedVariables(actor);
            removeDeclaredVariables(usedVars, actor);

            for (String v : usedVars) {
                missingVarUsage.computeIfAbsent(v, k -> new HashSet<>()).add(actor);
            }
        }

        // Remove variables declared on Stage
        if (stageActor != null) {
            removeDeclaredVariablesFromMap(missingVarUsage, stageActor);
        }

        // Identify and emit inferred globals
        for (var entry : missingVarUsage.entrySet()) {
            String varName = entry.getKey();
            Set<ActorDefinition> users = entry.getValue();
            if (users.size() > 1 || (stageActor != null && users.contains(stageActor))) {
                inferredGlobals.add(varName);
            }
        }

        for (String varName : new java.util.TreeSet<>(inferredGlobals)) {
            emitNoSpace(STAGE_VAR_NAME + ".create_variable(\"" + escapeString(varName) + "\")");
            newLine();
        }
    }

    private Set<String> collectUsedVariables(ActorDefinition actor) {
        VariableReferenceCollector collector = new VariableReferenceCollector();
        actor.getScripts().accept(collector);
        return collector.getReferencedVariables();
    }

    private void removeDeclaredVariables(Set<String> usedVars, ActorDefinition actor) {
        for (var decl : actor.getDecls().getDeclarationStmts()) {
            String name = getDeclarationName(decl);
            if (name != null) {
                usedVars.remove(name);
            }
        }
    }

    private void removeDeclaredVariablesFromMap(Map<String, Set<ActorDefinition>> map, ActorDefinition actor) {
        for (var decl : actor.getDecls().getDeclarationStmts()) {
            String name = getDeclarationName(decl);
            if (name != null) {
                map.remove(name);
            }
        }
    }

    private String getDeclarationName(DeclarationStmt decl) {
        if (decl instanceof DeclarationIdentAsTypeStmt did) {
            if (did.getIdent() instanceof Variable v) {
                return v.getName().getName();
            } else if (did.getIdent() instanceof ScratchList l) {
                return l.getName().getName();
            }
        }
        return null;
    }

    private void emitEventRegistrations() {
        newLine();
        for (String registration : eventRegistrations) {
            emitNoSpace(registration);
            newLine();
        }
    }

    private void emitInitialBackdrop(Program program) {
        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            if (actor.isStage() && actor.getActorMetadata() != null && actor.getActorMetadata().getCostumes() != null) {
                var costumes = actor.getActorMetadata().getCostumes().getList();
                if (costumes != null && !costumes.isEmpty()) {
                    String backdropName = costumes.getFirst().getName();
                    newLine();
                    emitNoSpace(STAGE_VAR_NAME + ".switch_backdrop_to(\""
                            + escapeString(resolveBackdropPath(backdropName)) + "\")");
                    newLine();
                }
                break;
            }
        }
    }

    private void emitProgramFooter() {
        newLine();
        emitNoSpace(STAGE_VAR_NAME + ".play()");
        newLine();
    }

    @Override
    public void visit(ActorDefinition node) {
        currentActor = node;
        handlerCounter = 0;

        setupActorContext(node);
        emitCostumes(node);
        emitSounds(node);
        emitInitialCostume(node);

        node.getDecls().accept(this);
        emitMissingVariables(node);

        node.getSetStmtList().accept(this);
        node.getProcedureDefinitionList().accept(this);
        node.getScripts().accept(this);

        currentActor = null;
    }

    private void setupActorContext(ActorDefinition node) {
        if (node.isStage()) {
            currentSpritePyName = STAGE_VAR_NAME;
        } else {
            String spriteName = toPythonIdentifier(node.getIdent().getName());
            currentSpritePyName = spriteName;
            newLine();
            emitNoSpace(spriteName + " = " + STAGE_VAR_NAME + ".add_a_sprite()");
            newLine();
        }
    }

    private void emitCostumes(ActorDefinition node) {
        if (node.getActorMetadata() == null || node.getActorMetadata().getCostumes() == null) {
            return;
        }

        for (ImageMetadata costume : node.getActorMetadata().getCostumes().getList()) {
            double bitmapResolution = costume.getBitmapResolution() != null ? costume.getBitmapResolution() : 1.0;
            double centerX = costume.getRotationCenterX() / bitmapResolution;
            double centerY = costume.getRotationCenterY() / bitmapResolution;
            String filePath = buildImagePath(costume.getName(), costume.getMd5ext());

            emitCostumeAddCall(node.isStage(), filePath, centerX, centerY, bitmapResolution);
            newLine();
        }
    }

    private void emitCostumeAddCall(boolean isStage, String filePath, double centerX,
                                    double centerY, double bitmapResolution) {
        String baseArgs = "\"" + escapeString(filePath) + "\", center_x=" + centerX
                + ", center_y=" + centerY;
        boolean hasScaling = bitmapResolution != 1.0;

        if (isStage) {
            if (hasScaling) {
                emitNoSpace(currentSpritePyName + CORE_ACCESSOR + ".costume_manager.add_costume("
                        + baseArgs + ", factor=" + (int) bitmapResolution + ")");
            } else {
                emitNoSpace(currentSpritePyName + ".add_backdrop(" + baseArgs + ")");
            }
        } else {
            if (hasScaling) {
                emitNoSpace(currentSpritePyName + ".add_costume("
                        + baseArgs + ", factor=" + (int) bitmapResolution + ")");
            } else {
                emitNoSpace(currentSpritePyName + ".add_costume(" + baseArgs + ")");
            }
        }
    }

    private void emitSounds(ActorDefinition node) {
        if (node.getActorMetadata() == null || node.getActorMetadata().getSounds() == null) {
            return;
        }

        String addSoundMethod = node.isStage() ? CORE_ACCESSOR + ".sound_manager.add_sound" : ".add_sound";
        for (SoundMetadata sound : node.getActorMetadata().getSounds().getList()) {
            emitNoSpace(currentSpritePyName + addSoundMethod
                    + "(\"" + escapeString(sound.getName()) + "\")");
            newLine();
        }
    }

    private void emitInitialCostume(ActorDefinition node) {
        if (node.getActorMetadata() == null || node.getActorMetadata().getCostumes() == null) {
            return;
        }

        var costumes = node.getActorMetadata().getCostumes().getList();
        if (costumes == null || costumes.isEmpty()) {
            return;
        }

        int costumeIndex = determineInitialCostumeIndex(node, costumes.size());
        String costumeName = costumes.get(costumeIndex).getName();

        emitNoSpace("# Initial costume: " + costumeName);
        newLine();
        if (node.isStage()) {
            emitNoSpace(currentSpritePyName + ".switch_backdrop_to(\""
                    + escapeString(resolveBackdropPath(costumeName)) + "\")");
        } else {
            emitNoSpace(currentSpritePyName + ".switch_costume_to(\""
                    + escapeString(resolveCostumePath(costumeName)) + "\")");
        }
        newLine();
    }

    private int determineInitialCostumeIndex(ActorDefinition node, int costumeCount) {
        if (node.isStage()) {
            return 0;  // Stage backdrops: always start with the first backdrop
        }
        // Sprite costumes: use saved state
        int costumeIndex = node.getActorMetadata().getCurrentCostume();
        if (costumeIndex < 0 || costumeIndex >= costumeCount) {
            costumeIndex = 0;
        }
        return costumeIndex;
    }

    private void emitMissingVariables(ActorDefinition node) {
        Set<String> usedVars = collectUsedVariables(node);

        // Remove variables already declared on this actor
        removeDeclaredVariables(usedVars, node);

        // Remove variables declared on Stage (globals), if not the stage
        if (!node.isStage()) {
            ActorDefinition stage = getStageActor();
            if (stage != null) {
                removeDeclaredVariables(usedVars, stage);
            }
        }

        // Remove inferred globals to avoid re-creation
        usedVars.removeAll(inferredGlobals);

        // Emit missing variables (sorted for determinism)
        for (String varName : new java.util.TreeSet<>(usedVars)) {
            emitVariableCreation(node.isStage(), varName);
        }
    }

    private void emitVariableCreation(boolean isStage, String varName) {
        if (isStage) {
            emitNoSpace(currentSpritePyName + ".create_variable(\"" + escapeString(varName) + "\")");
        } else {
            emitNoSpace(currentSpritePyName + CORE_ACCESSOR + ".pystage_makevariable(\""
                    + escapeString(varName) + "\", all_sprites=False)");
        }
        newLine();
    }

    @Override
    public void visit(DeclarationStmtList node) {
        for (var decl : node.getDeclarationStmts()) {
            decl.accept(this);
        }
    }

    @Override
    public void visit(DeclarationIdentAsTypeStmt node) {
        // Emit variable/list creation
        DataExpr ident = node.getIdent();
        String name = null;
        if (ident instanceof Variable v) {
            name = v.getName().getName();
        } else if (ident instanceof ScratchList l) {
            name = l.getName().getName();
        }

        if (name != null) {
            if (currentActor != null && currentActor.isStage()) {
                emitNoSpace(currentSpritePyName + ".create_variable(\"" + escapeString(name) + "\")");
            } else {
                emitNoSpace(currentSpritePyName + "._core.pystage_makevariable(\""
                        + escapeString(name) + "\", all_sprites=False)");
            }
            newLine();
        }
    }

    @Override
    public void visit(DeclarationAttributeAsTypeStmt node) {
        // Built-in attributes don't need declaration
    }

    @Override
    public void visit(DeclarationBroadcastStmt node) {
        // Broadcast declarations are implicit in PyStage.
        // We emit a comment for clarity, but no functional code.
        emitNoSpace("# Implicit declaration: "
                + escapeString(getIdentifierName(node.getIdent())));
        newLine();
    }

    @Override
    public void visit(SetStmtList node) {
        for (var stmt : node.getStmts()) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(SetAttributeTo node) {
        String attr = getExpressionString(node.getStringExpr());
        switch (attr) {
            case "costume" -> {
                emitNoSpace(currentSpritePyName + ".switch_costume_to(");
                if (node.getExpr() instanceof StringLiteral str) {
                    emitNoSpace("\"" + escapeString(resolveCostumePath(str.getText())) + "\"");
                } else {
                    node.getExpr().accept(this);
                }
                emitNoSpace(")");
                newLine();
            }
            case "backdrop" -> {
                emitNoSpace(currentSpritePyName + ".switch_backdrop_to(");
                if (node.getExpr() instanceof StringLiteral str) {
                    emitNoSpace("\"" + escapeString(resolveBackdropPath(str.getText())) + "\"");
                } else {
                    node.getExpr().accept(this);
                }
                emitNoSpace(")");
                newLine();
            }
            case "size" -> {
                emitNoSpace(currentSpritePyName + ".set_size_to(");
                node.getExpr().accept(this);
                emitNoSpace(")");
                newLine();
            }
            case "x" -> {
                emitNoSpace(currentSpritePyName + "._core.motion_setx(");
                node.getExpr().accept(this);
                emitNoSpace(")");
                newLine();
            }
            case "y" -> {
                emitNoSpace(currentSpritePyName + "._core.motion_sety(");
                node.getExpr().accept(this);
                emitNoSpace(")");
                newLine();
            }
            case "direction" -> {
                emitNoSpace(currentSpritePyName + ".point_in_direction(");
                node.getExpr().accept(this);
                emitNoSpace(")");
                newLine();
            }
            case "visible" -> {
                // visible is a boolean - true means show, false means hide
                if (node.getExpr() instanceof BoolLiteral bool) {
                    if (bool.getValue()) {
                        emitNoSpace(currentSpritePyName + ".show()");
                    } else {
                        emitNoSpace(currentSpritePyName + ".hide()");
                    }
                    newLine();
                }
            }
            case "draggable" -> {
                // draggable is a boolean
                if (node.getExpr() instanceof BoolLiteral bool) {
                    if (bool.getValue()) {
                        emitNoSpace(currentSpritePyName + ".set_drag_mode_draggable()");
                    } else {
                        emitNoSpace(currentSpritePyName + ".set_drag_mode_not_draggable()");
                    }
                    newLine();
                }
            }
            case "rotationStyle" -> {
                if (node.getExpr() instanceof StringLiteral str) {
                    String style = str.getText();
                    switch (style) {
                        case "all around" -> emitNoSpace(currentSpritePyName + ".set_rotation_style_around()");
                        case "left-right" -> emitNoSpace(currentSpritePyName + ".set_rotation_style_left_right()");
                        case "don't rotate" -> emitNoSpace(currentSpritePyName + ".set_rotation_style_none()");
                        default -> {
                        }
                    }
                    newLine();
                }
            }
            default -> {
            }
        }
    }

    @Override
    public void visit(ShowVariable node) {
        String obj = inHandler ? "self" : currentSpritePyName;
        String name = getIdentifierName(node.getIdentifier());
        emitNoSpace(obj + ".show_variable(\"" + escapeString(name) + "\")");
        newLine();
    }

    @Override
    public void visit(HideVariable node) {
        String obj = inHandler ? "self" : currentSpritePyName;
        String name = getIdentifierName(node.getIdentifier());
        emitNoSpace(obj + ".hide_variable(\"" + escapeString(name) + "\")");
        newLine();
    }

    @Override
    public void visit(ShowList node) {
        String obj = inHandler ? "self" : currentSpritePyName;
        String name = getIdentifierName(node.getIdentifier());
        emitNoSpace(obj + ".show_variable(\"" + escapeString(name) + "\")");
        newLine();
    }

    @Override
    public void visit(HideList node) {
        String obj = inHandler ? "self" : currentSpritePyName;
        String name = getIdentifierName(node.getIdentifier());
        emitNoSpace(obj + ".hide_variable(\"" + escapeString(name) + "\")");
        newLine();
    }

    // ---------------------------------------------------------------
    // Pen Extension Blocks (not supported in PyStage - emit as comments)
    // ---------------------------------------------------------------

    @Override
    public void visit(PenBlock node) {
        // Pen blocks are not supported in PyStage - emit as comment
        emitNoSpace("# pen block (not supported in PyStage)");
        newLine();
        appendIndentation();
        emitNoSpace("pass");
        newLine();
    }

    @Override
    public void visit(ProcedureDefinitionList node) {
        for (ProcedureDefinition proc : node.getList()) {
            proc.accept(this);
        }
    }

    @Override
    public void visit(ScriptList node) {
        for (Script script : node.getScriptList()) {
            script.accept(this);
        }
    }

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            return; // Skip loose blocks
        }
        script.getEvent().accept(this);
    }

    @Override
    public void visit(StmtList node) {
        if (node.getStmts().isEmpty()) {
            appendIndentation();
            emitNoSpace("pass");
            newLine();
            return;
        }
        for (var stmt : node.getStmts()) {
            // Workaround for nested StmtList structures which can cause double indentation.
            // StmtList visitor adds indentation for its children, so we skip it here for the list itself.
            if (stmt instanceof StmtList) {
                stmt.accept(this);
            } else {
                appendIndentation();
                stmt.accept(this);
            }
        }
    }

    // ---------------------------------------------------------------
    // Event Handlers (Hat Blocks)
    // ---------------------------------------------------------------

    @Override
    public void visit(GreenFlag greenFlag) {
        String handlerName = currentSpritePyName + "_on_start_" + (handlerCounter++);
        emitEventHandler(handlerName, greenFlag);

        if (currentActor != null && currentActor.isStage()) {
            eventRegistrations.add(currentSpritePyName + ".when_GREENFLAG_clicked(" + handlerName + ")");
        } else {
            eventRegistrations.add(currentSpritePyName + ".when_program_starts(" + handlerName + ")");
        }
    }

    @Override
    public void visit(KeyPressed keyPressed) {
        String handlerName = currentSpritePyName + "_on_key_" + (handlerCounter++);
        emitEventHandler(handlerName, keyPressed);

        String keyName = getKeyName(keyPressed.getKey());
        eventRegistrations.add(currentSpritePyName + ".when_key_pressed(\"" + keyName + "\", " + handlerName + ")");
    }

    @Override
    public void visit(SpriteClicked spriteClicked) {
        String handlerName = currentSpritePyName + "_on_click_" + (handlerCounter++);
        emitEventHandler(handlerName, spriteClicked);
        eventRegistrations.add(currentSpritePyName + ".when_this_sprite_clicked(" + handlerName + ")");
    }

    @Override
    public void visit(StageClicked stageClicked) {
        String handlerName = currentSpritePyName + "_on_click_" + (handlerCounter++);
        emitEventHandler(handlerName, stageClicked);
        eventRegistrations.add(currentSpritePyName + ".when_stage_clicked(" + handlerName + ")");
    }

    @Override
    public void visit(BackdropSwitchTo backdrop) {
        String handlerName = currentSpritePyName + "_on_backdrop_" + (handlerCounter++);
        emitEventHandler(handlerName, backdrop);

        String backdropName = backdrop.getBackdrop().getName();
        eventRegistrations.add(currentSpritePyName + ".when_backdrop_switches_to(\""
                + escapeString(resolveBackdropPath(backdropName)) + "\", " + handlerName + ")");
    }

    @Override
    public void visit(ReceptionOfMessage receptionOfMessage) {
        String handlerName = currentSpritePyName + "_on_message_" + (handlerCounter++);
        emitEventHandler(handlerName, receptionOfMessage);

        String messageName = getExpressionString(receptionOfMessage.getMsg().getMessage());
        if (currentActor.isStage()) {
            // Workaround for PyStage: Stage does not support when_i_receive_message
            eventRegistrations.add(currentSpritePyName + "._core.code_manager.code_blocks[\""
                    + handlerName + "\"] = " + handlerName);
            eventRegistrations.add(currentSpritePyName
                    + "._core.code_manager.broadcast_blocks.setdefault(\""
                    + escapeString(messageName) + "\", []).append(\"" + handlerName + "\")");
        } else {
            eventRegistrations.add(currentSpritePyName + ".when_i_receive_message(\""
                    + escapeString(messageName) + "\", " + handlerName + ")");
        }
    }

    @Override
    public void visit(StartedAsClone startedAsClone) {
        String handlerName = currentSpritePyName + "_on_clone_" + (handlerCounter++);
        // Clone handlers need special handling because PyStage doesn't automatically
        // update the sprite's rendered image after cloning (it copies costume_manager
        // but doesn't call update_sprite_image()).
        emitCloneEventHandler(handlerName, startedAsClone);
        eventRegistrations.add(currentSpritePyName + ".when_i_start_as_a_clone(" + handlerName + ")");
    }

    private void emitCloneEventHandler(String handlerName, Event event) {
        newLine();
        emitNoSpace("def " + handlerName + "(self):");
        newLine();
        beginIndentation();
        inHandler = true;

        emitCloneImageUpdateWorkaround();
        emitCloneBroadcastBindingWorkaround(currentActor);

        Script parentScript = AstNodeUtil.findParent(event, Script.class);
        if (parentScript != null) {
            StmtList stmtList = parentScript.getStmtList();
            if (stmtList.getStmts().isEmpty() || containsOnlyUnsupportedStmts(stmtList)) {
                appendIndentation();
                emitNoSpace("pass");
                newLine();
            }
            stmtList.accept(this);
        } else {
            appendIndentation();
            emitNoSpace("pass");
            newLine();
        }

        inHandler = false;
        endIndentation();
    }

    private void emitCloneImageUpdateWorkaround() {
        // Workaround for PyStage: clones don't have their image updated from costume_manager
        // after creation. Force an image update so the clone renders with the correct costume.
        appendIndentation();
        emitNoSpace("self._core.costume_manager.update_sprite_image()");
        newLine();
    }

    private void emitCloneBroadcastBindingWorkaround(ActorDefinition actor) {
        // Workaround for PyStage: CodeManager.clone() doesn't copy broadcast_blocks,
        // so clones don't respond to broadcasts. The clone inherits the code blocks from the
        // parent (with "-clone-N" suffix), but broadcast_blocks is not copied.
        // We bind the existing cloned code blocks directly to broadcast_blocks without calling
        // when_i_receive_message (which would modify code_blocks during iteration and crash).
        java.util.Map<String, String> messageHandlers = findMessageHandlers(actor);
        for (var entry : messageHandlers.entrySet()) {
            String messageName = entry.getKey();
            String originalHandlerName = entry.getValue();
            appendIndentation();
            emitNoSpace("for _block_name in list(self._core.code_manager.code_blocks.keys()):");
            newLine();
            beginIndentation();
            appendIndentation();
            emitNoSpace("if _block_name.startswith(\"" + escapeString(originalHandlerName) + "\"):");
            newLine();
            beginIndentation();
            appendIndentation();
            emitNoSpace("self._core.code_manager.broadcast_blocks.setdefault(\""
                    + escapeString(messageName) + "\", []).append(_block_name)");
            newLine();
            appendIndentation();
            emitNoSpace("break");
            newLine();
            endIndentation();
            endIndentation();
        }
    }

    @Override
    public void visit(AttributeAboveValue node) {
        // PyStage may not support this directly, emit as comment
        String handlerName = currentSpritePyName + "_on_attr_" + (handlerCounter++);
        emitEventHandler(handlerName, node);
        eventRegistrations.add("# " + currentSpritePyName + " when " + node.getAttribute().getTypeName() + " > value");
    }

    @Override
    public void visit(Never never) {
        // Loose blocks - skip
    }

    private void emitEventHandler(String handlerName, Event event) {
        newLine();
        emitNoSpace("def " + handlerName + "(self):");
        newLine();
        beginIndentation();
        inHandler = true;

        Script parentScript = AstNodeUtil.findParent(event, Script.class);
        if (parentScript != null) {
            StmtList stmtList = parentScript.getStmtList();
            if (stmtList.getStmts().isEmpty() || containsOnlyUnsupportedStmts(stmtList)) {
                appendIndentation();
                emitNoSpace("pass");
                newLine();
            }
            stmtList.accept(this);
        } else {
            appendIndentation();
            emitNoSpace("pass");
            newLine();
        }

        inHandler = false;
        endIndentation();
    }

    private boolean containsOnlyUnsupportedStmts(StmtList stmtList) {
        for (var stmt : stmtList.getStmts()) {
            if (!(stmt instanceof PenStmt)) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------------
    // Control Structures
    // ---------------------------------------------------------------

    @Override
    public void visit(IfThenStmt ifThenStmt) {
        emitNoSpace("if ");
        ifThenStmt.getBoolExpr().accept(this);
        emitNoSpace(":");
        newLine();
        beginIndentation();
        ifThenStmt.getThenStmts().accept(this);
        endIndentation();
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        emitNoSpace("if ");
        ifElseStmt.getBoolExpr().accept(this);
        emitNoSpace(":");
        newLine();
        beginIndentation();
        ifElseStmt.getThenStmts().accept(this);
        endIndentation();
        appendIndentation();
        emitNoSpace("else:");
        newLine();
        beginIndentation();
        ifElseStmt.getElseStmts().accept(this);
        endIndentation();
    }

    @Override
    public void visit(RepeatForeverStmt repeatForeverStmt) {
        emitNoSpace("while True:");
        newLine();
        beginIndentation();
        repeatForeverStmt.getStmtList().accept(this);
        emitYield();  // Yield to event loop each iteration
        endIndentation();
    }

    @Override
    public void visit(RepeatTimesStmt repeatTimesStmt) {
        emitNoSpace("for _ in range(int(");
        repeatTimesStmt.getTimes().accept(this);
        emitNoSpace(")):");
        newLine();
        beginIndentation();
        repeatTimesStmt.getStmtList().accept(this);
        endIndentation();
    }

    @Override
    public void visit(UntilStmt untilStmt) {
        emitNoSpace("while not (");
        untilStmt.getBoolExpr().accept(this);
        emitNoSpace("):");
        newLine();
        beginIndentation();
        untilStmt.getStmtList().accept(this);
        emitYield();  // Yield to event loop each iteration
        endIndentation();
    }

    @Override
    public void visit(WaitSeconds node) {
        emitNoSpace(SELF + "." + getWaitMethodName() + "(");
        node.getSeconds().accept(this);
        emitNoSpace(")");
        newLine();
    }

    @Override
    public void visit(WaitUntil node) {
        emitNoSpace("while not (");
        node.getUntil().accept(this);
        emitNoSpace("):");
        newLine();
        beginIndentation();
        emitYield();  // Yield to event loop while waiting
        endIndentation();
    }

    // ---------------------------------------------------------------
    // Motion Blocks
    // ---------------------------------------------------------------

    @Override
    public void visit(MoveSteps node) {
        emitSelfMethodCall("move", () -> node.getSteps().accept(this));
    }

    @Override
    public void visit(TurnLeft node) {
        emitSelfMethodCall("turn_left", () -> node.getDegrees().accept(this));
    }

    @Override
    public void visit(TurnRight node) {
        emitSelfMethodCall("turn_right", () -> node.getDegrees().accept(this));
    }

    @Override
    public void visit(GoToPos node) {
        Position pos = node.getPosition();
        switch (pos) {
            case MousePos mousePos -> emitNoSpace("self.go_to_mouse_pointer()");
            case RandomPos randomPos -> emitNoSpace("self.go_to_random_position()");
            case FromExpression fromExpr -> {
                emitNoSpace("self.go_to_sprite(");
                emitSpriteReference(fromExpr);
                emitNoSpace(")");
            }
            default -> {
                emitNoSpace("self.go_to_sprite(");
                pos.accept(this);
                emitNoSpace(")");
            }
        }
        newLine();
    }

    @Override
    public void visit(GoToPosXY node) {
        emitSelfMethodCall("go_to_x_y", () -> {
            node.getX().accept(this);
            emitNoSpace(", ");
            node.getY().accept(this);
        });
    }

    @Override
    public void visit(GlideSecsTo node) {
        Position pos = node.getPosition();
        switch (pos) {
            case MousePos mousePos -> {
                emitNoSpace("self.glide_to_mouse_pointer(");
                node.getSecs().accept(this);
                emitNoSpace(")");
            }
            case RandomPos randomPos -> {
                emitNoSpace("self.glide_to_random_position(");
                node.getSecs().accept(this);
                emitNoSpace(")");
            }
            case FromExpression fromExpr -> {
                emitNoSpace("self.glide_to_sprite(");
                node.getSecs().accept(this);
                emitNoSpace(", ");
                emitSpriteReference(fromExpr);
                emitNoSpace(")");
            }
            default -> {
                emitNoSpace("self.glide_to_sprite(");
                node.getSecs().accept(this);
                emitNoSpace(", ");
                pos.accept(this);
                emitNoSpace(")");
            }
        }
        newLine();
    }

    @Override
    public void visit(GlideSecsToXY node) {
        emitSelfMethodCall("glide_to_x_y", () -> {
            node.getSecs().accept(this);
            emitNoSpace(", ");
            node.getX().accept(this);
            emitNoSpace(", ");
            node.getY().accept(this);
        });
    }

    @Override
    public void visit(PointInDirection node) {
        emitSelfMethodCall("point_in_direction", () -> node.getDirection().accept(this));
    }

    @Override
    public void visit(PointTowards node) {
        Position pos = node.getPosition();
        if (pos instanceof MousePos) {
            emitSimpleCall("point_towards_mouse_pointer");
        } else if (pos instanceof FromExpression fromExpr) {
            emitSelfMethodCall("point_towards_sprite", () -> {
                emitSpriteReference(fromExpr);
                emitNoSpace("._core");
            });
        } else {
            emitSelfMethodCall("point_towards_sprite", () -> pos.accept(this));
        }
    }

    private void emitSpriteReference(FromExpression fromExpr) {
        StringExpr stringExpr = fromExpr.getStringExpr();
        // Unwrap AsString if present
        if (stringExpr instanceof AsString asStr) {
            Expression inner = asStr.getOperand1();
            if (inner instanceof StringLiteral str) {
                emitNoSpace(toPythonIdentifier(str.getText()));
                return;
            } else if (inner instanceof StrId strId) {
                emitNoSpace(toPythonIdentifier(strId.getName()));
                return;
            }
        }
        if (stringExpr instanceof StringLiteral str) {
            emitNoSpace(toPythonIdentifier(str.getText()));
        } else {
            // Fallback - visit and hope for the best
            stringExpr.accept(this);
        }
    }

    @Override
    public void visit(ChangeXBy node) {
        emitCoreMethodCall("motion_changexby", () -> node.getNum().accept(this));
    }

    @Override
    public void visit(ChangeYBy node) {
        emitCoreMethodCall("motion_changeyby", () -> node.getNum().accept(this));
    }

    @Override
    public void visit(SetXTo node) {
        emitCoreMethodCall("motion_setx", () -> node.getNum().accept(this));
    }

    @Override
    public void visit(SetYTo node) {
        emitCoreMethodCall("motion_sety", () -> node.getNum().accept(this));
    }

    @Override
    public void visit(IfOnEdgeBounce node) {
        emitSimpleCall("if_on_edge_bounce");
    }

    @Override
    public void visit(SetRotationStyle node) {
        RotationStyle.RotationStyleType type = node.getRotation().getType();
        switch (type) {
            case dont_rotate -> emitSimpleCall("set_rotation_style_none");
            case left_right -> emitSimpleCall("set_rotation_style_left_right");
            case all_around -> emitSimpleCall("set_rotation_style_around");
            default -> {
            }
        }
    }

    @Override
    public void visit(FromExpression node) {
        // Extract the sprite name and emit as Python identifier (variable reference)
        // The string expression contains the sprite name (usually as StringLiteral)
        StringExpr stringExpr = node.getStringExpr();
        if (stringExpr instanceof StringLiteral str) {
            // Emit as variable reference without quotes
            emitNoSpace(toPythonIdentifier(str.getText()));
        } else {
            // For other expression types, visit them
            stringExpr.accept(this);
        }
    }

    // ---------------------------------------------------------------
    // Looks Blocks
    // ---------------------------------------------------------------

    @Override
    public void visit(Say node) {
        emitSelfMethodCall("say", () -> node.getString().accept(this));
    }

    @Override
    public void visit(SayForSecs node) {
        emitSelfMethodCall("say_for_seconds", () -> {
            node.getString().accept(this);
            emitNoSpace(", ");
            node.getSecs().accept(this);
        });
    }

    @Override
    public void visit(Think node) {
        emitSelfMethodCall("think", () -> node.getThought().accept(this));
    }

    @Override
    public void visit(ThinkForSecs node) {
        emitSelfMethodCall("think_for_seconds", () -> {
            node.getThought().accept(this);
            emitNoSpace(", ");
            node.getSecs().accept(this);
        });
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        emitSelfMethodCall("switch_costume_to", () -> {
            if (node.getCostumeChoice() instanceof WithExpr withExpr
                    && withExpr.getExpression() instanceof StringLiteral str) {
                emitNoSpace("\"" + escapeString(resolveCostumePath(str.getText())) + "\"");
            } else {
                node.getCostumeChoice().accept(this);
            }
        });
    }

    @Override
    public void visit(NextCostume node) {
        emitSimpleCall("next_costume");
    }

    @Override
    public void visit(SwitchBackdrop node) {
        emitSelfMethodCall("switch_backdrop_to", () -> {
            switch (node.getElementChoice()) {
                case Next ignored -> emitNoSpace("\"next backdrop\"");
                case Prev ignored -> emitNoSpace("\"previous backdrop\"");
                case Random ignored -> emitNoSpace("\"random backdrop\"");
                case WithExpr withExpr when withExpr.getExpression() instanceof StringLiteral str ->
                        emitNoSpace("\"" + escapeString(resolveBackdropPath(str.getText())) + "\"");
                default -> node.getElementChoice().accept(this);
            }
        });
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        emitSelfMethodCall("switch_backdrop_to_and_wait", () -> {
            switch (node.getElementChoice()) {
                case Next ignored -> emitNoSpace("\"next backdrop\"");
                case Prev ignored -> emitNoSpace("\"previous backdrop\"");
                case Random ignored -> emitNoSpace("\"random backdrop\"");
                case WithExpr withExpr when withExpr.getExpression() instanceof StringLiteral str ->
                        emitNoSpace("\"" + escapeString(resolveBackdropPath(str.getText())) + "\"");
                default -> node.getElementChoice().accept(this);
            }
        });
    }

    @Override
    public void visit(NextBackdrop node) {
        emitSimpleCall("next_backdrop");
    }

    @Override
    public void visit(ChangeSizeBy node) {
        emitSelfMethodCall("change_size_by", () -> node.getNum().accept(this));
    }

    @Override
    public void visit(SetSizeTo node) {
        emitSelfMethodCall("set_size_to", () -> node.getPercent().accept(this));
    }

    @Override
    public void visit(Show node) {
        emitSimpleCall("show");
    }

    @Override
    public void visit(Hide node) {
        emitSimpleCall("hide");
    }

    @Override
    public void visit(GoToLayer node) {
        String layerType = node.getLayerChoice().getTypeName();
        if ("back".equals(layerType)) {
            emitSimpleCall("go_to_back_layer");
        } else {
            emitSimpleCall("go_to_front_layer");
        }
    }

    @Override
    public void visit(ChangeLayerBy node) {
        String direction = node.getForwardBackwardChoice().getTypeName();
        emitNoSpace("self.change_layer_by(");
        if ("backward".equals(direction)) {
            emitNoSpace("-(");
            node.getNum().accept(this);
            emitNoSpace(")");
        } else {
            node.getNum().accept(this);
        }
        emitNoSpace(")");
        newLine();
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        emitSimpleCall("clear_graphic_effects");
    }

    @Override
    public void visit(ClearSoundEffects node) {
        emitSimpleCall("clear_sound_effects");
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        String effectName = node.getEffect().getTypeName();
        emitSelfMethodCall("change_" + effectName + "_effect_by", () -> node.getValue().accept(this));
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        String effectName = node.getEffect().getTypeName();
        emitSelfMethodCall("set_" + effectName + "_effect_to", () -> node.getValue().accept(this));
    }

    // ---------------------------------------------------------------
    // Sound Blocks
    // ---------------------------------------------------------------

    @Override
    public void visit(PlaySoundUntilDone node) {
        emitSelfMethodCall("play_sound_until_done", () -> node.getElementChoice().accept(this));
    }

    @Override
    public void visit(StartSound node) {
        emitSelfMethodCall("start_sound", () -> node.getElementChoice().accept(this));
    }

    @Override
    public void visit(StopAllSounds node) {
        emitSimpleCall("stop_all_sounds");
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        emitSelfMethodCall("change_volume_by", () -> node.getVolumeValue().accept(this));
    }

    @Override
    public void visit(SetVolumeTo node) {
        emitSelfMethodCall("set_volume_to", () -> node.getVolumeValue().accept(this));
    }

    // ---------------------------------------------------------------
    // Sensing Blocks
    // ---------------------------------------------------------------

    @Override
    public void visit(AskAndWait node) {
        emitSelfMethodCall("ask_and_wait", () -> node.getQuestion().accept(this));
    }

    @Override
    public void visit(ResetTimer node) {
        emitSimpleCall("reset_timer");
    }

    @Override
    public void visit(SetDragMode node) {
        String dragType = node.getDrag().getTypeName();
        if ("draggable".equals(dragType)) {
            emitNoSpace("self.set_drag_mode_draggable()");
        } else {
            emitNoSpace("self.set_drag_mode_not_draggable()");
        }
        newLine();
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        emitNoSpace("self.touching_color(");
        node.getColor().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(ColorTouchingColor node) {
        emitNoSpace("self.color_is_touching(");
        node.getOperand1().accept(this);
        emitNoSpace(", ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    // ---------------------------------------------------------------
    // Variables
    // ---------------------------------------------------------------

    @Override
    public void visit(SetVariableTo node) {
        String obj = inHandler ? "self" : currentSpritePyName;
        emitNoSpace(obj + ".set_variable(\"");
        emitVariableName(node.getIdentifier());
        emitNoSpace("\", ");
        node.getExpr().accept(this);
        emitNoSpace(")");
        newLine();
    }

    @Override
    public void visit(ChangeVariableBy node) {
        String obj = inHandler ? "self" : currentSpritePyName;
        String varName = escapeString(getIdentifierName(node.getIdentifier()));

        emitNoSpace(obj + ".set_variable(\"" + varName + "\", ");
        emitNoSpace("_to_num(" + obj + ".get_variable(\"" + varName + "\")) + ");
        emitAsNumber(node.getExpr());
        emitNoSpace(")");
        newLine();
    }

    private void emitVariableName(Identifier id) {
        emitNoSpace(escapeString(getIdentifierName(id)));
    }

    // ---------------------------------------------------------------
    // Lists
    // ---------------------------------------------------------------

    /**
     * Special index types used in Scratch list operations.
     */
    private enum ListIndexType {
        LAST, RANDOM, ALL, NUMERIC
    }

    /**
     * Determines the type of list index from an expression.
     */
    private ListIndexType getListIndexType(Expression indexExpr) {
        if (indexExpr instanceof StringLiteral str) {
            String text = str.getText().toLowerCase();
            return switch (text) {
                case "last" -> ListIndexType.LAST;
                case "random" -> ListIndexType.RANDOM;
                case "all" -> ListIndexType.ALL;
                default -> ListIndexType.NUMERIC;
            };
        }
        return ListIndexType.NUMERIC;
    }

    /**
     * Emits a list accessor expression: self.get_variable("listName").
     */
    private void emitListAccessor(String listName) {
        emitNoSpace(SELF + ".get_variable(\"" + listName + "\")");
    }

    /**
     * Emits a list index expression for read access (e.g., [index], [-1], [random]).
     */
    private void emitListReadIndex(String listName, Expression indexExpr, ListIndexType indexType) {
        switch (indexType) {
            case LAST -> emitNoSpace("[-1]");
            case RANDOM -> emitNoSpace("[" + SELF + ".pick_random(1, len(" + SELF
                    + ".get_variable(\"" + listName + "\"))) - 1]");
            default -> {
                emitNoSpace("[int(");
                indexExpr.accept(this);
                emitNoSpace(") - 1]");
            }
        }
    }

    @Override
    public void visit(AddTo node) {
        String listName = escapeString(getIdentifierName(node.getIdentifier()));
        emitListAccessor(listName);
        emitNoSpace(".append(");
        node.getString().accept(this);
        emitNoSpace(")");
        newLine();
    }

    @Override
    public void visit(DeleteOf node) {
        String listName = escapeString(getIdentifierName(node.getIdentifier()));
        Expression indexExpr = node.getNum();
        ListIndexType indexType = getListIndexType(indexExpr);

        switch (indexType) {
            case LAST -> emitNoSpace(SELF + ".get_variable(\"" + listName + "\").pop()");
            case ALL -> emitNoSpace(SELF + ".set_variable(\"" + listName + "\", [])");
            default -> {
                emitListAccessor(listName);
                emitNoSpace(".pop(int(");
                indexExpr.accept(this);
                emitNoSpace(") - 1)");
            }
        }
        newLine();
    }

    @Override
    public void visit(DeleteAllOf node) {
        String listName = escapeString(getIdentifierName(node.getIdentifier()));
        emitNoSpace(SELF + ".set_variable(\"" + listName + "\", [])");
        newLine();
    }

    @Override
    public void visit(InsertAt node) {
        String listName = escapeString(getIdentifierName(node.getIdentifier()));
        Expression indexExpr = node.getIndex();
        ListIndexType indexType = getListIndexType(indexExpr);

        switch (indexType) {
            case LAST -> {
                emitListAccessor(listName);
                emitNoSpace(".append(");
                node.getString().accept(this);
                emitNoSpace(")");
            }
            case RANDOM -> {
                emitListAccessor(listName);
                emitNoSpace(".insert(" + SELF + ".pick_random(1, len(" + SELF
                        + ".get_variable(\"" + listName + "\")) + 1) - 1, ");
                node.getString().accept(this);
                emitNoSpace(")");
            }
            default -> {
                emitListAccessor(listName);
                emitNoSpace(".insert(int(");
                indexExpr.accept(this);
                emitNoSpace(") - 1, ");
                node.getString().accept(this);
                emitNoSpace(")");
            }
        }
        newLine();
    }

    @Override
    public void visit(ReplaceItem node) {
        String listName = escapeString(getIdentifierName(node.getIdentifier()));
        Expression indexExpr = node.getIndex();
        ListIndexType indexType = getListIndexType(indexExpr);

        emitListAccessor(listName);
        emitListReadIndex(listName, indexExpr, indexType);
        emitNoSpace(" = ");
        node.getString().accept(this);
        newLine();
    }

    // ---------------------------------------------------------------
    // Events / Messaging
    // ---------------------------------------------------------------

    @Override
    public void visit(Broadcast node) {
        String messageName = getExpressionString(node.getMessage().getMessage());
        emitNoSpace("self.broadcast(\"" + escapeString(messageName) + "\")");
        newLine();
    }

    @Override
    public void visit(BroadcastAndWait node) {
        String messageName = getExpressionString(node.getMessage().getMessage());
        emitNoSpace("self.broadcast_and_wait(\"" + escapeString(messageName) + "\")");
        newLine();
    }

    // ---------------------------------------------------------------
    // Clone Control
    // ---------------------------------------------------------------

    @Override
    public void visit(CreateCloneOf node) {
        if (node.getCloneChoice() instanceof Myself) {
            emitNoSpace("self._core.control_create_clone_of_myself()");
        } else {
            emitNoSpace("self._core.control_create_clone_of(");
            node.getCloneChoice().accept(this);
            emitNoSpace(")");
        }
        newLine();
    }

    @Override
    public void visit(Myself node) {
        emitNoSpace("\"myself\"");
    }

    @Override
    public void visit(WithCloneExpr node) {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(DeleteClone node) {
        emitNoSpace("self._core.control_delete_this_clone()");
        newLine();
    }

    // ---------------------------------------------------------------
    // Termination
    // ---------------------------------------------------------------

    @Override
    public void visit(StopAll node) {
        emitSimpleCall("stop_all");
    }

    @Override
    public void visit(StopThisScript node) {
        emitNoSpace("return");
        newLine();
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        emitSimpleCall("stop_other_scripts_in_sprite");
    }

    // ---------------------------------------------------------------
    // Custom Blocks (Procedures)
    // ---------------------------------------------------------------

    @Override
    public void visit(ProcedureDefinition node) {
        String procName = toPythonIdentifier(getPyStageProcedureName(node));
        newLine();
        emitNoSpace("def " + currentSpritePyName + "_" + procName + "(self");

        parameterNameMapping.clear();
        List<ParameterDefinition> params = node.getParameterDefinitionList().getParameterDefinitions();
        Set<String> usedParamNames = new HashSet<>();

        for (ParameterDefinition param : params) {
            String originalName = param.getIdent().getName();
            String safeName = toPythonIdentifier(originalName);

            // Ensure uniqueness
            String uniqueName = safeName;
            int counter = 1;
            // Handle empty if toPythonIdentifier returns ""
            while (usedParamNames.contains(uniqueName) || uniqueName.isEmpty()) {
                if (uniqueName.isEmpty()) {
                    uniqueName = "_" + counter;
                } else {
                    uniqueName = safeName + "_" + counter;
                }
                counter++;
            }

            usedParamNames.add(uniqueName);
            parameterNameMapping.put(originalName, uniqueName);

            emitNoSpace(", ");
            emitNoSpace(uniqueName);
        }
        emitNoSpace("):");
        newLine();
        beginIndentation();
        inHandler = true;
        StmtList stmtList = node.getStmtList();
        if (stmtList.getStmts().isEmpty() || containsOnlyUnsupportedStmts(stmtList)) {
            appendIndentation();
            emitNoSpace("pass");
            newLine();
        }
        stmtList.accept(this);
        inHandler = false;
        endIndentation();
    }

    @Override
    public void visit(CallStmt node) {
        String procName = toPythonIdentifier(getPyStageProcedureCallName(node));
        emitNoSpace(currentSpritePyName + "_" + procName + "(self");

        List<Expression> args = node.getExpressions().getExpressions();
        for (Expression arg : args) {
            emitNoSpace(", ");
            arg.accept(this);
        }
        emitNoSpace(")");
        newLine();
    }

    // ---------------------------------------------------------------
    // Expressions - Numbers
    // ---------------------------------------------------------------

    @Override
    public void visit(NumberLiteral node) {
        double value = node.getValue();
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            emitNoSpace(String.valueOf((long) value));
        } else {
            emitNoSpace(String.valueOf(value));
        }
    }

    @Override
    public void visit(AsNumber node) {
        node.getOperand1().accept(this);
    }

    @Override
    public void visit(AsString node) {
        node.getOperand1().accept(this);
    }

    @Override
    public void visit(AsBool node) {
        node.getOperand1().accept(this);
    }

    @Override
    public void visit(Add node) {
        emitNoSpace("(");
        emitAsNumber(node.getOperand1());
        emitNoSpace(" + ");
        emitAsNumber(node.getOperand2());
        emitNoSpace(")");
    }

    @Override
    public void visit(Minus node) {
        emitNoSpace("(");
        emitAsNumber(node.getOperand1());
        emitNoSpace(" - ");
        emitAsNumber(node.getOperand2());
        emitNoSpace(")");
    }

    @Override
    public void visit(Mult node) {
        emitNoSpace("(");
        emitAsNumber(node.getOperand1());
        emitNoSpace(" * ");
        emitAsNumber(node.getOperand2());
        emitNoSpace(")");
    }

    @Override
    public void visit(Div node) {
        emitNoSpace("(");
        emitAsNumber(node.getOperand1());
        emitNoSpace(" / ");
        emitAsNumber(node.getOperand2());
        emitNoSpace(")");
    }

    @Override
    public void visit(Mod node) {
        emitNoSpace("(");
        emitAsNumber(node.getOperand1());
        emitNoSpace(" % ");
        emitAsNumber(node.getOperand2());
        emitNoSpace(")");
    }

    @Override
    public void visit(Round node) {
        emitNoSpace("round(");
        emitAsNumber(node.getOperand1());
        emitNoSpace(")");
    }

    @Override
    public void visit(PickRandom node) {
        emitNoSpace("self.pick_random(");
        emitAsNumber(node.getOperand1());
        emitNoSpace(", ");
        emitAsNumber(node.getOperand2());
        emitNoSpace(")");
    }

    @Override
    public void visit(Timer node) {
        emitNoSpace("self.timer()");
    }

    @Override
    public void visit(MouseX node) {
        emitNoSpace("self.mouse_x()");
    }

    @Override
    public void visit(MouseY node) {
        emitNoSpace("self.mouse_y()");
    }

    @Override
    public void visit(PositionX node) {
        emitNoSpace("self.x_position()");
    }

    @Override
    public void visit(PositionY node) {
        emitNoSpace("self.y_position()");
    }

    @Override
    public void visit(Direction node) {
        emitNoSpace("self.direction()");
    }

    @Override
    public void visit(Size node) {
        emitNoSpace("self.size()");
    }

    @Override
    public void visit(Volume node) {
        emitNoSpace("self.volume()");
    }

    @Override
    public void visit(Loudness node) {
        emitNoSpace("self.loudness()");
    }

    @Override
    public void visit(DistanceTo node) {
        Position pos = node.getPosition();
        if (pos instanceof MousePos) {
            emitCoreMethodCall("sensing_distanceto_mousepointer", () -> {
            });
        } else if (pos instanceof FromExpression fromExpr) {
            emitCoreMethodCall("sensing_distanceto_sprite", () -> {
                emitSpriteReference(fromExpr);
                emitNoSpace("._core");
            });
        } else {
            // Fallback for unknown position types
            emitCoreMethodCall("sensing_distanceto", () -> pos.accept(this));
        }
    }

    @Override
    public void visit(AttributeOf node) {
        // Resolve target sprite variable name
        String targetVar = "stage"; // Default fallback
        ASTNode choice = node.getElementChoice();

        // Unwrap WithExpr if present
        if (choice instanceof WithExpr withExpr) {
            choice = withExpr.getExpression();
        }

        if (choice instanceof StringLiteral str) {
            String targetName = str.getText();
            if (!targetName.equals("_stage_") && !targetName.equals("Stage")) {
                targetVar = toPythonIdentifier(targetName);
            }
        } else if (choice instanceof StrId strId) {
            String targetName = strId.getName();
            if (!targetName.equals("_stage_") && !targetName.equals("Stage")) {
                targetVar = toPythonIdentifier(targetName);
            }
        }

        String objCode = targetVar + "._core";
        Attribute attr = node.getAttribute();

        if (attr instanceof AttributeFromFixed fixed) {
            switch (fixed.getAttribute().getType()) {
                case X_POSITION -> emitNoSpace(objCode + ".motion_xposition()");
                case Y_POSITION -> emitNoSpace(objCode + ".motion_yposition()");
                case DIRECTION -> emitNoSpace(objCode + ".motion_direction()");
                case COSTUME_NUMBER -> emitNoSpace(objCode + ".looks_costumenumbername_number()");
                case COSTUME_NAME -> emitNoSpace(objCode + ".looks_costumenumbername_name()");
                case SIZE -> emitNoSpace(objCode + ".looks_size()");
                case VOLUME -> emitNoSpace(objCode + ".sound_volume()");
                case BACKDROP_NUMBER -> emitNoSpace("stage._core.looks_backdropnumbername_number()");
                case BACKDROP_NAME -> emitNoSpace("stage._core.looks_backdropnumbername_name()");
                default -> {
                }
            }
        } else {
            // Assume it is a variable name
            String attrName = getAttributeString(attr);
            emitNoSpace(objCode + ".data_variable(\"" + escapeString(attrName) + "\")");
        }
    }

    private String getAttributeString(Attribute attr) {
        if (attr instanceof AttributeFromFixed fixed) {
            return fixed.getAttribute().getTypeName();
        } else if (attr instanceof AttributeFromVariable varAttr) {
            return varAttr.getVariable().getName().getName();
        }
        return "unknown";
    }

    @Override
    public void visit(AttributeFromVariable node) {
        // This should generally be handled by getAttributeString inside AttributeOf,
        // but if visited standalone, we return the variable name as string.
        emitNoSpace("\"" + escapeString(node.getVariable().getName().getName()) + "\"");
    }

    @Override
    public void visit(AttributeFromFixed node) {
        emitNoSpace("\"" + escapeString(node.getAttribute().getTypeName()) + "\"");
    }

    @Override
    public void visit(DaysSince2000 node) {
        emitNoSpace("self.days_since_2000()");
    }

    @Override
    public void visit(Current node) {
        emitNoSpace("self.current_");
        emitNoSpace(node.getTimeComp().getType().name().toLowerCase());
        emitNoSpace("()");
    }

    @Override
    public void visit(LengthOfString node) {
        emitNoSpace("len(str(");
        node.getStringExpr().accept(this);
        emitNoSpace("))");
    }

    @Override
    public void visit(LengthOfVar node) {
        emitNoSpace("len(self.get_variable(\"");
        emitNoSpace(escapeString(getIdentifierName(node.getIdentifier())));
        emitNoSpace("\"))");
    }

    @Override
    public void visit(IndexOf node) {
        String listName = escapeString(getIdentifierName(node.getIdentifier()));
        emitNoSpace("_index_of(self.get_variable(\"");
        emitNoSpace(listName);
        emitNoSpace("\"), ");
        node.getExpr().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(ItemOfVariable node) {
        String listName = escapeString(getIdentifierName(node.getIdentifier()));
        Expression indexExpr = node.getNum();
        ListIndexType indexType = getListIndexType(indexExpr);

        emitListAccessor(listName);
        emitListReadIndex(listName, indexExpr, indexType);
    }

    @Override
    public void visit(ExpressionList node) {
        if (node.getExpressions().isEmpty()) {
            emitNoSpace("0");
        } else {
            // In Scratch context, an expression list in a value slot (like set variable)
            // usually implies the first value.
            node.getExpressions().getFirst().accept(this);
        }
    }

    // ---------------------------------------------------------------
    // Expressions - Strings
    // ---------------------------------------------------------------

    @Override
    public void visit(StringLiteral node) {
        emitNoSpace("\"" + escapeString(node.getText()) + "\"");
    }

    @Override
    public void visit(Join node) {
        emitNoSpace("str(");
        node.getOperand1().accept(this);
        emitNoSpace(") + str(");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Answer node) {
        emitNoSpace("self.answer()");
    }

    @Override
    public void visit(Username node) {
        emitNoSpace("self.username()");
    }

    @Override
    public void visit(LetterOf node) {
        emitNoSpace("str(");
        node.getStringExpr().accept(this);
        emitNoSpace(")[int(");
        node.getNum().accept(this);
        emitNoSpace(") - 1]");
    }

    @Override
    public void visit(Costume node) {
        String obj = inHandler ? "self" : currentSpritePyName;
        if (node.getType().getType() == NameNum.NameNumType.NUMBER) {
            emitNoSpace(obj + "._core.looks_costumenumbername_number()");
        } else {
            emitNoSpace(obj + "._core.looks_costumenumbername_name()");
        }
    }

    @Override
    public void visit(Backdrop node) {
        if (node.getType().getType() == NameNum.NameNumType.NUMBER) {
            emitNoSpace("stage._core.looks_backdropnumbername_number()");
        } else {
            emitNoSpace("stage._core.looks_backdropnumbername_name()");
        }
    }

    // ---------------------------------------------------------------
    // Expressions - Boolean
    // ---------------------------------------------------------------

    @Override
    public void visit(And node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" and ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Or node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" or ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Not node) {
        emitNoSpace("not ");
        node.getOperand1().accept(this);
    }

    @Override
    public void visit(BiggerThan node) {
        emitNoSpace("(");
        emitAsNumber(node.getOperand1());
        emitNoSpace(" > ");
        emitAsNumber(node.getOperand2());
        emitNoSpace(")");
    }

    @Override
    public void visit(LessThan node) {
        emitNoSpace("(");
        emitAsNumber(node.getOperand1());
        emitNoSpace(" < ");
        emitAsNumber(node.getOperand2());
        emitNoSpace(")");
    }

    @Override
    public void visit(Equals node) {
        emitNoSpace("_equals(");
        node.getOperand1().accept(this);
        emitNoSpace(", ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Touching node) {
        var touchable = node.getTouchable();
        switch (touchable) {
            case Edge edge -> emitNoSpace("self.touching_edge()");
            case MousePointer mousePointer -> emitNoSpace("self.touching_mouse_pointer()");
            case SpriteTouchable st -> {
                // For sprites, we need to reference the sprite variable, not a string
                StringExprNameVisitor nameVisitor = new StringExprNameVisitor();
                st.getStringExpr().accept(nameVisitor);
                String spriteName = toPythonIdentifier(nameVisitor.getResult());
                emitNoSpace("self.touching(" + spriteName + ")");
            }
            default -> {
                emitNoSpace("self.touching(");
                touchable.accept(this);
                emitNoSpace(")");
            }
        }
    }

    @Override
    public void visit(StringContains node) {
        emitNoSpace("(str(");
        node.getContained().accept(this);
        emitNoSpace(").lower() in str(");
        node.getContaining().accept(this);
        emitNoSpace(").lower())");
    }

    @Override
    public void visit(IsMouseDown node) {
        emitNoSpace("self.mouse_down()");
    }

    @Override
    public void visit(IsKeyPressed node) {
        String keyName = getKeyName(node.getKey());
        if ("any".equals(keyName)) {
            emitNoSpace("any(pygame.key.get_pressed())");
        } else {
            emitNoSpace("self.key_pressed(\"");
            emitNoSpace(keyName);
            emitNoSpace("\")");
        }
    }

    @Override
    public void visit(ListContains node) {
        emitNoSpace("self.list_contains(\"");
        emitNoSpace(escapeString(getIdentifierName(node.getIdentifier())));
        emitNoSpace("\", ");
        node.getElement().accept(this);
        emitNoSpace(")");
    }

    // ---------------------------------------------------------------
    // Identifiers and Variables
    // ---------------------------------------------------------------

    @Override
    public void visit(UnspecifiedExpression node) {
        emitNoSpace("None");
    }

    @Override
    public void visit(UnspecifiedBoolExpr node) {
        emitNoSpace("False");
    }

    @Override
    public void visit(TranslateBlock node) {
        emitNoSpace("\"\"");
    }

    @Override
    public void visit(MusicBlock node) {
        if (node instanceof Stmt) {
            emitNoSpace("# Unhandled node: " + node.getClass().getSimpleName());
            newLine();
            appendIndentation();
            emitNoSpace("pass");
            newLine();
        } else {
            // Music blocks are not supported - emit a neutral value
            emitNoSpace("0");
        }
    }

    @Override
    public void visit(TextToSpeechBlock node) {
        // Text-to-speech blocks are usually statements
        emitNoSpace("# text-to-speech block (not supported)");
        newLine();
        appendIndentation();
        emitNoSpace("pass");
        newLine();
    }

    @Override
    public void visit(MBlockNode node) {
        // mBlock nodes are not supported
        emitNoSpace("# mBlock node (not supported)");
        newLine();
        appendIndentation();
        emitNoSpace("pass");
        newLine();
    }

    @Override
    public void visit(Variable node) {
        emitNoSpace("self.get_variable(\"" + escapeString(node.getName().getName()) + "\")");
    }

    @Override
    public void visit(ScratchList node) {
        emitNoSpace("self.get_list(\"" + escapeString(node.getName().getName()) + "\")");
    }

    @Override
    public void visit(Parameter node) {
        String name = node.getName().getName();
        String mappedName = parameterNameMapping.get(name);
        if (mappedName != null) {
            emitNoSpace(mappedName);
        } else {
            // Fallback if not found (shouldn't happen in valid scope)
            emitNoSpace(toPythonIdentifier(name));
        }
    }

    @Override
    public void visit(StrId node) {
        emitNoSpace("\"" + escapeString(node.getName()) + "\"");
    }

    @Override
    public void visit(Qualified node) {
        // For qualified names (e.g., "Stage.varName"), extract names directly
        // rather than visiting child nodes which would emit method calls
        String varName;
        if (node.getSecond() instanceof Variable v) {
            varName = v.getName().getName();
        } else if (node.getSecond() instanceof ScratchList l) {
            varName = l.getName().getName();
        } else {
            varName = "unknown";
        }

        // In PyStage, stage variables are global and accessible via get_variable from any sprite.
        // The get_variable_of method is not fully implemented in PyStage.
        // So we just use get_variable for all qualified variable access.
        emitNoSpace("self.get_variable(\"" + escapeString(varName) + "\")");
    }

    // ---------------------------------------------------------------
    // Touchables
    // ---------------------------------------------------------------

    @Override
    public void visit(SpriteTouchable node) {
        StringExprNameVisitor nameVisitor = new StringExprNameVisitor();
        node.getStringExpr().accept(nameVisitor);
        emitNoSpace("\"" + escapeString(nameVisitor.getResult()) + "\"");
    }

    @Override
    public void visit(Edge node) {
        emitNoSpace("\"edge\"");
    }

    @Override
    public void visit(MousePointer node) {
        emitNoSpace("\"mouse-pointer\"");
    }

    // ---------------------------------------------------------------
    // Element Choices
    // ---------------------------------------------------------------

    @Override
    public void visit(WithExpr node) {
        node.getExpression().accept(this);
    }

    // ---------------------------------------------------------------
    // Colors
    // ---------------------------------------------------------------

    @Override
    public void visit(ColorLiteral node) {
        emitNoSpace(String.format("\"#%02x%02x%02x\"",
                (int) node.getRed(), (int) node.getGreen(), (int) node.getBlue()));
    }

    /**
     * Helper to cast an expression to a number (float), mimicking Scratch's behavior.
     * Strings that can be parsed as numbers are converted; others become 0.
     */
    private void emitAsNumber(Expression expr) {
        if (expr instanceof NumExpr) {
            // These are already safe numbers
            expr.accept(this);
        } else {
            emitNoSpace("_to_num(");
            expr.accept(this);
            emitNoSpace(")");
        }
    }

    // ---------------------------------------------------------------
    // Helper Methods
    // ---------------------------------------------------------------

    private String toPythonIdentifier(String name) {
        // Convert to valid Python identifier
        // Replace all non-alphanumeric characters (except underscore) with underscore
        String result = name.replaceAll("[^a-zA-Z0-9_]", "_");
        // Ensure it doesn't start with a digit
        if (!result.isEmpty() && Character.isDigit(result.charAt(0))) {
            result = "_" + result;
        }
        // Handle Python keywords
        if (PYTHON_KEYWORDS.contains(result)) {
            result = "_" + result;
        }
        return result;
    }

    @Override
    protected String escapeString(String text) {
        // PyStage requires newline escaping in strings
        return escapeStringWithNewlines(text);
    }

    private String getKeyName(Key key) {
        if (key.getKey() instanceof NumberLiteral num) {
            int keyCode = (int) num.getValue();
            return switch (keyCode) {
                case ScratchKeyCode.SPACE -> "space";
                case ScratchKeyCode.LEFT_ARROW -> "left";
                case ScratchKeyCode.UP_ARROW -> "up";
                case ScratchKeyCode.RIGHT_ARROW -> "right";
                case ScratchKeyCode.DOWN_ARROW -> "down";
                default -> {
                    Character c = ScratchKeyCode.toChar(keyCode);
                    yield c != null ? String.valueOf(c) : "any";
                }
            };
        } else if (key.getKey() instanceof StringLiteral str) {
            String keyName = str.getText();
            // Convert Scratch key names to PyStage key names
            return switch (keyName) {
                case "left arrow" -> "left";
                case "right arrow" -> "right";
                case "up arrow" -> "up";
                case "down arrow" -> "down";
                default -> keyName;
            };
        }
        return "any";
    }

    private String getExpressionString(Expression expr) {
        if (expr instanceof StringLiteral str) {
            return str.getText();
        } else if (expr instanceof AsString asStr) {
            return getExpressionString(asStr.getOperand1());
        } else if (expr instanceof AsNumber asNum) {
            return getExpressionString(asNum.getOperand1());
        } else if (expr instanceof AsBool asBool) {
            return getExpressionString(asBool.getOperand1());
        }
        return "";
    }

    private String getIdentifierName(Identifier id) {
        if (id instanceof Variable v) {
            return v.getName().getName();
        } else if (id instanceof ScratchList l) {
            return l.getName().getName();
        } else if (id instanceof Qualified q) {
            // For qualified names, get the variable/list name (second part)
            return q.getSecond().getName().getName();
        } else if (id instanceof StrId s) {
            return s.getName();
        }
        return "unknown";
    }

    private String getPyStageProcedureName(ProcedureDefinition node) {
        return getProcedureName(node, AbstractExportVisitor::normalizeProcedureName);
    }

    private String getPyStageProcedureCallName(CallStmt node) {
        return getProcedureCallName(node, AbstractExportVisitor::normalizeProcedureName);
    }

    private String getExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex) : "";
    }

    /**
     * Gets the output extension for a file, converting SVG to PNG since PyStage
     * has limited SVG support.
     */
    private String getOutputExtension(String fileName) {
        String extension = getExtension(fileName);
        // Convert SVG to PNG for PyStage compatibility
        if (extension.equalsIgnoreCase(".svg")) {
            return ".png";
        }
        return extension;
    }

    /**
     * Finds all message handler names for the given actor.
     * Returns a map from message name to handler function name.
     *
     * <p>The handler counter is shared across all event types, so we need to
     * iterate through all scripts in order to match the actual naming scheme.
     */
    private Map<String, String> findMessageHandlers(ActorDefinition actor) {
        Map<String, String> handlers = new HashMap<>();
        if (actor == null || actor.getScripts() == null) {
            return handlers;
        }
        String spritePyName = actor.isStage() ? "stage" : toPythonIdentifier(actor.getIdent().getName());
        // Counter is shared across all event types, matching the behavior in visit methods
        int counter = 0;
        for (Script script : actor.getScripts().getScriptList()) {
            Event event = script.getEvent();
            if (event instanceof Never) {
                continue; // Loose blocks are skipped
            }
            if (event instanceof ReceptionOfMessage rom) {
                String messageName = getExpressionString(rom.getMsg().getMessage());
                String handlerName = spritePyName + "_on_message_" + counter;
                handlers.put(messageName, handlerName);
            }
            counter++;
        }
        return handlers;
    }

    /**
     * Resolves an asset name to its image path.
     *
     * @param assetName The asset name to resolve
     * @param actor     The actor to search for the asset in
     * @return The full image path, or the original name if not found
     */
    private String resolveAssetPath(String assetName, ActorDefinition actor) {
        if (actor != null && actor.getActorMetadata() != null && actor.getActorMetadata().getCostumes() != null) {
            for (ImageMetadata costume : actor.getActorMetadata().getCostumes().getList()) {
                if (costume.getName().equals(assetName)) {
                    String extension = getOutputExtension(costume.getMd5ext());
                    String fileName = costume.getName() + extension;
                    return IMAGES_DIR + fileName;
                }
            }
        }
        return assetName;
    }

    private String resolveCostumePath(String costumeName) {
        return resolveAssetPath(costumeName, currentActor);
    }

    private String resolveBackdropPath(String backdropName) {
        return resolveAssetPath(backdropName, getStageActor());
    }

    @Override
    public void visit(NumFunctOf node) {
        String func = node.getOperand1().getTypeName();
        switch (func) {
            case "abs" -> {
                emitNoSpace("math.fabs(");
                node.getOperand2().accept(this);
                emitNoSpace(")");
            }
            case "floor" -> {
                emitNoSpace("math.floor(");
                node.getOperand2().accept(this);
                emitNoSpace(")");
            }
            case "ceiling" -> {
                emitNoSpace("math.ceil(");
                node.getOperand2().accept(this);
                emitNoSpace(")");
            }
            case "sqrt" -> {
                emitNoSpace("math.sqrt(");
                node.getOperand2().accept(this);
                emitNoSpace(")");
            }
            case "sin" -> {
                emitNoSpace("math.sin(math.radians(");
                node.getOperand2().accept(this);
                emitNoSpace("))");
            }
            case "cos" -> {
                emitNoSpace("math.cos(math.radians(");
                node.getOperand2().accept(this);
                emitNoSpace("))");
            }
            case "tan" -> {
                emitNoSpace("math.tan(math.radians(");
                node.getOperand2().accept(this);
                emitNoSpace("))");
            }
            case "asin" -> {
                emitNoSpace("math.degrees(math.asin(");
                node.getOperand2().accept(this);
                emitNoSpace("))");
            }
            case "acos" -> {
                emitNoSpace("math.degrees(math.acos(");
                node.getOperand2().accept(this);
                emitNoSpace("))");
            }
            case "atan" -> {
                emitNoSpace("math.degrees(math.atan(");
                node.getOperand2().accept(this);
                emitNoSpace("))");
            }
            case "ln" -> {
                emitNoSpace("math.log(");
                node.getOperand2().accept(this);
                emitNoSpace(")");
            }
            case "log" -> {
                emitNoSpace("math.log10(");
                node.getOperand2().accept(this);
                emitNoSpace(")");
            }
            case "e ^" -> {
                emitNoSpace("math.exp(");
                node.getOperand2().accept(this);
                emitNoSpace(")");
            }
            case "10 ^" -> {
                emitNoSpace("math.pow(10, ");
                node.getOperand2().accept(this);
                emitNoSpace(")");
            }
            default -> {
                emitNoSpace("0");
            }
        }
    }
}
