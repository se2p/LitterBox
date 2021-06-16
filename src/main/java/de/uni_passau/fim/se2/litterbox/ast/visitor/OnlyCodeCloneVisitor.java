package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class OnlyCodeCloneVisitor extends CloneVisitor {
    /**
     * Default implementation of visit method for ActorDefinition.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ActorDefinition which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ActorDefinition node) {
        ActorDefinition actorDefinition = new ActorDefinition(node.getActorType(), node.getIdent(), node.getDecls(), node.getSetStmtList(), apply(node.getProcedureDefinitionList()), apply(node.getScripts()), node.getActorMetadata());
        actorDefinition.getScripts().accept(new ParentVisitor());
        actorDefinition.getProcedureDefinitionList().accept(new ParentVisitor());
        return actorDefinition;
    }

    /**
     * Default implementation of visit method for {@link Program}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Program  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Program node) {
        Program program = new Program(node.getIdent(),
                apply(node.getActorDefinitionList()),
                node.getSymbolTable(),
                node.getProcedureMapping(),
                node.getProgramMetadata());
        return program;
    }
}
