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

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.type.ListType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A specialized {@link CloneVisitor} that promotes shared variables and lists to the Stage.
 * This is used by {@link GoboscriptProjectExporter} to satisfy Goboscript's scoping requirements
 * without mutating the original AST.
 */
public class GoboscriptPromotionVisitor extends CloneVisitor {

    private final Set<String> sharedVarNames;
    private final Set<String> sharedListNames;
    private final List<DeclarationIdentAsTypeStmt> promotedDecls = new ArrayList<>();

    public GoboscriptPromotionVisitor(Set<String> sharedVarNames, Set<String> sharedListNames) {
        super(false); // Preserve original block IDs
        this.sharedVarNames = sharedVarNames;
        this.sharedListNames = sharedListNames;
    }

    @Override
    public Program visit(Program node) {
        // First pass: collect all declarations that need to be promoted from Sprites
        // We visit the actor definition list directly to avoid visiting Stage first if it's at the beginning
        for (ActorDefinition actor : node.getActorDefinitionList().getDefinitions()) {
            if (!actor.isStage()) {
                collectPromotableDecls(actor);
            }
        }

        // Add declarations for variables and lists that are shared but were never declared
        // in any actor (e.g. "ghost" variables used in blocks but missing from SB3 variables list).
        for (var var : node.getSymbolTable().getVariables().values()) {
            if (sharedVarNames.contains(var.variableName()) && !isAlreadyPromoted(var.variableName(), false)) {
                promotedDecls.add(new DeclarationIdentAsTypeStmt(
                        new Variable(new StrId(var.variableName())),
                        var.type()
                ));
            }
        }
        for (var list : node.getSymbolTable().getLists().values()) {
            if (sharedListNames.contains(list.variableName()) && !isAlreadyPromoted(list.variableName(), true)) {
                promotedDecls.add(new DeclarationIdentAsTypeStmt(
                        new ScratchList(new StrId(list.variableName())),
                        new ListType()
                ));
            }
        }

        // Second pass: clone the program, which will use our overridden visit(ActorDefinition)
        return (Program) super.visit(node);
    }

    private boolean isAlreadyPromoted(String name, boolean isList) {
        return promotedDecls.stream().anyMatch(d -> {
            boolean dIsList = d.getType() instanceof ListType;
            return dIsList == isList && d.getIdent().getName().getName().equals(name);
        });
    }

    private void collectPromotableDecls(ActorDefinition actor) {
        for (DeclarationStmt stmt : actor.getDecls().getDeclarationStmts()) {
            if (stmt instanceof DeclarationIdentAsTypeStmt identDecl) {
                String name = identDecl.getIdent().getName().getName();
                boolean isList = identDecl.getType() instanceof ListType;
                if ((!isList && sharedVarNames.contains(name)) || (isList && sharedListNames.contains(name))) {
                    // Check if already in promotedDecls to avoid duplicates
                    if (promotedDecls.stream().noneMatch(d -> d.getIdent().getName().getName().equals(name))) {
                        promotedDecls.add((DeclarationIdentAsTypeStmt) identDecl.accept(new CloneVisitor(false)));
                    }
                }
            }
        }
    }

    @Override
    public ActorDefinition visit(ActorDefinition node) {
        if (node.isStage()) {
            // Clone everything as usual
            ActorDefinition cloned = (ActorDefinition) super.visit(node);

            // Add promoted declarations to Stage
            List<DeclarationStmt> stageDecls = new ArrayList<>(cloned.getDecls().getDeclarationStmts());

            for (DeclarationIdentAsTypeStmt promoted : promotedDecls) {
                // Only add if not already present in Stage
                boolean alreadyPresent = stageDecls.stream()
                        .filter(s -> s instanceof DeclarationIdentAsTypeStmt)
                        .map(s -> (DeclarationIdentAsTypeStmt) s)
                        .anyMatch(d -> d.getIdent().getName().getName().equals(promoted.getIdent().getName().getName()));

                if (!alreadyPresent) {
                    stageDecls.add(promoted);
                }
            }

            return new ActorDefinition(
                    cloned.getActorType(),
                    cloned.getIdent(),
                    new DeclarationStmtList(Collections.unmodifiableList(stageDecls)),
                    cloned.getSetStmtList(),
                    cloned.getProcedureDefinitionList(),
                    cloned.getScripts(),
                    cloned.getActorMetadata()
            );
        } else {
            // It's a Sprite. Clone and filter out promoted declarations.
            ActorDefinition cloned = (ActorDefinition) super.visit(node);

            List<DeclarationStmt> filteredDecls = cloned.getDecls().getDeclarationStmts().stream()
                    .filter(stmt -> {
                        if (stmt instanceof DeclarationIdentAsTypeStmt identDecl) {
                            String name = identDecl.getIdent().getName().getName();
                            boolean isList = identDecl.getType() instanceof ListType;
                            return (!isList && !sharedVarNames.contains(name)) && (isList || !sharedListNames.contains(name));
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

            return new ActorDefinition(
                    cloned.getActorType(),
                    cloned.getIdent(),
                    new DeclarationStmtList(Collections.unmodifiableList(filteredDecls)),
                    cloned.getSetStmtList(),
                    cloned.getProcedureDefinitionList(),
                    cloned.getScripts(),
                    cloned.getActorMetadata()
            );
        }
    }
}
