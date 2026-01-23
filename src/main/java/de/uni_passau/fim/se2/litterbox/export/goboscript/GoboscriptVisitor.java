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
package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.WithCloneExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.MusicBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.Tempo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.TextToSpeechBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.MutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.ProcedureMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.UnspecifiedStmt;
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
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.ListType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.Type;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.export.AbstractExportVisitor;
import de.uni_passau.fim.se2.litterbox.export.ScratchKeyCode;
import de.uni_passau.fim.se2.litterbox.export.StringExprNameVisitor;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Exports LitterBox AST to Goboscript format.
 *
 * <p>Goboscript is a text-based programming language that compiles to Scratch.
 * Syntax uses C/Rust-style braces and semicolons.
 *
 * <p>Example output:
 * <pre>{@code
 * sprite Stage
 *
 * onflag {
 *     say "Hello!";
 *     forever {
 *         turn_right 15;
 *     }
 * }
 * }</pre>
 *
 * @see <a href="https://aspizu.github.io/goboscript/">Goboscript Documentation</a>
 */
public class GoboscriptVisitor extends AbstractExportVisitor {

    private boolean inScript = false;
    private boolean inProcedure = false;
    private final Set<String> currentProcedureParams = new HashSet<>();
    private String assetPrefix = "";
    private Set<String> availableAssets = null;
    private final Set<String> validCostumes = new HashSet<>();
    private int costumeCount = 0;

    /**
     * Set of escaped variable names declared in the Stage (global scope).
     * These are accessible from all sprites.
     */
    private Set<String> globalVariables = new HashSet<>();

    /**
     * Set of escaped variable names declared in the current sprite (local scope).
     * Cleared at the start of each actor visit.
     */
    private final Set<String> localVariables = new HashSet<>();

    /**
     * Set of escaped list names declared in the Stage (global scope).
     */
    private Set<String> globalLists = new HashSet<>();

    /**
     * Set of escaped list names declared in the current sprite (local scope).
     */
    private final Set<String> localLists = new HashSet<>();

    /**
     * Map of variable names to their initial values (from SetStmtList).
     * Used to emit variable declarations with initial values.
     */
    private final java.util.Map<String, String> variableInitialValues = new java.util.HashMap<>();

    /**
     * Creates a new Goboscript visitor with internal buffering.
     */
    public GoboscriptVisitor() {
        super();
    }

    /**
     * Creates a new Goboscript visitor writing to the given stream.
     *
     * @param printStream The stream to write output to
     */
    public GoboscriptVisitor(PrintStream printStream) {
        super(printStream);
    }

    public void setAssetPrefix(String prefix) {
        this.assetPrefix = prefix;
    }

    /**
     * Sets the set of available asset filenames.
     * When set, only costumes and sounds whose md5ext matches an available asset will be emitted.
     *
     * @param assets Set of available asset filenames (e.g., "bcf454acf82e4504149f7ffe07081dbc.svg")
     */
    public void setAvailableAssets(Set<String> assets) {
        this.availableAssets = assets;
    }

    /**
     * Sets the global variables (declared in Stage) for cross-sprite reference validation.
     * This should be called before visiting non-Stage actors.
     *
     * @param variables Set of escaped variable names from Stage
     */
    public void setGlobalVariables(Set<String> variables) {
        this.globalVariables = new HashSet<>(variables);
    }

    /**
     * Sets the global lists (declared in Stage) for cross-sprite reference validation.
     * This should be called before visiting non-Stage actors.
     *
     * @param lists Set of escaped list names from Stage
     */
    public void setGlobalLists(Set<String> lists) {
        this.globalLists = new HashSet<>(lists);
    }

    /**
     * Returns the set of global variables (declared in Stage).
     * Used by the exporter to pass to sprite visitors.
     *
     * @return Set of escaped variable names
     */
    public Set<String> getGlobalVariables() {
        return new HashSet<>(globalVariables);
    }

    /**
     * Returns the set of global lists (declared in Stage).
     * Used by the exporter to pass to sprite visitors.
     *
     * @return Set of escaped list names
     */
    public Set<String> getGlobalLists() {
        return new HashSet<>(globalLists);
    }

    /**
     * Converts the given AST node to Goboscript format.
     *
     * @param node Some node of the AST
     * @return The Goboscript representation
     */
    public static String of(ASTNode node) {
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.setProgram(AstNodeUtil.findParent(node, Program.class));
        visitor.setCurrentActor(AstNodeUtil.findParent(node, ActorDefinition.class));
        node.accept(visitor);
        return visitor.getOutput();
    }

    // ---------------------------------------------------------------
    // Program Structure
    // ---------------------------------------------------------------

    @Override
    public void visit(Program program) {
        this.program = program;
        // Clear global variables/lists before processing any actors
        globalVariables.clear();
        globalLists.clear();
        program.getActorDefinitionList().accept(this);
    }

    private final Set<String> definedProcedures = new HashSet<>();

    @Override
    public void visit(ActorDefinition node) {
        prepareActorScope(node);

        emitActorHeader(node);

        node.getDecls().accept(this);

        if (node.getActorMetadata() != null) {
            emitAssets(node.getActorMetadata());
        }

        // Emit sprite metadata commands (position, size, direction, visibility, layer order)
        // These must come after costumes/sounds but before procedures/scripts
        if (!node.isStage()) {
            emitSpriteMetadata(node);
        }

        node.getProcedureDefinitionList().accept(this);
        node.getScripts().accept(this);

        currentActor = null;
    }

    private void prepareActorScope(ActorDefinition node) {
        currentActor = node;
        validCostumes.clear();
        costumeCount = 0;
        definedProcedures.clear();
        localVariables.clear();
        localLists.clear();
        variableInitialValues.clear();

        // Collect defined procedures for validation
        for (ProcedureDefinition proc : node.getProcedureDefinitionList().getList()) {
            definedProcedures.add(getGoboscriptProcedureName(proc));
        }

        // Pre-collect initial variable values from SetStmtList for use in declarations
        collectVariableInitialValues(node);
    }

    private void emitActorHeader(ActorDefinition node) {
        String actorType = node.isStage() ? "stage" : "sprite";
        emitNoSpace("# " + actorType + " " + escapeIdentifier(node.getIdent().getName()));
        newLine();
        newLine();
    }

    private void emitAssets(de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata metadata) {
        for (ImageMetadata costume : metadata.getCostumes().getList()) {
            if (!isAssetAvailable(costume.getMd5ext())) {
                continue;
            }
            validCostumes.add(costume.getName());
            costumeCount++;
            emitAssetDeclaration("costumes", costume.getMd5ext(), costume.getName());
        }

        // If all costumes were filtered out, we must emit at least one placeholder costume
        // to satisfy Goboscript's requirement (it errors on "no costumes").
        if (validCostumes.isEmpty() && availableAssets != null && !availableAssets.isEmpty()) {
            String placeholderAsset = availableAssets.iterator().next();
            emitNoSpace("# Warning: All original costumes/backdrops were missing. Using placeholder.");
            newLine();
            emitAssetDeclaration("costumes", placeholderAsset, "placeholder");
            validCostumes.add("placeholder");
            costumeCount++;
        }
        for (SoundMetadata sound : metadata.getSounds().getList()) {
            // Skip sounds that don't exist in the available assets
            if (!isAssetAvailable(sound.getMd5ext())) {
                continue;
            }
            emitAssetDeclaration("sounds", sound.getMd5ext(), sound.getName());
        }
    }

    /**
     * Collects initial variable values from the SetStmtList for use in declarations.
     */
    private void collectVariableInitialValues(ActorDefinition node) {
        for (var stmt : node.getSetStmtList().getStmts()) {
            if (stmt instanceof SetVariableTo setVar) {
                Identifier id = setVar.getIdentifier();
                String varName = null;
                if (id instanceof Variable var) {
                    varName = var.getName().getName();
                } else if (id instanceof Qualified qual && qual.getSecond() instanceof Variable var) {
                    varName = var.getName().getName();
                }
                if (varName != null) {
                    String escapedName = escapeIdentifier(varName);
                    // Get the initial value as a string
                    GoboscriptVisitor tempVisitor = new GoboscriptVisitor();
                    tempVisitor.setProgram(this.program);
                    tempVisitor.setCurrentActor(this.currentActor);
                    setVar.getExpr().accept(tempVisitor);
                    String valueStr = tempVisitor.getOutput().trim();
                    variableInitialValues.put(escapedName, valueStr);
                }
            }
        }
    }

    /**
     * Emits sprite metadata commands for position, size, direction, visibility, and layer order.
     */
    private void emitSpriteMetadata(ActorDefinition node) {
        Double xpos = null;
        Double ypos = null;
        Double size = null;
        Double direction = null;
        Boolean visible = null;
        Integer layerOrder = null;

        // Extract metadata values from SetStmtList
        for (var stmt : node.getSetStmtList().getStmts()) {
            if (stmt instanceof SetAttributeTo setAttr) {
                String attrName = getAttributeNameFromExpr(setAttr.getStringExpr());
                if (attrName == null) {
                    continue;
                }
                Double numValue = getNumericValue(setAttr.getExpr());
                switch (attrName) {
                    case "x" -> xpos = numValue;
                    case "y" -> ypos = numValue;
                    case "size" -> size = numValue;
                    case "direction" -> direction = numValue;
                    case "visible" -> {
                        // Only set visible if we have a valid numeric value
                        // Otherwise leave it null to avoid incorrectly emitting hide;
                        if (numValue != null) {
                            visible = numValue != 0;
                        }
                    }
                    case "layerOrder" -> layerOrder = numValue != null ? numValue.intValue() : null;
                    default -> { }
                }
            }
        }

        // Emit layer order first (if present and not default)
        if (layerOrder != null && layerOrder != 1) {
            emitNoSpace("set_layer_order " + layerOrder + ";");
            newLine();
        }

        // Emit visibility if hidden (visible=false means emit hide;)
        if (visible != null && !visible) {
            emitNoSpace("hide;");
            newLine();
        }

        // Emit position if non-zero
        if (xpos != null && xpos != 0) {
            emitNoSpace("set_x " + formatNumber(xpos) + ";");
            newLine();
        }
        if (ypos != null && ypos != 0) {
            emitNoSpace("set_y " + formatNumber(ypos) + ";");
            newLine();
        }

        // Emit size if not default (100)
        if (size != null && size != 100) {
            emitNoSpace("set_size " + formatNumber(size) + ";");
            newLine();
        }

        // Emit direction if not default (90)
        if (direction != null && direction != 90) {
            emitNoSpace("point_in_direction " + formatNumber(direction) + ";");
            newLine();
        }
    }

    /**
     * Extracts the attribute name from a StringExpr.
     */
    private String getAttributeNameFromExpr(StringExpr expr) {
        if (expr instanceof StringLiteral lit) {
            return lit.getText();
        } else if (expr instanceof FixedAttribute fixedAttr) {
            return fixedAttr.getTypeName();
        }
        return null;
    }

    /**
     * Extracts a numeric value from an expression if it's a NumberLiteral.
     */
    private Double getNumericValue(de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression expr) {
        if (expr instanceof NumberLiteral num) {
            return num.getValue();
        }
        return null;
    }

    /**
     * Formats a number for output, using integer format when appropriate.
     */
    private String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    @Override
    public void visit(DeclarationStmtList node) {
        for (DeclarationStmt stmt : node.getDeclarationStmts()) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(DeclarationIdentAsTypeStmt node) {
        Type type = node.getType();
        String rawName = node.getIdent().getName().getName();
        if (type instanceof ListType) {
            // List declarations use "_list" suffix to avoid name collisions with variables
            String escapedName = escapeListIdentifier(rawName);
            emitNoSpace("list ");
            emitNoSpace(escapedName);
            // Track declared lists for later validation
            if (currentActor != null && currentActor.isStage()) {
                globalLists.add(escapedName);
            } else {
                localLists.add(escapedName);
            }
        } else {
            // Variable declarations
            String escapedName = escapeIdentifier(rawName);
            emitNoSpace("var ");
            emitNoSpace(escapedName);
            // Track declared variables for later validation
            if (currentActor != null && currentActor.isStage()) {
                globalVariables.add(escapedName);
            } else {
                localVariables.add(escapedName);
            }
            // Emit initial value if available
            String initialValue = variableInitialValues.get(escapedName);
            if (initialValue != null && !initialValue.isEmpty()) {
                emitNoSpace(" = ");
                emitNoSpace(initialValue);
            }
        }
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(DeclarationAttributeAsTypeStmt node) {
        // Built-in attributes are implicitly declared in Goboscript
    }

    @Override
    public void visit(DeclarationBroadcastStmt node) {
        // Broadcasts are implicit in Goboscript
    }

    @Override
    public void visit(ProcedureDefinitionList node) {
        for (ProcedureDefinition proc : node.getList()) {
            proc.accept(this);
            newLine();
        }
    }

    @Override
    public void visit(ScriptList node) {
        for (Script script : node.getScriptList()) {
            script.accept(this);
            newLine();
        }
    }

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            return; // Skip loose blocks
        }
        inScript = true;
        script.getEvent().accept(this);
        inScript = false;
    }

    @Override
    public void visit(StmtList node) {
        for (var stmt : node.getStmts()) {
            appendIndentation();
            stmt.accept(this);
        }
    }

    /**
     * Handles orphan reporter blocks (expressions not connected to any statement).
     * In Scratch, these disconnected blocks don't execute, so we skip them entirely.
     * This prevents invalid Goboscript output like standalone numbers (e.g., "0.25").
     */
    @Override
    public void visit(ExpressionStmt node) {
        // Orphan blocks don't execute in Scratch - skip them entirely
    }

    /**
     * Handles music extension blocks (play note, set tempo, etc.).
     * Goboscript does not support the music extension, so we skip these blocks entirely.
     * This prevents invalid output from the block's children being visited.
     */

    @Override
    public void visit(MusicBlock node) {
        if (node instanceof Tempo) {
            emitNoSpace("60");
        }
        // Other Music blocks are not supported in goboscript - skip them
    }

    /**
     * Handles text-to-speech extension blocks (speak, set voice, set language).
     * Goboscript does not support the text-to-speech extension, so we skip these blocks entirely.
     * This prevents invalid output from the block's children being visited.
     */
    @Override
    public void visit(TextToSpeechBlock node) {
        // Text-to-speech extension blocks are not supported in goboscript - skip them
    }

    // ---------------------------------------------------------------
    // Event Handlers (Hat Blocks)
    // ---------------------------------------------------------------

    @Override
    public void visit(GreenFlag greenFlag) {
        emitNoSpace("onflag ");
        emitBlock(greenFlag);
    }

    @Override
    public void visit(KeyPressed keyPressed) {
        emitNoSpace("onkey ");
        Key key = keyPressed.getKey();
        if (key.getKey() instanceof NumberLiteral num) {
            emitNoSpace("\"" + getKeyName((int) num.getValue()) + "\" ");
        } else {
            key.getKey().accept(this);
        }
        emitBlock(keyPressed);
    }

    @Override
    public void visit(SpriteClicked spriteClicked) {
        emitNoSpace("onclick ");
        emitBlock(spriteClicked);
    }

    @Override
    public void visit(StageClicked stageClicked) {
        emitNoSpace("onclick ");
        emitBlock(stageClicked);
    }

    @Override
    public void visit(BackdropSwitchTo backdrop) {
        emitNoSpace("onbackdrop ");
        backdrop.getBackdrop().accept(this);
        emitNoSpace(" ");
        emitBlock(backdrop);
    }

    @Override
    public void visit(ReceptionOfMessage receptionOfMessage) {
        emitNoSpace("on ");
        Message message = receptionOfMessage.getMsg();
        if (message.getMessage() instanceof StringLiteral literal) {
            emitNoSpace("\"" + escapeString(literal.getText()) + "\" ");
        } else {
            message.getMessage().accept(this);
        }
        emitBlock(receptionOfMessage);
    }

    @Override
    public void visit(StartedAsClone startedAsClone) {
        emitNoSpace("onclone ");
        emitBlock(startedAsClone);
    }

    @Override
    public void visit(AttributeAboveValue node) {
        String attr = getAttributeName(node.getAttribute());
        emitNoSpace("on" + attr + " > ");
        node.getValue().accept(this);
        emitNoSpace(" ");
        emitBlock(node);
    }

    @Override
    public void visit(Never never) {
        // Loose blocks - skip
    }

    // ---------------------------------------------------------------
    // Control Structures
    // ---------------------------------------------------------------

    @Override
    public void visit(IfThenStmt ifThenStmt) {
        emitNoSpace("if ");
        ifThenStmt.getBoolExpr().accept(this);
        emitNoSpace(" {");
        newLine();
        beginIndentation();
        ifThenStmt.getThenStmts().accept(this);
        endIndentation();
        appendIndentation();
        emitNoSpace("}");
        newLine();
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        emitNoSpace("if ");
        ifElseStmt.getBoolExpr().accept(this);
        emitNoSpace(" {");
        newLine();
        beginIndentation();
        ifElseStmt.getThenStmts().accept(this);
        endIndentation();
        appendIndentation();
        emitNoSpace("} else {");
        newLine();
        beginIndentation();
        ifElseStmt.getElseStmts().accept(this);
        endIndentation();
        appendIndentation();
        emitNoSpace("}");
        newLine();
    }

    @Override
    public void visit(RepeatForeverStmt repeatForeverStmt) {
        emitNoSpace("forever {");
        newLine();
        beginIndentation();
        repeatForeverStmt.getStmtList().accept(this);
        endIndentation();
        appendIndentation();
        emitNoSpace("}");
        newLine();
    }

    @Override
    public void visit(RepeatTimesStmt repeatTimesStmt) {
        emitNoSpace("repeat ");
        repeatTimesStmt.getTimes().accept(this);
        emitNoSpace(" {");
        newLine();
        beginIndentation();
        repeatTimesStmt.getStmtList().accept(this);
        endIndentation();
        appendIndentation();
        emitNoSpace("}");
        newLine();
    }

    @Override
    public void visit(UntilStmt untilStmt) {
        emitNoSpace("until ");
        untilStmt.getBoolExpr().accept(this);
        emitNoSpace(" {");
        newLine();
        beginIndentation();
        untilStmt.getStmtList().accept(this);
        endIndentation();
        appendIndentation();
        emitNoSpace("}");
        newLine();
    }

    @Override
    public void visit(WaitSeconds node) {
        emitNoSpace("wait ");
        node.getSeconds().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(WaitUntil node) {
        emitNoSpace("until ");
        node.getUntil().accept(this);
        emitNoSpace(" {}"); // Empty loop
        newLine();
    }

    // ---------------------------------------------------------------
    // Motion Blocks
    // ---------------------------------------------------------------

    @Override
    public void visit(MoveSteps node) {
        emitNoSpace("move ");
        if (node.getSteps() instanceof de.uni_passau.fim.se2.litterbox.ast.model.expression.num.UnspecifiedNumExpr) {
            emitNoSpace("10"); // Default Scratch value
        } else {
            node.getSteps().accept(this);
        }
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(TurnLeft node) {
        emitNoSpace("turn_left ");
        node.getDegrees().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(TurnRight node) {
        emitNoSpace("turn_right ");
        node.getDegrees().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(GoToPos node) {
        Position pos = node.getPosition();
        if (pos instanceof RandomPos) {
            emitNoSpace("goto_random_position;");
        } else if (pos instanceof MousePos) {
            emitNoSpace("goto_mouse_pointer;");
        } else {
            emitNoSpace("goto ");
            pos.accept(this);
            emitNoSpace(";");
        }
        newLine();
    }

    @Override
    public void visit(GoToPosXY node) {
        emitNoSpace("goto ");
        node.getX().accept(this);
        emitNoSpace(", ");
        node.getY().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(GlideSecsTo node) {
        Position pos = node.getPosition();
        if (pos instanceof RandomPos) {
            emitNoSpace("glide_to_random_position ");
            node.getSecs().accept(this);
            emitNoSpace(";");
        } else if (pos instanceof MousePos) {
            emitNoSpace("glide_to_mouse_pointer ");
            node.getSecs().accept(this);
            emitNoSpace(";");
        } else {
            // glide_to with sprite name is not supported in Goboscript compiler
            // Use goto instead (instant teleport, no gliding animation)
            emitNoSpace("goto ");
            pos.accept(this);
            emitNoSpace("; # was: glide_to ");
            node.getSecs().accept(this);
            emitNoSpace("s");
        }
        newLine();
    }

    @Override
    public void visit(GlideSecsToXY node) {
        emitNoSpace("glide ");
        node.getSecs().accept(this);
        emitNoSpace(", ");
        node.getX().accept(this);
        emitNoSpace(", ");
        node.getY().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(PointInDirection node) {
        emitNoSpace("point_in_direction ");
        node.getDirection().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(PointTowards node) {
        Position pos = node.getPosition();
        if (pos instanceof RandomPos) {
            emitNoSpace("point_towards_random_direction;");
        } else if (pos instanceof MousePos) {
            emitNoSpace("point_towards_mouse_pointer;");
        } else {
            emitNoSpace("point_towards ");
            pos.accept(this);
            emitNoSpace(";");
        }
        newLine();
    }

    @Override
    public void visit(ChangeXBy node) {
        emitNoSpace("change_x ");
        node.getNum().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(ChangeYBy node) {
        emitNoSpace("change_y ");
        node.getNum().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(SetXTo node) {
        emitNoSpace("set_x ");
        node.getNum().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(SetYTo node) {
        emitNoSpace("set_y ");
        node.getNum().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(IfOnEdgeBounce node) {
        emitNoSpace("if_on_edge_bounce;");
        newLine();
    }

    @Override
    public void visit(SetRotationStyle node) {
        switch (node.getRotation().getType()) {
            case left_right -> emitNoSpace("set_rotation_style_left_right;");
            case dont_rotate -> emitNoSpace("set_rotation_style_do_not_rotate;");
            case all_around -> emitNoSpace("set_rotation_style_all_around;");
            default -> {
            }
        }
        newLine();
    }

    // ---------------------------------------------------------------
    // Looks Blocks
    // ---------------------------------------------------------------

    @Override
    public void visit(Say node) {
        emitNoSpace("say ");
        node.getString().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(SayForSecs node) {
        emitNoSpace("say ");
        node.getString().accept(this);
        emitNoSpace(", ");
        node.getSecs().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(Think node) {
        emitNoSpace("think ");
        node.getThought().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(ThinkForSecs node) {
        emitNoSpace("think ");
        node.getThought().accept(this);
        emitNoSpace(", ");
        node.getSecs().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        if (handleSpecialSwitchCases(
                "switch_costume",
                "switch_costume",
                node.getCostumeChoice(),
                validCostumes::contains)) {
            return;
        }

        emitNoSpace("switch_costume ");
        node.getCostumeChoice().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(NextCostume node) {
        emitNoSpace("next_costume;");
        newLine();
    }

    @Override
    public void visit(SwitchBackdrop node) {
        visitSwitchBackdropCommon(node.getElementChoice(), "switch_backdrop");
    }

    @Override
    public void visit(NextBackdrop node) {
        emitNoSpace("next_backdrop;");
        newLine();
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        visitSwitchBackdropCommon(node.getElementChoice(), "switch_backdrop_and_wait");
    }

    private void visitSwitchBackdropCommon(de.uni_passau.fim.se2.litterbox.ast.model.ASTNode choice,
                                           String commentName) {
        if (handleSpecialSwitchCases("switch_backdrop", commentName, choice, this::isBackdropAvailable)) {
            return;
        }

        switch (choice) {
            case Next ignored -> {
                emitNoSpace("next_backdrop;");
                newLine();
            }
            case Prev ignored -> {
                emitNoSpace("previous_backdrop;");
                newLine();
            }
            case Random ignored -> {
                emitNoSpace("random_backdrop;");
                newLine();
            }
            default -> {
                emitNoSpace("switch_backdrop ");
                choice.accept(this);
                emitNoSpace(";");
                newLine();
            }
        }
    }

    private boolean handleSpecialSwitchCases(String command, String commentName,
                                             de.uni_passau.fim.se2.litterbox.ast.model.ASTNode choice,
                                             java.util.function.Predicate<String> validityCheck) {
        String name = resolveStringElement(choice);
        if (name != null) {
            if (!validityCheck.test(name)) {
                emitNoSpace("# " + commentName + " \"" + escapeString(name) + "\";");
                newLine();
                return true;
            }
        }

        if (choice instanceof WithExpr withExpr) {
            if (withExpr.getExpression() instanceof NumExpr) {
                int count = costumeCount;
                if (count > 0) {
                    emitNoSpace(command + " ((");
                    withExpr.accept(this);
                    emitNoSpace(" - 1) % " + count + " + 1);");
                    newLine();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void visit(ChangeSizeBy node) {
        emitNoSpace("change_size ");
        node.getNum().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(SetSizeTo node) {
        emitNoSpace("set_size ");
        node.getPercent().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(Show node) {
        emitNoSpace("show;");
        newLine();
    }

    @Override
    public void visit(Hide node) {
        emitNoSpace("hide;");
        newLine();
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        visitGraphicEffectsCommon(node.getEffect(), node.getValue(), "change");
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        visitGraphicEffectsCommon(node.getEffect(), node.getValue(), "set");
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        emitNoSpace("clear_graphic_effects;");
        newLine();
    }

    @Override
    public void visit(GoToLayer node) {
        String layer = node.getLayerChoice().getTypeName().toLowerCase();
        // "front" or "back"
        emitNoSpace("goto_" + layer + ";");
        newLine();
    }

    @Override
    public void visit(ChangeLayerBy node) {
        String direction = node.getForwardBackwardChoice().getTypeName().toLowerCase();
        // "forward" or "backward"
        emitNoSpace("go_" + direction + " ");
        node.getNum().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(ShowVariable node) {
        String escapedName = getEscapedVariableName(node.getIdentifier());
        if (isVariableDeclared(escapedName)) {
            emitNoSpace("show ");
            node.getIdentifier().accept(this);
            emitNoSpace(";");
            newLine();
        } else {
            // Variable not declared - emit a comment to avoid compilation error
            emitNoSpace("# Skipped: show undeclared variable");
            newLine();
        }
    }

    @Override
    public void visit(HideVariable node) {
        String escapedName = getEscapedVariableName(node.getIdentifier());
        if (isVariableDeclared(escapedName)) {
            emitNoSpace("hide ");
            node.getIdentifier().accept(this);
            emitNoSpace(";");
            newLine();
        } else {
            // Variable not declared - emit a comment to avoid compilation error
            emitNoSpace("# Skipped: hide undeclared variable");
            newLine();
        }
    }

    @Override
    public void visit(ShowList node) {
        String escapedName = getEscapedListName(node.getIdentifier());
        if (isListDeclared(escapedName)) {
            emitNoSpace("show ");
            node.getIdentifier().accept(this);
            emitNoSpace(";");
            newLine();
        } else {
            // List not declared - emit a comment to avoid compilation error
            emitNoSpace("# Skipped: show undeclared list");
            newLine();
        }
    }

    @Override
    public void visit(HideList node) {
        String escapedName = getEscapedListName(node.getIdentifier());
        if (isListDeclared(escapedName)) {
            emitNoSpace("hide ");
            node.getIdentifier().accept(this);
            emitNoSpace(";");
            newLine();
        } else {
            // List not declared - emit a comment to avoid compilation error
            emitNoSpace("# Skipped: hide undeclared list");
            newLine();
        }
    }

    /**
     * Extracts and escapes the variable name from an identifier.
     */
    private String getEscapedVariableName(Identifier identifier) {
        if (identifier instanceof Variable var) {
            return escapeIdentifier(var.getName().getName());
        } else if (identifier instanceof Qualified qual) {
            // For qualified identifiers, get the variable part
            if (qual.getSecond() instanceof Variable var) {
                return escapeIdentifier(var.getName().getName());
            }
        }
        return null;
    }

    /**
     * Extracts and escapes the list name from an identifier.
     */
    private String getEscapedListName(Identifier identifier) {
        if (identifier instanceof ScratchList list) {
            return escapeListIdentifier(list.getName().getName());
        } else if (identifier instanceof Qualified qual) {
            // For qualified identifiers, get the list part
            if (qual.getSecond() instanceof ScratchList list) {
                return escapeListIdentifier(list.getName().getName());
            }
        }
        return null;
    }

    /**
     * Checks if a variable is declared (in global or local scope).
     */
    private boolean isVariableDeclared(String escapedName) {
        if (escapedName == null) {
            return true; // Unknown identifier type - assume declared
        }
        return globalVariables.contains(escapedName) || localVariables.contains(escapedName);
    }

    /**
     * Checks if a list is declared (in global or local scope).
     */
    private boolean isListDeclared(String escapedName) {
        if (escapedName == null) {
            return true; // Unknown identifier type - assume declared
        }
        return globalLists.contains(escapedName) || localLists.contains(escapedName);
    }

    // ---------------------------------------------------------------
    // Sound Blocks
    // ---------------------------------------------------------------

    @Override
    public void visit(PlaySoundUntilDone node) {
        emitNoSpace("play_sound_until_done ");
        node.getElementChoice().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(StartSound node) {
        emitNoSpace("start_sound ");
        node.getElementChoice().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(StopAllSounds node) {
        emitNoSpace("stop_all_sounds;");
        newLine();
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        emitNoSpace("change_volume ");
        node.getVolumeValue().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(SetVolumeTo node) {
        emitNoSpace("set_volume ");
        node.getVolumeValue().accept(this);
        emitNoSpace(";");
        newLine();
    }

    private void visitGraphicEffectsCommon(GraphicEffect graphicEffect, ASTNode value, String operation) {
        String effect = graphicEffect.getTypeName().toLowerCase();
        emitNoSpace(operation + "_" + effect + "_effect ");
        value.accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        String effect = node.getEffect().getType() == SoundEffect.SoundEffectType.PITCH ? "pitch" : "pan";
        emitNoSpace("change_" + effect + "_effect ");
        node.getValue().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        String effect = node.getEffect().getType() == SoundEffect.SoundEffectType.PITCH ? "pitch" : "pan";
        emitNoSpace("set_" + effect + "_effect ");
        node.getValue().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(ClearSoundEffects node) {
        emitNoSpace("clear_sound_effects;");
        newLine();
    }

    // ---------------------------------------------------------------
    // Sensing Blocks
    // ---------------------------------------------------------------

    @Override
    public void visit(AskAndWait node) {
        emitNoSpace("ask ");
        node.getQuestion().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(ResetTimer node) {
        emitNoSpace("reset_timer;");
        newLine();
    }

    @Override
    public void visit(SetDragMode node) {
        if ("draggable".equals(node.getDrag().getTypeName())) {
            emitNoSpace("set_drag_mode_draggable;");
        } else {
            emitNoSpace("set_drag_mode_not_draggable;");
        }
        newLine();
    }

    // ---------------------------------------------------------------
    // Pen Extension
    // ---------------------------------------------------------------

    @Override
    public void visit(PenBlock node) {
        if (node instanceof PenClearStmt) {
            emitNoSpace("erase_all;");
            newLine();
        } else if (node instanceof PenStampStmt) {
            emitNoSpace("stamp;");
            newLine();
        } else if (node instanceof PenDownStmt) {
            emitNoSpace("pen_down;");
            newLine();
        } else if (node instanceof PenUpStmt) {
            emitNoSpace("pen_up;");
            newLine();
        } else if (node instanceof SetPenColorToColorStmt stmt) {
            emitNoSpace("set_pen_color(");
            stmt.getColorExpr().accept(this);
            emitNoSpace(");");
            newLine();
        } else if (node instanceof ChangePenSizeBy stmt) {
            emitNoSpace("change_pen_size(");
            stmt.getValue().accept(this);
            emitNoSpace(");");
            newLine();
        } else if (node instanceof SetPenSizeTo stmt) {
            emitNoSpace("set_pen_size(");
            stmt.getValue().accept(this);
            emitNoSpace(");");
            newLine();
        } else if (node instanceof ChangePenColorParamBy stmt) {
            String param = "hue"; // Default
            if (stmt.getParam() instanceof FixedColorParam fixed) {
                param = fixed.getTypeName();
                if ("color".equals(param)) {
                    param = "hue";
                }
            }
            emitNoSpace("change_pen_" + param + "(");
            stmt.getValue().accept(this);
            emitNoSpace(");");
            newLine();
        } else if (node instanceof SetPenColorParamTo stmt) {
            String param = "hue"; // Default
            if (stmt.getParam() instanceof FixedColorParam fixed) {
                param = fixed.getTypeName();
                if ("color".equals(param)) {
                    param = "hue";
                }
            }
            emitNoSpace("set_pen_" + param + "(");
            stmt.getValue().accept(this);
            emitNoSpace(");");
            newLine();
        } else {
            // Fallback
            visitChildren(node);
        }
    }

    // ---------------------------------------------------------------
    // Variables
    // ---------------------------------------------------------------

    @Override
    public void visit(SetVariableTo node) {
        // Skip list initializations - lists are declared with "list name;" and don't need
        // assignment statements. List operations within scripts are handled separately.
        Identifier id = node.getIdentifier();
        if (id instanceof Qualified q) {
            if (q.getSecond() instanceof ScratchList) {
                return;
            }
        } else if (id instanceof ScratchList) {
            return;
        }

        handleVariableAssignment(id);
        emitNoSpace(" = ");
        node.getExpr().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(ChangeVariableBy node) {
        handleVariableAssignment(node.getIdentifier());
        emitNoSpace(" += ");
        node.getExpr().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(SetAttributeTo node) {
        // Attribute initializations (volume, tempo, layer order, etc.) are not needed in Goboscript
        // These are handled automatically by the Scratch runtime
    }

    private void handleVariableAssignment(Identifier id) {
        if (id instanceof Qualified q) {
            String qualifier = q.getFirst().getName();
            // If the qualifier is the current actor or explicitly "Stage",
            // we strip it and let visit(Variable) handle the correct qualification/scoping.
            boolean isSelf = currentActor != null && qualifier.equals(currentActor.getIdent().getName());
            boolean isStage = qualifier.equals("Stage");

            if (isSelf || isStage) {
                q.getSecond().accept(this);
            } else {
                id.accept(this);
            }
        } else {
            id.accept(this);
        }
    }

    // ---------------------------------------------------------------
    // Lists
    // ---------------------------------------------------------------

    @Override
    public void visit(AddTo node) {
        emitNoSpace("add ");
        node.getString().accept(this);
        emitNoSpace(" to ");
        node.getIdentifier().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(DeleteOf node) {
        emitNoSpace("delete ");
        node.getIdentifier().accept(this);
        emitNoSpace("[");
        node.getNum().accept(this);
        emitNoSpace("];");
        newLine();
    }

    @Override
    public void visit(DeleteAllOf node) {
        emitNoSpace("delete ");
        node.getIdentifier().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(InsertAt node) {
        emitNoSpace("insert ");
        node.getString().accept(this);
        emitNoSpace(" at ");
        node.getIdentifier().accept(this);
        emitNoSpace("[");
        node.getIndex().accept(this);
        emitNoSpace("];");
        newLine();
    }

    @Override
    public void visit(ReplaceItem node) {
        node.getIdentifier().accept(this);
        emitNoSpace("[");
        node.getIndex().accept(this);
        emitNoSpace("] = ");
        node.getString().accept(this);
        emitNoSpace(";");
        newLine();
    }

    // ---------------------------------------------------------------
    // Events / Messaging
    // ---------------------------------------------------------------

    @Override
    public void visit(Broadcast node) {
        emitNoSpace("broadcast ");
        node.getMessage().accept(this);
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(BroadcastAndWait node) {
        emitNoSpace("broadcast_and_wait ");
        node.getMessage().accept(this);
        emitNoSpace(";");
        newLine();
    }

    // ---------------------------------------------------------------
    // Clone Control
    // ---------------------------------------------------------------

    @Override
    public void visit(CreateCloneOf node) {
        emitNoSpace("clone");
        if (!(node.getCloneChoice() instanceof Myself)) {
            emitNoSpace(" ");
            node.getCloneChoice().accept(this);
        }
        emitNoSpace(";");
        newLine();
    }

    @Override
    public void visit(Myself node) {
        emitNoSpace("myself");
    }

    @Override
    public void visit(WithCloneExpr node) {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(DeleteClone node) {
        emitNoSpace("delete_this_clone;");
        newLine();
    }

    // ---------------------------------------------------------------
    // Termination
    // ---------------------------------------------------------------

    @Override
    public void visit(StopAll node) {
        emitNoSpace("stop_all;");
        newLine();
    }

    @Override
    public void visit(StopThisScript node) {
        emitNoSpace("stop_this_script;");
        newLine();
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        emitNoSpace("stop_other_scripts;");
        newLine();
    }

    // ---------------------------------------------------------------
    // Custom Blocks (Procedures)
    // ---------------------------------------------------------------

    @Override
    public void visit(ProcedureDefinition node) {
        String procName = getGoboscriptProcedureName(node);

        // Determine if the procedure runs with warp (without screen refresh)
        // In goboscript: "proc" = warp (run without screen refresh)
        //                "nowarp proc" = no warp (normal execution with screen refresh)
        boolean isWarp = isProcedureWarp(node);
        if (isWarp) {
            emitNoSpace("proc " + escapeProcedureName(procName));
        } else {
            emitNoSpace("nowarp proc " + escapeProcedureName(procName));
        }

        List<ParameterDefinition> params = node.getParameterDefinitionList().getParameterDefinitions();

        // Track parameter names for validation during body traversal
        currentProcedureParams.clear();
        for (ParameterDefinition param : params) {
            currentProcedureParams.add(param.getIdent().getName());
        }

        if (!params.isEmpty()) {
            emitNoSpace(" ");
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) {
                    emitNoSpace(", ");
                }
                ParameterDefinition param = params.get(i);
                emitNoSpace(escapeIdentifier(param.getIdent().getName()));
            }
        }
        emitNoSpace(" {");
        newLine();
        beginIndentation();
        inScript = true;
        inProcedure = true;
        node.getStmtList().accept(this);
        inProcedure = false;
        inScript = false;
        currentProcedureParams.clear();
        endIndentation();
        emitNoSpace("}");
        newLine();
    }

    @Override
    public void visit(CallStmt node) {
        String procName = getGoboscriptProcedureCallName(node);

        // Check if procedure is defined in this actor
        if (!definedProcedures.contains(procName)) {
            emitNoSpace("# Unsupported procedure call (cross-sprite or undefined): "
                    + escapeProcedureName(procName) + ";");
            newLine();
            return;
        }

        emitNoSpace(escapeProcedureName(procName));
        if (!node.getExpressions().getExpressions().isEmpty()) {
            emitNoSpace(" ");
            List<Expression> args = node.getExpressions().getExpressions();
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) {
                    emitNoSpace(", ");
                }
                args.get(i).accept(this);
            }
        }
        emitNoSpace(";");
        newLine();
    }

    // ---------------------------------------------------------------
    // Expressions - Numbers
    // ---------------------------------------------------------------

    @Override
    public void visit(NumberLiteral node) {
        double value = node.getValue();
        if (Double.isInfinite(value)) {
            if (value > 0) {
                emitNoSpace("(1.0 / 0.0)");
            } else {
                emitNoSpace("(-1.0 / 0.0)");
            }
        } else if (Double.isNaN(value)) {
            emitNoSpace("(0.0 / 0.0)");
        } else if (value == Math.floor(value)) {
            emitNoSpace(String.valueOf((long) value));
        } else {
            emitNoSpace(String.valueOf(value));
        }
    }

    @Override
    public void visit(Add node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" + ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Minus node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" - ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Mult node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" * ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Div node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" / ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Mod node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" % ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Round node) {
        emitNoSpace("round ");
        node.getOperand1().accept(this);
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
    public void visit(NumFunctOf node) {
        String func = node.getOperand1().getType().getFunction();
        if ("10 ^".equals(func)) {
            func = "pow10";
        } else if ("e ^".equals(func)) {
            func = "exp";
        } else if ("ceiling".equals(func)) {
            func = "ceil";
        }
        emitNoSpace(func + "(");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(PickRandom node) {
        emitNoSpace("random(");
        node.getOperand1().accept(this);
        emitNoSpace(", ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Timer node) {
        emitNoSpace("timer()");
    }

    @Override
    public void visit(MouseX node) {
        emitNoSpace("mouse_x()");
    }

    @Override
    public void visit(MouseY node) {
        emitNoSpace("mouse_y()");
    }

    @Override
    public void visit(PositionX node) {
        emitNoSpace("x_position()");
    }

    @Override
    public void visit(PositionY node) {
        emitNoSpace("y_position()");
    }

    @Override
    public void visit(Direction node) {
        emitNoSpace("direction()");
    }

    @Override
    public void visit(Size node) {
        emitNoSpace("size()");
    }

    @Override
    public void visit(Volume node) {
        emitNoSpace("volume()");
    }

    @Override
    public void visit(Loudness node) {
        emitNoSpace("loudness()");
    }

    @Override
    public void visit(DistanceTo node) {
        Position pos = node.getPosition();
        if (pos instanceof MousePos) {
            emitNoSpace("distance_to_mouse_pointer()");
        } else {
            emitNoSpace("distance_to(");
            pos.accept(this);
            emitNoSpace(")");
        }
    }

    @Override
    public void visit(AttributeOf node) {
        // Goboscript syntax: Object.Attribute
        // Note: Cross-sprite attribute access (non-Stage) is not fully supported

        // Check if this is accessing another sprite's attribute (not Stage)
        String spriteName = resolveStringElement(node.getElementChoice());

        // If accessing non-Stage sprite attributes, emit 0 (safe fallback) because
        // Goboscript doesn't support "SpriteName".x
        // Note: Scratch uses "_stage_" internally to refer to the Stage
        if (spriteName != null && !spriteName.equals("Stage") && !spriteName.equals("_stage_")) {
            // We cannot safely emit a comment here because we are inside an expression,
            // and an inline comment # would ignore the rest of the line (potentially closing parens/braces).
            // So we just emit 0.
            emitNoSpace("0");
            return;
        }

        // 1. Object (ElementChoice)
        // Ensure we print the identifier, not a string literal if possible
        String resolvedName = resolveStringElement(node.getElementChoice());
        if (resolvedName != null) {
            emitNoSpace("\"" + escapeString(resolvedName) + "\"");
        } else {
            node.getElementChoice().accept(this);
        }

        emitNoSpace(".");

        // 2. Attribute
        Attribute attr = node.getAttribute();
        if (attr instanceof AttributeFromFixed af) {
            emitNoSpace(mapFixedAttribute(af.getAttribute().getTypeName()));
        } else if (attr instanceof AttributeFromVariable av) {
            String name = av.getVariable().getName().getName();
            // Clean up potential scratchblocks syntax artifacts if present
            if (name.startsWith("[\"") && name.endsWith("\" v]")) {
                name = name.substring(2, name.length() - 4);
            }
            emitNoSpace(escapeIdentifier(name));
        } else {
            attr.accept(this);
        }
    }

    private String mapFixedAttribute(String attr) {
        return switch (attr) {
            case "x position" -> "x";
            case "y position" -> "y";
            case "direction" -> "direction";
            case "costume #" -> "costume_number";
            case "costume name" -> "costume_name";
            case "size" -> "size";
            case "volume" -> "volume";
            case "backdrop #" -> "backdrop_number";
            case "backdrop name" -> "backdrop_name";
            default -> attr.replace(" ", "_");
        };
    }

    @Override
    public void visit(DaysSince2000 node) {
        emitNoSpace("days_since_2000()");
    }

    @Override
    public void visit(Current node) {
        emitNoSpace("current_");
        emitNoSpace(node.getTimeComp().getTypeName());
        emitNoSpace("()");
    }

    @Override
    public void visit(LengthOfString node) {
        emitNoSpace("length(");
        node.getStringExpr().accept(this);
        emitNoSpace(")");
    }

    // ---------------------------------------------------------------
    // Expressions - Strings
    // ---------------------------------------------------------------

    @Override
    public void visit(Backdrop node) {
        if (node.getType().getType() == NameNum.NameNumType.NUMBER) {
            emitNoSpace("backdrop_number()");
        } else {
            emitNoSpace("backdrop_name()");
        }
    }

    @Override
    public void visit(Costume node) {
        if (node.getType().getType() == NameNum.NameNumType.NUMBER) {
            emitNoSpace("costume_number()");
        } else {
            emitNoSpace("costume_name()");
        }
    }

    @Override
    public void visit(StringLiteral node) {
        emitNoSpace("\"" + escapeString(node.getText()) + "\"");
    }

    @Override
    public void visit(ItemOfVariable node) {
        node.getIdentifier().accept(this);
        emitNoSpace("[");
        node.getNum().accept(this);
        emitNoSpace("]");
    }

    @Override
    public void visit(Join node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" & ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Answer node) {
        emitNoSpace("answer()");
    }

    @Override
    public void visit(Username node) {
        emitNoSpace("username()");
    }

    @Override
    public void visit(LetterOf node) {
        node.getStringExpr().accept(this);
        emitNoSpace("[");
        node.getNum().accept(this);
        emitNoSpace("]");
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
        node.getOperand1().accept(this);
        emitNoSpace(" > ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(LessThan node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" < ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Equals node) {
        emitNoSpace("(");
        node.getOperand1().accept(this);
        emitNoSpace(" == ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(Touching node) {
        var touchable = node.getTouchable();
        if (touchable instanceof MousePointer) {
            emitNoSpace("touching_mouse_pointer()");
        } else if (touchable instanceof Edge) {
            emitNoSpace("touching_edge()");
        } else {
            emitNoSpace("touching(");
            touchable.accept(this);
            emitNoSpace(")");
        }
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        emitNoSpace("touching_color(");
        node.getColor().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(ColorTouchingColor node) {
        emitNoSpace("color_is_touching_color(");
        node.getOperand1().accept(this);
        emitNoSpace(", ");
        node.getOperand2().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(StringContains node) {
        emitNoSpace("contains(");
        node.getContaining().accept(this);
        emitNoSpace(", ");
        node.getContained().accept(this);
        emitNoSpace(")");
    }

    @Override
    public void visit(ListContains node) {
        node.getElement().accept(this);
        emitNoSpace(" in ");
        node.getIdentifier().accept(this);
    }

    @Override
    public void visit(IndexOf node) {
        // Goboscript syntax for "item # of X in LIST": "X in list_name"
        node.getExpr().accept(this);
        emitNoSpace(" in ");
        node.getIdentifier().accept(this);
    }

    @Override
    public void visit(IsMouseDown node) {
        emitNoSpace("mouse_down()");
    }

    @Override
    public void visit(IsKeyPressed node) {
        emitNoSpace("key_pressed(");
        if (node.getKey().getKey() instanceof NumberLiteral num) {
            emitNoSpace("\"" + getKeyName((int) num.getValue()) + "\"");
        } else {
            node.getKey().getKey().accept(this);
        }
        emitNoSpace(")");
    }

    // ---------------------------------------------------------------
    // Identifiers and Variables
    // ---------------------------------------------------------------

    @Override
    public void visit(Variable node) {
        // In Goboscript, variables are accessed by name directly.
        // Global variables (defined in stage.gs) don't need a Stage. prefix.
        emitNoSpace(escapeIdentifier(node.getName().getName()));
    }

    @Override
    public void visit(ScratchList node) {
        // In Goboscript, lists are accessed by name directly with "_list" suffix to avoid
        // name collisions with variables. Global lists (defined in stage.gs) don't need a Stage. prefix.
        emitNoSpace(escapeListIdentifier(node.getName().getName()));
    }

    @Override
    public void visit(Parameter node) {
        String paramName = node.getName().getName();

        // Parameters are only valid inside procedure bodies and must match a declared parameter.
        // When an argument_reporter_boolean/string_number block is used outside a procedure,
        // Scratch always returns the default value (false for boolean, "" for string).
        if (!inProcedure || !currentProcedureParams.contains(paramName)) {
            // Emit default value based on parameter type
            if (node.getType() instanceof BooleanType) {
                emitNoSpace("false");
            } else {
                emitNoSpace("\"\""); // Empty string as default
            }
            return;
        }

        emitNoSpace("$" + escapeIdentifier(paramName));
    }

    @Override
    public void visit(StrId node) {
        emitNoSpace("\"" + escapeString(node.getName()) + "\"");
    }

    @Override
    public void visit(Qualified node) {
        String qualifier = node.getFirst().getName();
        boolean isList = node.getSecond() instanceof ScratchList;

        // Check if this is self-qualified access (sprite referring to its own variable)
        boolean isSelf = currentActor != null && qualifier.equals(currentActor.getIdent().getName());
        // Check if this is Stage-qualified access (global variables)
        boolean isStage = "Stage".equals(qualifier);

        // For self-qualified or Stage-qualified data expressions, emit just the name without prefix.
        // In Goboscript:
        // - A sprite's own variables are accessed directly by name
        // - Stage variables are global and accessed directly by name
        // The "Sprite.varName" and "Stage.varName" syntax doesn't exist in goboscript.
        if (isSelf || isStage) {
            // Simply emit the second part without qualification
            // Use escapeListIdentifier for lists to add "_list" suffix and avoid name collisions
            String name = node.getSecond().getName().getName();
            emitNoSpace(isList ? escapeListIdentifier(name) : escapeIdentifier(name));
            return;
        }

        // For cross-sprite qualified access (accessing another sprite's variable), use the dot notation
        String name = node.getSecond().getName().getName();
        emitNoSpace(escapeIdentifier(qualifier));
        emitNoSpace(".");
        emitNoSpace(isList ? escapeListIdentifier(name) : escapeIdentifier(name));
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
        emitNoSpace(String.format("0x%02x%02x%02x",
                (int) node.getRed(), (int) node.getGreen(), (int) node.getBlue()));
    }

    // ---------------------------------------------------------------
    // Unspecified/Fallback Handlers
    // ---------------------------------------------------------------

    /**
     * Handles unspecified statements (e.g., unsupported or placeholder blocks).
     * These are silently skipped to avoid generating invalid code.
     */
    @Override
    public void visit(UnspecifiedStmt node) {
        // Skip - unspecified statements produce no output
    }

    /**
     * Handles unspecified boolean expressions.
     * Emits 'false' as a safe default value.
     */
    @Override
    public void visit(UnspecifiedBoolExpr node) {
        emitNoSpace("false");
    }

    /**
     * Handles unspecified numeric expressions.
     * Emits '0' as a safe default value.
     */
    @Override
    public void visit(UnspecifiedNumExpr node) {
        emitNoSpace("0");
    }

    /**
     * Handles unspecified string expressions.
     * Emits an empty string as a safe default value.
     */
    @Override
    public void visit(UnspecifiedStringExpr node) {
        emitNoSpace("\"\"");
    }

    // ---------------------------------------------------------------
    // Helper Methods
    // ---------------------------------------------------------------

    private void emitBlock(Event event) {
        Script parentScript = AstNodeUtil.findParent(event, Script.class);
        if (parentScript != null) {
            emitNoSpace("{");
            newLine();
            beginIndentation();
            parentScript.getStmtList().accept(this);
            endIndentation();
            emitNoSpace("}");
            newLine();
        }
    }

    private String escapeIdentifier(String name) {
        return GoboscriptSanitizer.escapeIdentifier(name);
    }

    /**
     * Escapes a list identifier name with a "_list" suffix to avoid name collisions
     * with variables of the same name in Scratch.
     */
    private String escapeListIdentifier(String name) {
        return GoboscriptSanitizer.escapeListIdentifier(name);
    }

    /**
     * Escapes a procedure name, adding a "_proc" suffix if it conflicts with a
     * Goboscript built-in command name.
     */
    private String escapeProcedureName(String name) {
        return GoboscriptSanitizer.escapeProcedureName(name);
    }

    @Override
    protected String escapeString(String text) {
        return escapeStringWithNewlines(text);
    }

    private String getKeyName(int keyCode) {
        return switch (keyCode) {
            case ScratchKeyCode.SPACE -> "space";
            case ScratchKeyCode.LEFT_ARROW -> "left arrow";
            case ScratchKeyCode.UP_ARROW -> "up arrow";
            case ScratchKeyCode.RIGHT_ARROW -> "right arrow";
            case ScratchKeyCode.DOWN_ARROW -> "down arrow";
            default -> {
                Character c = ScratchKeyCode.toChar(keyCode);
                yield c != null ? String.valueOf(c) : "any";
            }
        };
    }

    private String getAttributeName(EventAttribute attribute) {
        return attribute.getTypeName();
    }

    private String getGoboscriptProcedureName(ProcedureDefinition node) {
        return getProcedureName(node, AbstractExportVisitor::normalizeProcedureName);
    }

    /**
     * Checks whether a procedure definition has the "warp" flag set (run without screen refresh).
     *
     * @param node The procedure definition
     * @return true if warp is enabled, false otherwise (defaults to true if metadata unavailable)
     */
    private boolean isProcedureWarp(ProcedureDefinition node) {
        ProcedureMetadata metadata = node.getMetadata();
        if (metadata != null && metadata.getPrototype() instanceof NonDataBlockMetadata protoMeta) {
            MutationMetadata mutation = protoMeta.getMutation();
            if (mutation instanceof ProcedureMutationMetadata procMutation) {
                return procMutation.isWarp();
            }
        }
        // Default to warp=true if metadata is unavailable
        return true;
    }

    private String getGoboscriptProcedureCallName(CallStmt node) {
        return getProcedureCallName(node, AbstractExportVisitor::normalizeProcedureName);
    }

    private String resolveStringElement(ASTNode node) {
        GoboscriptVisitor tempVisitor = new GoboscriptVisitor();
        // Propagate context
        tempVisitor.setProgram(this.program);
        tempVisitor.setCurrentActor(this.currentActor);
        // We generally want to resolve static values, so we don't need asset info for this check

        node.accept(tempVisitor);
        String output = tempVisitor.getOutput().trim();

        // Check if output is a quoted string literal: "something"
        if (output.startsWith("\"") && output.endsWith("\"") && output.length() >= 2) {
            // Unescape quotes for internal checking
            // Note: This is an approximation since we don't have a full unescaper here,
            // but for checking simple asset names it suffices.
            return output.substring(1, output.length() - 1);
        }
        return null;
    }

    private boolean isBackdropAvailable(String name) {
        ActorDefinition stage = getStageActor();
        if (stage == null || stage.getActorMetadata() == null) {
            return false;
        }

        for (ImageMetadata costume : stage.getActorMetadata().getCostumes().getList()) {
            if (costume.getName().equals(name)) {
                // Found the backdrop name, check if its asset is available
                return isAssetAvailable(costume.getMd5ext());
            }
        }
        // Backdrop name not found in Stage costumes
        return false;
    }

    private boolean isAssetAvailable(String md5ext) {
        return availableAssets == null || availableAssets.contains(md5ext);
    }

    private void emitAssetDeclaration(String type, String md5ext, String name) {
        emitNoSpace(type + " \"");
        emitNoSpace(assetPrefix + escapeString(md5ext));
        emitNoSpace("\" as \"");
        emitNoSpace(escapeString(name));
        emitNoSpace("\";");
        newLine();
    }
    // ---------------------------------------------------------------
    // Video Sensing Extension
    // ---------------------------------------------------------------

    @Override
    public void visit(VideoSensingBlock node) {
        switch (node) {
            case VideoOn stmt -> {
                // Goboscript does not support video sensing reporters.
                // Emit 0 as a safe default numeric value to ensure compilation.
                emitNoSpace("0");
            }
            case WhenVideoMotionGreaterThan whenVideoMotionGreaterThan -> {
                // Goboscript does not currently support video motion events.
                // Emit a comment to indicate it was skipped, preventing invalid output.
                emitNoSpace("# Unsupported event: when video motion > ...");
                newLine();
                // Do NOT visit children, to avoid emitting the threshold number.
            }
            case SetVideoTransparency setVideoTransparency -> {
                // Goboscript does not support video transparency.
                // Emit a comment to indicate it was skipped.
                emitNoSpace("# Unsupported: set video transparency");
                newLine();
            }
            case TurnVideo turnVideo -> {
                // Goboscript does not support video toggle (turn video on/off).
                // Emit a comment to indicate it was skipped.
                emitNoSpace("# Unsupported: turn video on/off");
                newLine();
            }
            case null, default -> {
                // For any other unknown VideoSensingBlock types, emit a generic comment
                // rather than visitChildren() which could produce invalid output.
                emitNoSpace("# Unsupported video sensing block");
                newLine();
            }
        }
    }
}
