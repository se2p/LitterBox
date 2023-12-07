package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.SpriteClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class MissingCloneInitializationFix extends AbstractIssueFinder {
    public static final String NAME = "message_never_sent_fix";
    private final String bugLocationBlockId;
    private boolean firstRun = false;
    private String clonedActor;
    private boolean insideClonedActor;
    private boolean alreadyFound;

    public MissingCloneInitializationFix(String bugLocationBlockId) {
        this.bugLocationBlockId = bugLocationBlockId;
    }

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        firstRun = true;
        program.accept(this);
        firstRun = false;
        if (clonedActor != null) {
            program.accept(this);
        }
        return Collections.unmodifiableSet(issues);
    }

    @Override
    public void visit(ActorDefinition node) {
        if (!firstRun && node.getIdent().getName().equals(clonedActor)) {
            insideClonedActor = true;
        }
        super.visit(node);
        insideClonedActor = false;
    }

    @Override
    public void visit(CreateCloneOf node) {
        if (firstRun && Objects.equals(AstNodeUtil.getBlockId(node), bugLocationBlockId)) {
            if (node.getStringExpr() instanceof AsString asString && asString.getOperand1() instanceof StrId strId) {
                final String spriteName = strId.getName();
                if (spriteName.equals("_myself_")) {
                    clonedActor = currentActor.getIdent().getName();
                } else {
                    clonedActor = spriteName;
                }
            }
        }
    }

    @Override
    public void visit(StartedAsClone node) {
        if (!firstRun && insideClonedActor && !alreadyFound) {
            alreadyFound = true;
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(SpriteClicked node) {
        if (!firstRun && insideClonedActor && !alreadyFound) {
            alreadyFound = true;
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.FIX;
    }
}
