package de.uni_passau.fim.se2.litterbox.cfg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

public class ListTest {


    @Test
    public void testAllListUses() throws IOException, ParsingException {
        ControlFlowGraph cfg = getCFG("src/test/fixtures/cfg/listoperations.json");

        CFGNode node = cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof AddTo).findFirst().get();
        List theList = new List(((AddTo)node.getASTNode()).getIdentifier());

        for(CFGNode sayNode : cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).collect(Collectors.toSet())) {
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
        List theList = new List(((AddTo)node.getASTNode()).getIdentifier());

        for(CFGNode sayNode : cfg.getNodes().stream().filter(n -> n.getASTNode() instanceof SayForSecs).collect(Collectors.toSet())) {
            assertThat(getDefinedLists(sayNode)).isEmpty();
        }

        assertThat(defsOf(cfg, DeleteAllOf.class)).containsExactly(theList);
        assertThat(defsOf(cfg, DeleteOf.class)).containsExactly(theList);
        assertThat(defsOf(cfg, InsertAt.class)).containsExactly(theList);
        assertThat(defsOf(cfg, ReplaceItem.class)).containsExactly(theList);
        assertThat(defsOf(cfg, AddTo.class)).containsExactly(theList);
    }

    private Set<List> defsOf(ControlFlowGraph cfg, Class<?> nodeClass) {
        return getDefinedLists(getNodeOfType(cfg, nodeClass));
    }

    private Set<List> usesOf(ControlFlowGraph cfg, Class<?> nodeClass) {
        return getUsedLists(getNodeOfType(cfg, nodeClass));
    }

    private CFGNode getNodeOfType(ControlFlowGraph cfg, Class<?> nodeClass) {
        return cfg.getNodes().stream().filter(n -> n.getASTNode() != null && nodeClass.isAssignableFrom(n.getASTNode().getClass())).findFirst().get();
    }

    private Program getAST(String fileName) throws IOException, ParsingException {
        File file = new File(fileName);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode project = objectMapper.readTree(file);
        Program program = ProgramParser.parseProgram("TestProgram", project);
        return program;
    }

    private ControlFlowGraph getCFG(String fileName) throws IOException, ParsingException {
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        visitor.visit(getAST(fileName));
        return visitor.getControlFlowGraph();
    }

    private Set<List> getDefinedLists(CFGNode node) {
        ListDefinitionVisitor visitor = new ListDefinitionVisitor();
        node.getASTNode().accept(visitor);

        Set<List> vars = new LinkedHashSet<>();
        visitor.getDefinitions().forEach(q -> vars.add(new List(q)));

        return vars;
    }

    private Set<List> getUsedLists(CFGNode node) {
        ListUseVisitor visitor = new ListUseVisitor();
        node.getASTNode().accept(visitor);

        Set<List> vars = new LinkedHashSet<>();
        visitor.getUses().forEach(q -> vars.add(new List(q)));

        return vars;
    }
}
