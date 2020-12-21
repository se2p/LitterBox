package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiBlockIssue extends Issue {

    private List<ASTNode> nodes = new ArrayList<>();

    public MultiBlockIssue(IssueFinder finder, IssueSeverity severity, Program program, ActorDefinition actor, Script script, List<ASTNode> nodes, Metadata metaData, Hint hint) {
        super(finder, severity, program, actor, script, nodes.get(0), metaData, hint);
        this.nodes.addAll(nodes);
    }

    public MultiBlockIssue(IssueFinder finder, IssueSeverity severity, Program program, ActorDefinition actor, ProcedureDefinition procedure, List<ASTNode> nodes, Metadata metaData, Hint hint) {
        super(finder, severity, program, actor, procedure, nodes.get(0), metaData, hint);
        this.nodes.addAll(nodes);
    }

    @Override
    public boolean isCodeLocation(ASTNode node) {
        return nodes.contains(node);
    }

    @Override
    public boolean hasMultipleBlocks() {
        return nodes.size() > 1;
    }

    public List<ASTNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }
}
