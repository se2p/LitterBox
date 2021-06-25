package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;

/**
 * Checks if at the same time a speech bubble appears a sound file plays of. And as soon as the sound ends, the
 * speech bubble is removed.
 */
public class SaySoundSynchronisation extends AbstractIssueFinder {

    public static final String NAME = "say_sound_synchronisation";
    private boolean saySomething = false;
    private boolean afterSound = false;

    @Override
    public void visit(StmtList node) {
        node.getStmts().forEach(stmt -> {
            if (stmt instanceof Say) {
                stmt.accept(this);
            } else if (stmt instanceof PlaySoundUntilDone) {
                stmt.accept(this);
            } else {
                afterSound = false;
                saySomething = false;
            }
        });
    }


    @Override
    public void visit(Say node) {
        if (saySomething && afterSound) {
            if (node.getString() instanceof StringLiteral) {
                if (((StringLiteral) node.getString()).getText().isEmpty()) {
                    addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
                }
            }
            saySomething = false;
            afterSound = false;
        } else {
            saySomething = true;
        }
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        if (saySomething) {
            afterSound = true;
        }
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}