/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Pair;

import java.util.*;
import java.util.stream.Stream;

abstract class GgnnGraphEdgesVisitor implements ScratchVisitor {
    protected final List<Pair<ASTNode>> edges = new ArrayList<>();

    static List<Pair<ASTNode>> getChildEdges(final ASTNode node) {
        return getEdges(new ChildEdgesVisitor(), node);
    }

    static List<Pair<ASTNode>> getNextTokenEdges(final ASTNode node) {
        return getEdges(new NextTokenVisitor(), node);
    }

    static List<Pair<ASTNode>> getGuardedByEdges(final ASTNode node) {
        return getEdges(new GuardedByVisitor(), node);
    }

    static List<Pair<ASTNode>> getComputedFromEdges(final ASTNode node) {
        return getEdges(new ComputedFromVisitor(), node);
    }

    static List<Pair<ASTNode>> getParameterPassingEdges(final Program program, final ASTNode node) {
        return getEdges(new ParameterPassingVisitor(program), node);
    }

    static List<Pair<ASTNode>> getMessagePassingEdges(final ASTNode node) {
        return getEdges(new MessagePassingVisitor(), node);
    }

    private static List<Pair<ASTNode>> getEdges(final GgnnGraphEdgesVisitor v, final ASTNode node) {
        node.accept(v);
        return v.getEdges();
    }

    protected List<Pair<ASTNode>> getEdges() {
        return edges;
    }

    @Override
    public void visitChildren(ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            if (!AstNodeUtil.isMetadata(node)) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(DeclarationStmtList node) {
        // intentionally empty
    }

    @Override
    public void visit(SetStmtList node) {
        // intentionally empty
    }

    @Override
    public void visit(Metadata node) {
        // intentionally empty
    }

    protected Stream<? extends ASTNode> childrenWithoutMetadata(final ASTNode node) {
        return node.getChildren().stream().filter(c -> !AstNodeUtil.isMetadata(c));
    }

    static class ChildEdgesVisitor extends GgnnGraphEdgesVisitor {
        @Override
        public void visit(ASTNode node) {
            childrenWithoutMetadata(node).forEach(child -> edges.add(Pair.of(node, child)));
            super.visit(node);
        }
    }

    static class NextTokenVisitor extends GgnnGraphEdgesVisitor {
        @Override
        public void visit(ASTNode node) {
            List<? extends ASTNode> children = node.getChildren();
            for (int i = 0; i < children.size() - 1; ++i) {
                ASTNode curr = children.get(i);
                ASTNode next = children.get(i + 1);

                if (!AstNodeUtil.isMetadata(curr) && !AstNodeUtil.isMetadata(next)) {
                    edges.add(Pair.of(curr, next));
                }
            }

            super.visit(node);
        }
    }

    private static class GuardedByVisitor extends GgnnGraphEdgesVisitor {
        @Override
        public void visit(IfElseStmt node) {
            DefineableUsesVisitor guardsVisitor = DefineableUsesVisitor.visitNode(node.getBoolExpr());
            DefineableUsesVisitor thenStmtVisitor = DefineableUsesVisitor.visitNode(node.getThenStmts());
            DefineableUsesVisitor elseStmtVisitor = DefineableUsesVisitor.visitNode(node.getElseStmts());

            connectVars(node.getBoolExpr(), guardsVisitor.getVariables(), thenStmtVisitor.getVariables());
            connectAttributes(node.getBoolExpr(), guardsVisitor.getAttributes(), thenStmtVisitor.getAttributes());

            connectVars(node.getBoolExpr(), guardsVisitor.getVariables(), elseStmtVisitor.getVariables());
            connectAttributes(node.getBoolExpr(), guardsVisitor.getAttributes(), elseStmtVisitor.getAttributes());
        }

        @Override
        public void visit(IfThenStmt node) {
            guardedByCBlock(node.getBoolExpr(), node.getThenStmts());
        }

        @Override
        public void visit(RepeatTimesStmt node) {
            guardedByCBlock(node.getTimes(), node.getStmtList());
        }

        @Override
        public void visit(UntilStmt node) {
            guardedByCBlock(node.getBoolExpr(), node.getStmtList());
        }

        private void guardedByCBlock(final Expression guardExpression, final ASTNode body) {
            DefineableUsesVisitor guardsVisitor = DefineableUsesVisitor.visitNode(guardExpression);
            DefineableUsesVisitor usesVisitor = DefineableUsesVisitor.visitNode(body);

            connectVars(guardExpression, guardsVisitor.getVariables(), usesVisitor.getVariables());
            connectAttributes(guardExpression, guardsVisitor.getAttributes(), usesVisitor.getAttributes());
        }

        private void connectVars(final Expression guardExpression, final Map<String, List<Variable>> guards,
                                 final Map<String, List<Variable>> inBlock) {
            for (Map.Entry<String, List<Variable>> usedVar : inBlock.entrySet()) {
                if (!guards.containsKey(usedVar.getKey())) {
                    continue;
                }

                for (Variable v : usedVar.getValue()) {
                    edges.add(Pair.of(v, guardExpression));
                }
            }
        }

        private void connectAttributes(final Expression guardExpression, final List<ASTNode> guards,
                                       final List<ASTNode> inBlock) {
            for (ASTNode guard : guards) {
                for (ASTNode used : inBlock) {
                    if (guard.equals(used)) {
                        edges.add(Pair.of(used, guardExpression));
                    }
                }
            }
        }
    }

    private static class ComputedFromVisitor extends GgnnGraphEdgesVisitor {
        @Override
        public void visit(ChangeVariableBy node) {
            if (node.getIdentifier() instanceof Qualified qualified) {
                addEdges(qualified, node.getExpr());
            }
        }

        @Override
        public void visit(SetVariableTo node) {
            if (node.getIdentifier() instanceof Qualified qualified) {
                addEdges(qualified, node.getExpr());
            }
        }

        private void addEdges(final Qualified assignTo, final ASTNode expr) {
            DefineableUsesVisitor v = DefineableUsesVisitor.visitNode(expr);
            Stream<Variable> variables = v.getVariables().values().stream().flatMap(List::stream);
            Stream<ASTNode> attributes = v.getAttributes().stream();

            Stream.concat(variables, attributes)
                    .forEach(variable -> edges.add(Pair.of(assignTo.getSecond(), variable)));
        }
    }

    private static class ParameterPassingVisitor extends GgnnGraphEdgesVisitor {
        private final ProcedureDefinitionNameMapping procedureMapping;
        private final Map<LocalIdentifier, ProcedureDefinition> procedures = new IdentityHashMap<>();

        ParameterPassingVisitor(final Program program) {
            this.procedureMapping = program.getProcedureMapping();
        }

        @Override
        public void visit(ProcedureDefinitionList node) {
            for (ProcedureDefinition procedureDefinition : node.getList()) {
                procedures.put(procedureDefinition.getIdent(), procedureDefinition);
            }
            super.visit(node);
        }

        @Override
        public void visit(CallStmt node) {
            findCalledProcedure(node).ifPresent(procedure -> connectParameters(node, procedure));
        }

        private Optional<ProcedureDefinition> findCalledProcedure(final CallStmt callStmt) {
            String procedureName = callStmt.getIdent().getName();
            String sprite = AstNodeUtil.findActor(callStmt).orElseThrow().getIdent().getName();

            return procedureMapping.getProceduresForName(sprite, procedureName)
                    .stream()
                    .filter(procedure -> hasMatchingParameterCount(callStmt, procedure.getRight()))
                    .map(org.apache.commons.lang3.tuple.Pair::getKey)
                    .map(procedures::get)
                    .findFirst();
        }

        private boolean hasMatchingParameterCount(final CallStmt callStmt, final ProcedureInfo procedure) {
            int passedArgumentCount = callStmt.getExpressions().getExpressions().size();
            int acceptingArgumentCount = procedure.getArguments().length;
            return passedArgumentCount == acceptingArgumentCount;
        }

        private void connectParameters(final CallStmt callStmt, final ProcedureDefinition procedure) {
            List<Expression> passedArguments = callStmt.getExpressions().getExpressions();
            List<ParameterDefinition> parameters = procedure.getParameterDefinitionList().getParameterDefinitions();

            if (passedArguments.isEmpty()) {
                edges.add(Pair.of(callStmt, procedure));
            } else {
                for (int i = 0; i < passedArguments.size(); ++i) {
                    edges.add(Pair.of(passedArguments.get(i), parameters.get(i)));
                }
            }
        }
    }

    private static class MessagePassingVisitor extends GgnnGraphEdgesVisitor {
        private final Map<String, List<ASTNode>> senders = new HashMap<>();
        private final Map<String, List<ReceptionOfMessage>> receivers = new HashMap<>();

        @Override
        protected List<Pair<ASTNode>> getEdges() {
            List<Pair<ASTNode>> edges = new ArrayList<>();

            for (Map.Entry<String, List<ASTNode>> messageSenders : senders.entrySet()) {
                String message = messageSenders.getKey();
                if (!receivers.containsKey(message)) {
                    continue;
                }

                List<ASTNode> sendingNodes = messageSenders.getValue();
                List<ReceptionOfMessage> receivingNodes = receivers.get(message);

                for (ASTNode sender : sendingNodes) {
                    for (ASTNode receiver : receivingNodes) {
                        edges.add(Pair.of(sender, receiver));
                    }
                }
            }

            return edges;
        }

        @Override
        public void visit(ReceptionOfMessage node) {
            addReceiver(node.getMsg().getMessage().toString(), node);
            super.visit(node);
        }

        @Override
        public void visit(Broadcast node) {
            addSender(node.getMessage().getMessage().toString(), node);
            super.visit(node);
        }

        @Override
        public void visit(BroadcastAndWait node) {
            addSender(node.getMessage().getMessage().toString(), node);
            super.visit(node);
        }

        private void addSender(String message, ASTNode sender) {
            senders.compute(message, (msg, senderList) -> addToListOrCreate(senderList, sender));
        }

        private void addReceiver(String message, ReceptionOfMessage receiver) {
            receivers.compute(message, (msg, receiverList) -> addToListOrCreate(receiverList, receiver));
        }

        private static <T> List<T> addToListOrCreate(List<T> list, T element) {
            List<T> nonNullList = list;
            if (list == null) {
                nonNullList = new ArrayList<>();
            }
            nonNullList.add(element);
            return nonNullList;
        }
    }
}
