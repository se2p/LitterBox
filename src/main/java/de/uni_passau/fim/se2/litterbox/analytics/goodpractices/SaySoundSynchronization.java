package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
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
public class SaySoundSynchronization extends AbstractIssueFinder {

    public static final String NAME = "say_sound_synchronization";
    private boolean saySomething = false;
    private boolean afterSound = false;

    @Override
    public void visit(StmtList node) {
        node.getStmts().forEach(stmt -> {
            System.out.println(stmt);
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
                addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);}
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
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.GOOD_PRACTICE;
    }
}
