package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import com.google.common.graph.EndpointPair;
import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.dependency.ProgramDependenceGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeScripts;

import java.util.LinkedHashSet;
import java.util.Set;

public class MergeScriptsFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(ScriptList scriptList) {

        for (Script script1 : scriptList.getScriptList()) {
            for (Script script2 : scriptList.getScriptList()) {
                if (script1 == script2) {
                    continue;
                }

                if (!script1.getEvent().equals(script2.getEvent())) {
                    continue;
                }

                if (!hasDependencies(script1, script2)) {
                    refactorings.add(new MergeScripts(script1, script2));
                }
            }
        }
    }

    /*
     * Since the dependency analysis does not take concurrency into account
     * this method simply applies the merge and checks for dependencies in
     * the resulting script that span across the two parent scripts.
     */
    private boolean hasDependencies(Script script1, Script script2) {

        MergeScripts refactoring = new MergeScripts(script1, script2);
        Script merged = refactoring.getMergedScript();
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        merged.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);

        Set<ASTNode> nodesScript1 = getTransitiveNodes(script1);
        Set<ASTNode> nodesScript2 = getTransitiveNodes(script2);
        for (EndpointPair<CFGNode> edge : pdg.getEdges()) {
            if (edge.nodeU().getASTNode() == null || edge.nodeU().getASTNode() instanceof Event) {
                continue;
            }
            if (edge.nodeV().getASTNode() == null || edge.nodeV().getASTNode() instanceof Event) {
                continue;
            }
            if (nodesScript1.contains(edge.nodeU().getASTNode()) &&
                    nodesScript2.contains(edge.nodeV().getASTNode())) {
                return true;
            }
        }
        return false;
    }

    private Set<ASTNode> getTransitiveNodes(ASTNode node) {
        Set<ASTNode> nodes = new LinkedHashSet<>();
        nodes.addAll(node.getChildren());
        for (ASTNode child : node.getChildren()) {
            nodes.addAll(getTransitiveNodes(child));
        }
        return nodes;
    }

    @Override
    public String getName() {
        return MergeScripts.NAME;
    }
}
