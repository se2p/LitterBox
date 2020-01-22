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
package analytics.bugpattern;

import analytics.IssueFinder;
import analytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.event.ReceptionOfMessage;
import scratch.ast.model.literals.StringLiteral;
import scratch.ast.model.statement.common.Broadcast;
import scratch.ast.model.statement.common.BroadcastAndWait;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.*;

public class MessageNeverSent implements IssueFinder, ScratchVisitor {

    public static final String NAME = "message_Never_Sent";
    public static final String SHORT_NAME = "messNeverSent";

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
        for (Pair received : messageReceived) {
            boolean isReceived = false;
            for (Pair sent : messageSent) {
                if (received.msgName.toLowerCase().equals(sent.msgName.toLowerCase())) {
                    isReceived = true;
                    break;
                }
            }
            if (!isReceived) {
                nonSyncedPairs.add(received);
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
    public void visit(Script node) {
        if (node.getStmtList().getStmts().getListOfStmt().size() > 0 && node.getEvent() instanceof ReceptionOfMessage) {
            ReceptionOfMessage event = (ReceptionOfMessage) node.getEvent();
            if (event.getMsg().getMessage() instanceof StringLiteral) {
                final String actorName = currentActor.getIdent().getName();
                final String msgName = ((StringLiteral) event.getMsg().getMessage()).getText();
                messageReceived.add(new Pair(actorName, msgName));
            }
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    /**
     * Helper class to map which messages are sent / received by which actor
     */
    private static class Pair {
        private String actorName;
        String msgName;

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
