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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.extensions.quantum.*;

public interface QuantumExtensionVisitor extends ScratchVisitor {

    default void visit(QuantumExtensionEvent node) {
        visit((QuantumExtensionBlock) node);
    }

    default void visit(QuantumExtensionStmt node) {
        visit((QuantumExtensionBlock) node);
    }

    default void visit(WhenMeasured node) {
        visit((QuantumExtensionEvent) node);
    }

    default void visit(WhenEntanglementStarted node) {
        visit((QuantumExtensionEvent) node);
    }

    default void visit(WhenSuperpositionStarted node) {
        visit((QuantumExtensionEvent) node);
    }

    default void visit(Measure node) {
        visit((QuantumExtensionStmt) node);
    }

    default void visit(FixedQuantumAttribute node) {
        visit((QuantumExtensionBlock) node);
    }

    default void visit(QuantumSuperpositionPossibilities node) {
        visit((QuantumExtensionStmt) node);
    }

    default void visit(QuantumSuperpositionList node) {
        visit((QuantumExtensionStmt) node);
    }

    default void visit(QuantumSuperpositionPossibilitiesList node) {
        visit((QuantumExtensionStmt) node);
    }

    default void visit(QuantumEntanglementPossibilities node) {
        visit((QuantumExtensionStmt) node);
    }

    default void visit(QuantumEntanglementList node) {
        visit((QuantumExtensionStmt) node);
    }

    default void visit(QuantumEntanglementPossibilitiesList node) {
        visit((QuantumExtensionStmt) node);
    }
}
