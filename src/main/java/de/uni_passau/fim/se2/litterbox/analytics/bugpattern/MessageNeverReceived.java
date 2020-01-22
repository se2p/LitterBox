/*
 * Copyright (C) 2019 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import java.util.*;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class MessageNeverReceived implements IssueFinder, ScratchVisitor {

    public static final String NAME = "never_receive_message";
    public static final String SHORT_NAME = "neverRecMess";

    private List<Pair> messageSent = new ArrayList<>();
    private List<Pair> messageReceived = new ArrayList<>();
    private ActorDefinition currentActor;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        messageSent = new ArrayList<>();
        messageReceived = new ArrayList<>();
        program.accept(this);

        final LinkedHashSet<Pair> nonSyncedPairs = new LinkedHashSet<>();

        for (Pair sent : messageSent) {
            boolean isReceived = false;
            for (Pair received : messageReceived) {
                if (sent.msgName.toLowerCase().equals(received.msgName.toLowerCase())) {
                    isReceived = true;
                    break;
                }
            }
            if (!isReceived) {
                nonSyncedPairs.add(sent);
            }
        }

        final Set<String> actorNames = new LinkedHashSet<>();
        nonSyncedPairs.forEach(p -> actorNames.add(p.getActorName()));

        return new IssueReport(NAME, nonSyncedPairs.size(), new LinkedList<>(actorNames), "");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Broadcast node) {
        if (node.getMessage().getMessage() instanceof StringLiteral) {
            final String actorName = currentActor.getIdent().getName();
            final String msgName = ((StringLiteral) node.getMessage().getMessage()).getText();
            messageSent.add(new Pair(actorName, msgName));
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (node.getMessage().getMessage() instanceof StringLiteral) {
            final String actorName = currentActor.getIdent().getName();
            final String msgName = ((StringLiteral) node.getMessage().getMessage()).getText();
            messageSent.add(new Pair(actorName, msgName));
        }
    }


    @Override
    public void visit(ReceptionOfMessage node) {
        if (node.getMsg().getMessage() instanceof StringLiteral) {
            final String actorName = currentActor.getIdent().getName();
            final String msgName = ((StringLiteral) node.getMsg().getMessage()).getText();
            messageReceived.add(new Pair(actorName, msgName));
        }
    }

    /**
     * Helper class to map which messages are sent / received by which actor
     */
    private static class Pair {
        String msgName;
        private String actorName;

        public Pair(String actorName, String msgName) {
            this.setActorName(actorName);
            this.msgName = msgName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Pair pair = (Pair) o;

            if (!Objects.equals(getActorName(), pair.getActorName())) {
                return false;
            }
            return Objects.equals(msgName, pair.msgName);
        }

        @Override
        public int hashCode() {
            int result = getActorName() != null ? getActorName().hashCode() : 0;
            result = 31 * result + (msgName != null ? msgName.hashCode() : 0);
            return result;
        }

        String getActorName() {
            return actorName;
        }

        void setActorName(String actorName) {
            this.actorName = actorName;
        }
    }
}
