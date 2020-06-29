package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractIssueFinder implements IssueFinder, ScratchVisitor {

    protected ActorDefinition currentActor;
    protected Script currentScript;
    protected Set<Issue> issues = new LinkedHashSet<>();
    protected Map<LocalIdentifier, ProcedureInfo> procMap;
    protected Program program;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        program.accept(this);
        return issues;
    }

    @Override
    public void visit(ActorDefinition actor) {
        Preconditions.checkNotNull(program);
        currentActor = actor;
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
        for (ASTNode child : actor.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(Script script) {
        currentScript = script;
        for (ASTNode child : script.getChildren()) {
            child.accept(this);
        }
    }

    protected void addIssue(AbstractNode node, String hintText, Metadata metadata) {
        issues.add(new Issue(this, currentActor, currentScript, node,
                hintText, metadata));
    }

    protected void addIssueWithLooseComment(String hintText) {
        issues.add(new Issue(this, currentActor,
                null, // TODO: There is no script
                currentActor, // TODO: There is no node?
                hintText,
                null)); // TODO: There is no metadata
    }
}
