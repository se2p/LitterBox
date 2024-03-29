/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.clonedetection;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CodeClone {

    public enum CloneType { TYPE1, TYPE2, TYPE3 }

    private CloneType type = CloneType.TYPE1;

    private final List<Stmt> copy1 = new ArrayList<>();
    private final List<Stmt> copy2 = new ArrayList<>();

    private final ASTNode firstRoot;
    private final ASTNode secondRoot;

    public CodeClone(ASTNode root1, ASTNode root2) {
        this.firstRoot = root1;
        this.secondRoot = root2;
    }

    public CloneType getType() {
        return type;
    }

    public void addClonedStatement(Stmt stmt1, Stmt stmt2) {
        copy1.add(stmt1);
        copy2.add(stmt2);
    }

    public void setType(CloneType cloneType) {
        type = cloneType;
    }

    public ASTNode getFirstScript() {
        return firstRoot;
    }

    public ASTNode getSecondScript() {
        return secondRoot;
    }

    public List<Stmt> getFirstStatements() {
        return Collections.unmodifiableList(copy1);
    }

    public List<Stmt> getSecondStatements() {
        return Collections.unmodifiableList(copy2);
    }

    public ASTNode getFirstNode() {
        return copy1.get(0);
    }

    public ASTNode getSecondNode() {
        return copy2.get(0);
    }

    public int size() {
        return copy1.size();
    }

    public boolean subsumes(CodeClone otherClone) {
        // TODO: IdentifyHashSet does not handle removeAll properly, so using a workaround
        Set<Integer> firstIDs = otherClone.getFirstStatements().stream().map(System::identityHashCode).collect(Collectors.toSet());
        Set<Integer> secondIDs = otherClone.getSecondStatements().stream().map(System::identityHashCode).collect(Collectors.toSet());

        copy1.stream().map(System::identityHashCode).forEach(firstIDs::remove);
        copy2.stream().map(System::identityHashCode).forEach(secondIDs::remove);

        return firstIDs.isEmpty() && secondIDs.isEmpty();
    }
}
