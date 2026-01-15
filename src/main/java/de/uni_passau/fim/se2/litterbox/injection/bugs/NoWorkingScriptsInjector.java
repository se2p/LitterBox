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

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.NoWorkingScripts;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects the {@link NoWorkingScripts} bug by emptying all event scripts and creating detached code.
 *
 * <p>
 * The bug pattern requires:
 * <ol>
 *   <li>All event handlers have <b>empty</b> bodies</li>
 *   <li>Dead code (script with Never event) with a <b>non-empty</b> body</li>
 * </ol>
 *
 * <p>
 * Transformation (for entire sprite):
 * <pre>
 * Before: [GreenFlag -> {move}], [KeyPressed -> {say}]
 * After:  [GreenFlag -> {}], [KeyPressed -> {}], [Never -> {move}], [Never -> {say}]
 * </pre>
 */
public class NoWorkingScriptsInjector extends AbstractBugInjector {

    /**
     * X position for loose blocks (arbitrary but consistent).
     */
    private static final double LOOSE_BLOCK_X = 100.0;

    /**
     * Y position for loose blocks (arbitrary but consistent).
     */
    private static final double LOOSE_BLOCK_Y = 100.0;

    private final IssueFinder issueFinder = new NoWorkingScripts();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(ScriptList node) {
        // Check if there are any scripts with non-empty bodies to detach
        boolean hasContentToDetach = node.getScriptList().stream()
                .anyMatch(s -> !(s.getEvent() instanceof Never) && !s.getStmtList().getStmts().isEmpty());

        if (hasContentToDetach) {
            addSplitAllScriptsMutation(node);
        }
        super.visit(node);
    }

    /**
     * Empties ALL scripts in the sprite and creates detached Never scripts for their bodies.
     */
    private void addSplitAllScriptsMutation(ScriptList scriptList) {
        // Find best blame script (first non-empty, non-never script)
        String scriptId = "";
        for (Script s : scriptList.getScriptList()) {
            if (!(s.getEvent() instanceof Never) && !s.getStmtList().getStmts().isEmpty()) {
                String id = AstNodeUtil.getBlockId(s);
                if (id != null) {
                    scriptId = id;
                    break;
                }
            }
        }

        modifyClonedNode(scriptList, clonedScriptList -> {
            List<Script> newScripts = new ArrayList<>();
            List<Script> neverScripts = new ArrayList<>();

            for (Script script : clonedScriptList.getScriptList()) {
                if (script.getEvent() instanceof Never) {
                    // Keep existing Never scripts as-is
                    newScripts.add(script);
                } else if (script.getStmtList().getStmts().isEmpty()) {
                    // Already empty, keep as-is
                    newScripts.add(script);
                } else {
                    // Empty the event handler
                    Script emptyEventHandler = new Script(
                            script.getEvent(),
                            new StmtList(new ArrayList<>())
                    );
                    emptyEventHandler.setParentNode(clonedScriptList);
                    newScripts.add(emptyEventHandler);

                    // Create detached code with Never event
                    // The first statement needs TopNonDataBlockMetadata for proper serialization
                    StmtList stmtListWithTopLevel = makeFirstStmtTopLevel(script.getStmtList());
                    Script detachedCode = new Script(
                            new Never(),
                            stmtListWithTopLevel
                    );
                    detachedCode.setParentNode(clonedScriptList);
                    neverScripts.add(detachedCode);
                }
            }

            // Replace script list: all event handlers first, then all Never scripts
            clonedScriptList.getScriptList().clear();
            clonedScriptList.getScriptList().addAll(newScripts);
            clonedScriptList.getScriptList().addAll(neverScripts);
        }, scriptId);
    }

    /**
     * Creates a new StmtList where the first statement has TopNonDataBlockMetadata.
     * This is required for Never scripts to serialize correctly as loose/detached blocks.
     */
    private StmtList makeFirstStmtTopLevel(StmtList original) {
        if (original.getStmts().isEmpty()) {
            return original;
        }

        List<Stmt> stmts = new ArrayList<>(original.getStmts());
        Stmt firstStmt = stmts.get(0);

        // Clone the first statement with TopNonDataBlockMetadata
        if (firstStmt.getMetadata() instanceof NonDataBlockMetadata meta
                && !(meta instanceof TopNonDataBlockMetadata)) {
            TopNonDataBlockMetadata topMeta = new TopNonDataBlockMetadata(
                    meta.getCommentId(),
                    CloneVisitor.generateUID(),  // Fresh ID to prevent circular references
                    meta.isShadow(),
                    meta.getMutation(),
                    LOOSE_BLOCK_X,
                    LOOSE_BLOCK_Y
            );

            // Use CloneVisitor to create a copy of the statement with new metadata
            Stmt newFirstStmt = cloneStmtWithMetadata(firstStmt, topMeta);
            stmts.set(0, newFirstStmt);
        }

        return new StmtList(stmts);
    }

    /**
     * Clones a statement but replaces its metadata with the provided metadata.
     */
    private Stmt cloneStmtWithMetadata(Stmt stmt, TopNonDataBlockMetadata newMeta) {
        // Clone the statement using a visitor that replaces the metadata
        MetadataReplacingCloneVisitor visitor = new MetadataReplacingCloneVisitor(stmt, newMeta);
        return (Stmt) stmt.accept(visitor);
    }

    /**
     * A CloneVisitor that replaces the metadata of the target node.
     */
    private static class MetadataReplacingCloneVisitor extends CloneVisitor {
        private final Stmt targetStmt;
        private final TopNonDataBlockMetadata newMeta;
        private boolean replaced = false;

        MetadataReplacingCloneVisitor(Stmt targetStmt, TopNonDataBlockMetadata newMeta) {
            this.targetStmt = targetStmt;
            this.newMeta = newMeta;
        }

        @Override
        public ASTNode visit(NonDataBlockMetadata node) {
            // Replace metadata only for the target statement's metadata
            if (!replaced && node.getBlockId() != null
                    && targetStmt.getMetadata() instanceof NonDataBlockMetadata targetMeta
                    && node.getBlockId().equals(targetMeta.getBlockId())) {
                replaced = true;
                return newMeta;
            }
            return super.visit(node);
        }
    }
}
