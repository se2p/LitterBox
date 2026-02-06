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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.export.ProjectExporter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

/**
 * Exports a Scratch project to a full Goboscript project structure.
 *
 * <p>Creates a directory containing:
 * <ul>
 *   <li>One .gs file per actor (stage and sprites)</li>
 *   <li>All costume and sound assets extracted from the sb3 file</li>
 * </ul>
 *
 * <p>This exporter requires an .sb3 input file since JSON files do not
 * contain the binary assets needed for a compilable Goboscript project.
 */
public class GoboscriptProjectExporter extends ProjectExporter {

    private static final Logger log = Logger.getLogger(GoboscriptProjectExporter.class.getName());
    private static final String ASSETS_DIR = "assets";

    private Set<String> availableAssets;

    /**
     * Global variables declared in Stage, to be shared with sprite visitors.
     */
    private Set<String> globalVariables = new HashSet<>();

    /**
     * Global lists declared in Stage, to be shared with sprite visitors.
     */
    private Set<String> globalLists = new HashSet<>();

    /**
     * Escaped sprite names in the current project.
     */
    private Set<String> spriteNames = new HashSet<>();

    /**
     * Tracks filenames used in the current export to prevent collisions.
     */
    private Set<String> usedFilenames = new HashSet<>();

    /**
     * Exports a program to a full Goboscript project directory.
     *
     * @param sb3File   Path to the source .sb3 file (required for asset extraction)
     * @param program   The parsed program AST
     * @param outputDir The directory to create the project in
     * @throws IOException If file operations fail
     */
    @Override
    public void export(Path sb3File, Program program, Path outputDir) throws IOException {
        clearErrors();

        // Identify variables/lists accessed across sprites and promote them to global (Stage)
        // This returns a NEW program instance to preserve AST immutability.
        Program exportedProgram = promoteSharedVariablesToGlobal(program);

        // Create output directory
        Files.createDirectories(outputDir);

        // Extract assets from sb3 and collect available asset names
        availableAssets = extractAssets(sb3File, outputDir);

        // Clear global variables/lists and used filenames before processing
        globalVariables.clear();
        globalLists.clear();
        usedFilenames.clear();
        spriteNames.clear();

        // Collect escaped sprite names for collision detection
        // We also include "Stage" as it's a special global object in Goboscript
        spriteNames.add("Stage");
        for (ActorDefinition actor : exportedProgram.getActorDefinitionList().getDefinitions()) {
            if (!actor.isStage()) {
                spriteNames.add(GoboscriptSanitizer.escapeIdentifier(actor.getIdent().getName()));
            }
        }

        // Export Stage first to collect global variables/lists
        ActorDefinition stage = null;
        for (ActorDefinition actor : exportedProgram.getActorDefinitionList().getDefinitions()) {
            if (actor.isStage()) {
                stage = actor;
                exportActor(actor, exportedProgram, outputDir);
                break;
            }
        }

        // Export remaining actors (sprites), passing global variables/lists
        for (ActorDefinition actor : exportedProgram.getActorDefinitionList().getDefinitions()) {
            if (!actor.isStage()) {
                exportActor(actor, exportedProgram, outputDir);
            }
        }

        logExportSummary(outputDir);
    }

    protected Set<String> extractAssets(Path sb3File, Path outputDir) throws IOException {
        Set<String> assets = new HashSet<>();
        Path assetsDir = outputDir.resolve(ASSETS_DIR);
        Files.createDirectories(assetsDir);
        try (ZipFile zip = new ZipFile(sb3File.toFile())) {
            zip.stream()
                    .filter(entry -> !entry.isDirectory())
                    .filter(entry -> !entry.getName().equals("project.json"))
                    .forEach(entry -> {
                        extractEntry(zip, entry, assetsDir);
                        assets.add(entry.getName());
                    });
        }
        return assets;
    }

    private void exportActor(ActorDefinition actor, Program program, Path outputDir) throws IOException {
        // Skip actors that have no valid costumes (Goboscript requires at least one costume)
        // However, we MUST export the Stage regardless, otherwise the project structure is invalid.
        if (!hasValidCostumes(actor) && !actor.isStage()) {
            log.warning("Skipping actor '" + actor.getIdent().getName()
                    + "' because it has no costumes available in the sb3 file");
            return;
        }

        // Goboscript requires the Stage to be in a file named "stage.gs" (lowercase)
        String filename;
        if (actor.isStage()) {
            filename = "stage.gs";
        } else {
            filename = GoboscriptSanitizer.escapeIdentifier(actor.getIdent().getName()) + ".gs";
        }

        filename = getUniqueFilename(filename);
        Path actorFile = outputDir.resolve(filename);

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.setProgram(program);
        visitor.setCurrentActor(actor);
        visitor.setAssetPrefix(ASSETS_DIR + "/");
        visitor.setAvailableAssets(availableAssets);
        visitor.setSpriteNames(spriteNames);

        // For sprites, pass global variables/lists from Stage
        if (!actor.isStage()) {
            visitor.setGlobalVariables(globalVariables);
            visitor.setGlobalLists(globalLists);
        }

        actor.accept(visitor);

        // After processing Stage, capture its global variables/lists
        if (actor.isStage()) {
            globalVariables = visitor.getGlobalVariables();
            globalLists = visitor.getGlobalLists();
        }

        String code = visitor.getOutput();
        Files.writeString(actorFile, code, StandardCharsets.UTF_8);

        log.fine("Exported actor: " + actor.getIdent().getName() + " -> " + filename);
    }

    private String getUniqueFilename(String filename) {
        String base = getBaseName(filename);
        String ext = getExtension(filename);
        String uniqueName = filename;
        int counter = 2;
        while (usedFilenames.contains(uniqueName)) {
            uniqueName = base + "_" + counter + ext;
            counter++;
        }
        usedFilenames.add(uniqueName);
        return uniqueName;
    }

    private boolean hasValidCostumes(ActorDefinition actor) {
        if (actor.getActorMetadata() == null) {
            return false;
        }
        for (ImageMetadata costume : actor.getActorMetadata().getCostumes().getList()) {
            if (availableAssets == null || availableAssets.contains(costume.getMd5ext())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Identifies variables and lists that are accessed across different sprites
     * and moves their declarations to the Stage actor, effectively making them global.
     * This is necessary because Goboscript does not support cross-sprite variable assignment.
     *
     * @param program The program AST to analyze
     * @return A new Program AST with promoted declarations
     */
    private Program promoteSharedVariablesToGlobal(Program program) {
        Set<String> sharedVarNames = new HashSet<>();
        Set<String> sharedListNames = new HashSet<>();

        // Find all cross-sprite references
        program.accept(new ScratchVisitor() {
            private ActorDefinition currentActor;

            @Override
            public void visit(ActorDefinition node) {
                currentActor = node;
                visitChildren(node);
                currentActor = null;
            }

            @Override
            public void visit(Qualified node) {
                String qualifier = node.getFirst().getName();
                if (currentActor != null && !qualifier.equals(currentActor.getIdent().getName())
                        && !"Stage".equals(qualifier) && !"_stage_".equals(qualifier)) {
                    // This is a cross-sprite reference!
                    if (node.getSecond() instanceof Variable var) {
                        sharedVarNames.add(var.getName().getName());
                    } else if (node.getSecond() instanceof ScratchList list) {
                        sharedListNames.add(list.getName().getName());
                    }
                }
                visitChildren(node);
            }

            @Override
            public void visit(AttributeOf node) {
                // Check if the attribute is a user variable from another sprite
                if (node.getAttribute() instanceof AttributeFromVariable av) {
                    String spriteName = resolveSpriteName(node.getElementChoice());
                    if (currentActor != null && spriteName != null
                            && !spriteName.equals(currentActor.getIdent().getName())
                            && !"Stage".equals(spriteName) && !"_stage_".equals(spriteName)) {
                        sharedVarNames.add(av.getVariable().getName().getName());
                    }
                }
                visitChildren(node);
            }

            private String resolveSpriteName(ASTNode choice) {
                if (choice instanceof WithExpr withExpr) {
                    if (withExpr.getExpression() instanceof StringLiteral lit) {
                        return lit.getText();
                    }
                }
                return null;
            }
        });

        // Identify all declared variables and lists per actor to find "ghost" variables.
        Map<String, Set<String>> actorVars = new HashMap<>();
        Map<String, Set<String>> actorLists = new HashMap<>();
        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            String actorName = actor.getIdent().getName();
            Set<String> vars = new HashSet<>();
            Set<String> lists = new HashSet<>();
            if (actor.getDecls() != null) {
                for (DeclarationStmt stmt : actor.getDecls().getDeclarationStmts()) {
                    if (stmt instanceof DeclarationIdentAsTypeStmt identDecl) {
                        String name = identDecl.getIdent().getName().getName();
                        if (identDecl.getType() instanceof de.uni_passau.fim.se2.litterbox.ast.model.type.ListType) {
                            lists.add(name);
                        } else {
                            vars.add(name);
                        }
                    }
                }
            }
            actorVars.put(actorName, vars);
            actorLists.put(actorName, lists);
        }

        // Promote ghost variables and lists to global by adding them to the shared sets.
        // A variable is a ghost if it exists in the SymbolTable for an actor but is missing from its declarations.
        if (program.getSymbolTable() != null) {
            for (var varInfo : program.getSymbolTable().getVariables().values()) {
                Set<String> declared = actorVars.get(varInfo.actor());
                if (declared == null || !declared.contains(varInfo.variableName())) {
                    sharedVarNames.add(varInfo.variableName());
                }
            }
            for (var listInfo : program.getSymbolTable().getLists().values()) {
                Set<String> declared = actorLists.get(listInfo.actor());
                if (declared == null || !declared.contains(listInfo.variableName())) {
                    sharedListNames.add(listInfo.variableName());
                }
            }
        }

        if (sharedVarNames.isEmpty() && sharedListNames.isEmpty()) {
            return program;
        }

        // Use the visitor to create a new program with promoted declarations
        GoboscriptPromotionVisitor promotionVisitor = new GoboscriptPromotionVisitor(sharedVarNames, sharedListNames);
        return promotionVisitor.visit(program);
    }
}
