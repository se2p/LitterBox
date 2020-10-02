/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

public class ListVariableTest implements JsonTest {

    @Test
    public void testListUsesInLoop() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/loopandlistoperations.json");

        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof AddTo).findFirst().get();
        ListVariable theList = new ListVariable(((AddTo) node.getASTNode()).getIdentifier());

        for (CFGNode sayNode : cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).collect(Collectors.toSet())) {
            assertThat(getUsedLists(sayNode)).containsExactly(theList);
        }

        assertThat(usesOf(cfg, AddTo.class)).containsExactly(theList);
        assertThat(usesOf(cfg, DeleteOf.class)).containsExactly(theList);
    }

    @Test
    public void testAllListUses() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/listoperations.json");

        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof AddTo).findFirst().get();
        ListVariable theList = new ListVariable(((AddTo) node.getASTNode()).getIdentifier());

        for (CFGNode sayNode : cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).collect(Collectors.toSet())) {
            assertThat(getUsedLists(sayNode)).containsExactly(theList);
        }

        assertThat(usesOf(cfg, DeleteOf.class)).containsExactly(theList);
        assertThat(usesOf(cfg, DeleteAllOf.class)).isEmpty(); // This one doesn't read the list, it just resets it
        assertThat(usesOf(cfg, InsertAt.class)).containsExactly(theList);
        assertThat(usesOf(cfg, ReplaceItem.class)).containsExactly(theList);
        assertThat(usesOf(cfg, AddTo.class)).containsExactly(theList);
    }

    @Test
    public void testAllListDefs() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/listoperations.json");

        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof AddTo).findFirst().get();
        ListVariable theList = new ListVariable(((AddTo) node.getASTNode()).getIdentifier());

        for (CFGNode sayNode : cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).collect(Collectors.toSet())) {
            assertThat(getDefinedLists(sayNode)).isEmpty();
        }

        assertThat(defsOf(cfg, DeleteAllOf.class)).containsExactly(theList);
        assertThat(defsOf(cfg, DeleteOf.class)).containsExactly(theList);
        assertThat(defsOf(cfg, InsertAt.class)).containsExactly(theList);
        assertThat(defsOf(cfg, ReplaceItem.class)).containsExactly(theList);
        assertThat(defsOf(cfg, AddTo.class)).containsExactly(theList);
    }

    private Set<ListVariable> defsOf(ControlFlowGraph cfg, Class<?> nodeClass) {
        return getDefinedLists(getNodeOfType(cfg, nodeClass));
    }

    private Set<ListVariable> usesOf(ControlFlowGraph cfg, Class<?> nodeClass) {
        return getUsedLists(getNodeOfType(cfg, nodeClass));
    }

    private CFGNode getNodeOfType(ControlFlowGraph cfg, Class<?> nodeClass) {
        return cfg.getNodes().stream().filter(n -> n.getASTNode() != null && nodeClass.isAssignableFrom(n.getASTNode().getClass())).findFirst().get();
    }

    private Set<ListVariable> getDefinedLists(CFGNode node) {
        ListDefinitionVisitor visitor = new ListDefinitionVisitor();
        node.getASTNode().accept(visitor);
        return visitor.getDefineables();
    }

    private Set<ListVariable> getUsedLists(CFGNode node) {
        ListUseVisitor visitor = new ListUseVisitor();
        node.getASTNode().accept(visitor);
        return visitor.getDefineables();
    }
}
