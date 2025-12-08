/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.DataExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ChangeGraphicEffectBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ChangeSoundEffectBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ChangeVolumeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetSoundEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetVolumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ChangeSizeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysis;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysisBuilder;
import de.uni_passau.fim.se2.litterbox.dataflow.ForwardFlowDirection;
import de.uni_passau.fim.se2.litterbox.dataflow.JoinFunction;
import de.uni_passau.fim.se2.litterbox.dataflow.TransferFunction;

import java.util.*;
import java.util.function.Function;

/**
 * Detects type errors using dataflow analysis and type inference.
 */
public class TypeError extends AbstractIssueFinder {
    public static final String NAME = "type_error";
    public static final String WEIRD_DISTANCE = "type_error_weird_distance";

    private DataflowAnalysis<VariableTypeFact> analysis;
    private final Map<ASTNode, CFGNode> astToCfg = new HashMap<>();

    @Override
    public Set<Issue> check(Program program) {
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        program.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();

        this.analysis = new DataflowAnalysisBuilder<VariableTypeFact>(cfg)
                .withFlowDirection(new ForwardFlowDirection() {
                    @Override
                    public Iterable<CFGNode> getInitialNodes(ControlFlowGraph cfg) {
                        List<CFGNode> initialNodes = new ArrayList<>();
                        // Add standard entry nodes
                        super.getInitialNodes(cfg).forEach(initialNodes::add);
                        // Add all procedure nodes as they are also entry points
                        // but they are not connected to entry in the CFG
                        cfg.getNodes().stream()
                            .filter(n -> n instanceof ProcedureNode)
                            .forEach(initialNodes::add);
                        return initialNodes;
                    }
                })
                .withTransferFunction(new TypeTransferFunction())
                .withJoinFunction(new TypeJoinFunction())
                .withMayInitialization()
                .build();
        this.analysis.applyAnalysis();

        this.astToCfg.clear();
        buildCFGMap(this.analysis.getCFG());
        return super.check(program);
    }

    private void buildCFGMap(ControlFlowGraph cfg) {
        for (CFGNode node : cfg.getNodes()) {
            if (node.getASTNode() != null) {
                astToCfg.put(node.getASTNode(), node);
            }
        }
    }

    private Function<Identifier, Type> createNodeResolver(ASTNode node) {
        CFGNode cfgNode = null;
        ASTNode current = node;
        while (cfgNode == null && current != null) {
            if (astToCfg.containsKey(current)) {
                cfgNode = astToCfg.get(current);
            } else {
                current = current.getParentNode();
            }
        }

        if (cfgNode == null) {
             return id -> Type.ANY;
        }
        Set<VariableTypeFact> facts = analysis.getDataflowFacts(cfgNode);
        if (facts == null) return id -> Type.ANY;
        
        return createFactResolver(facts);
    }

    private static boolean idsEqual(Identifier a, Identifier b) {
        if (a instanceof LocalIdentifier la && b instanceof LocalIdentifier lb) {
            return la.getName().equals(lb.getName());
        }
        return a.equals(b);
    }

    private static Function<Identifier, Type> createFactResolver(Set<VariableTypeFact> facts) {
        return identifier -> {
            for (VariableTypeFact fact : facts) {
                if (fact.variable() instanceof Variable v) {
                    if (idsEqual(v.getIdentifier(), identifier)) {
                        return fact.type();
                    }
                }
            }
            return Type.BOTTOM;
        };
    }
    
    // --- Helper Methods ---

    private void checkType(ASTNode node, Type expected) {
        if (node instanceof Expression expr) {
             Type actual = inferType(expr, createNodeResolver(node));
             
             if (!isCompatible(expected, actual)) {
                 addIssue(node, node.getMetadata(), IssueSeverity.LOW);
             }
        }
    }
    
    private void checkComparison(BinaryExpression<?, ?> node) {
        // In comparisons, we expect operands to be compatible with each other.
        // Scratch is loosely typed, but some things are clearly odd.
        if (node.getChildren().size() >= 2) {
            if (node.getChildren().get(0) instanceof Expression left && node.getChildren().get(1) instanceof Expression right) {
                Type leftType = inferType(left, createNodeResolver(node));
                Type rightType = inferType(right, createNodeResolver(node));
                
                checkComparisonCompatibility(node, leftType, rightType);
            }
        }
    }

    private void checkComparisonCompatibility(ASTNode node, Type left, Type right) {
        if (left == Type.ANY || right == Type.ANY) return;
        if (left == Type.TOP || right == Type.TOP) return;
        if (left == Type.BOTTOM || right == Type.BOTTOM) return;

        if (left == Type.BOOLEAN && right == Type.NUMBER) {
             addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        } else if (left == Type.NUMBER && right == Type.BOOLEAN) {
             addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        } else if (left == Type.BOOLEAN && right == Type.STRING) {
             addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        } else if (left == Type.STRING && right == Type.BOOLEAN) {
             addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        } else if (left == Type.NUMBER && right == Type.STRING) {
             addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        } else if (left == Type.STRING && right == Type.NUMBER) {
             addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        } else if (left == Type.BOOLEAN && right == Type.BOOLEAN) {
             if (node instanceof LessThan || node instanceof BiggerThan) {
                 addIssue(node, node.getMetadata(), IssueSeverity.LOW);
             }
        }
    }

    private boolean isCompatible(Type expected, Type actual) {
        if (actual == Type.ANY || actual == Type.TOP || actual == Type.BOTTOM) return true;
        if (expected == actual) return true;

        if (expected == Type.NUMBER) {
            return actual == Type.POSITION || actual == Type.DIRECTION || actual == Type.LOUDNESS;
        }
        
        return false;
    }

    // --- Visitor Methods ---

    @Override
    public void visit(LessThan node) {
        checkComparison(node);
        visitChildren(node); 
    }

    @Override
    public void visit(BiggerThan node) {
        checkComparison(node);
        visitChildren(node);
    }

    @Override
    public void visit(Equals node) {
        checkComparison(node);
        visitChildren(node);
    }

    @Override
    public void visit(DistanceTo node) {
        // DistanceTo returns Number.
        Position position = node.getPosition();
        if (position instanceof FromExpression fromExpression) {
            Hint hint = Hint.fromKey(WEIRD_DISTANCE);
            if (!(fromExpression.getStringExpr() instanceof AsString asString)) {
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            } else if (!((asString.getOperand1() instanceof StrId) || asString.getOperand1() instanceof Qualified)) {
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        checkType(node.getBoolExpr(), Type.BOOLEAN);
        visitChildren(node);
    }

    @Override
    public void visit(IfElseStmt node) {
        checkType(node.getBoolExpr(), Type.BOOLEAN);
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        checkType(node.getBoolExpr(), Type.BOOLEAN);
        visitChildren(node);
    }

    @Override
    public void visit(WaitUntil node) {
        checkType(node.getUntil(), Type.BOOLEAN);
        visitChildren(node);
    }

    @Override
    public void visit(And node) {
        checkType(node.getOperand1(), Type.BOOLEAN);
        checkType(node.getOperand2(), Type.BOOLEAN);
        visitChildren(node);
    }

    @Override
    public void visit(Or node) {
        checkType(node.getOperand1(), Type.BOOLEAN);
        checkType(node.getOperand2(), Type.BOOLEAN);
        visitChildren(node);
    }

    @Override
    public void visit(Not node) {
        checkType(node.getOperand1(), Type.BOOLEAN);
        visitChildren(node);
    }
    
    @Override
    public void visit(ChangeVariableBy node) {
        checkType(node.getExpr(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(SetXTo node) {
        checkType(node.getNum(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(SetYTo node) {
        checkType(node.getNum(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(ChangeXBy node) {
        checkType(node.getNum(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(ChangeYBy node) {
        checkType(node.getNum(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(MoveSteps node) {
        checkType(node.getSteps(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(TurnLeft node) {
        checkType(node.getDegrees(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(TurnRight node) {
        checkType(node.getDegrees(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(PointInDirection node) {
        checkType(node.getDirection(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(SetSizeTo node) {
        checkType(node.getPercent(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(ChangeSizeBy node) {
        checkType(node.getNum(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(SetVolumeTo node) {
        checkType(node.getVolumeValue(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        checkType(node.getVolumeValue(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        checkType(node.getValue(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        checkType(node.getValue(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        checkType(node.getValue(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        checkType(node.getValue(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(WaitSeconds node) {
        checkType(node.getSeconds(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        checkType(node.getTimes(), Type.NUMBER);
        visitChildren(node);
    }
    
    @Override
    public void visit(SetPenColorToColorStmt node) {
        checkType(node.getColorExpr(), Type.COLOR);
        visitChildren(node);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        checkType(node.getColor(), Type.COLOR);
        visitChildren(node);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        checkType(node.getOperand1(), Type.COLOR);
        checkType(node.getOperand2(), Type.COLOR);
        visitChildren(node);
    }

    @Override
    public void visit(AsBool node) {
        checkType(node.getOperand1(), Type.BOOLEAN);
        visitChildren(node);
    }

    @Override
    public void visit(AsNumber node) {
        checkType(node.getOperand1(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(PickRandom node) {
        checkType(node.getOperand1(), Type.NUMBER);
        checkType(node.getOperand2(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(NAME);
        keys.add(WEIRD_DISTANCE);
        return keys;
    }

    // --- Inner Classes & Methods for Dataflow & Inference ---

    /**
     * Represents a dataflow fact associating a variable with its inferred Type at a specific program point.
     */
    private record VariableTypeFact(Defineable variable, Type type) implements DataflowFact {

        @Override
        public String toString() {
            return variable + " -> " + type;
        }
    }

    /**
     * Computes the transfer function for the dataflow analysis, updating variable types
     * based on assignments and modifications (e.g., SetVariableTo, ChangeVariableBy).
     */
    private static class TypeTransferFunction implements TransferFunction<VariableTypeFact> {

        @Override
        public Set<VariableTypeFact> apply(CFGNode node, Set<VariableTypeFact> inFacts) {
            Set<VariableTypeFact> outFacts = new LinkedHashSet<>(inFacts);

            Set<Definition> definitions = node.getDefinitions();
            ASTNode stmt = node.getASTNode();

            // Kill facts based on definitions
            if (stmt instanceof SetVariableTo setStmt) {
                 Type type = inferType(setStmt.getExpr(), createFactResolver(inFacts));
                 updateFact(outFacts, setStmt.getIdentifier(), type);
            } else if (stmt instanceof ChangeVariableBy changeStmt) {
                 updateFact(outFacts, changeStmt.getIdentifier(), Type.NUMBER);
            } else {
                 // For all other statements, check definitions
                 for (Definition def : definitions) {
                     Defineable definable = def.getDefinable();
                     if (definable instanceof Variable variable) {
                          updateFact(outFacts, variable.getIdentifier(), Type.ANY);
                     }
                 }
                 
                 if (stmt instanceof ProcedureDefinition procDef) {
                     for (ParameterDefinition param : procDef.getParameterDefinitionList().getParameterDefinitions()) {
                         Type analysisType = Type.ANY;
                         de.uni_passau.fim.se2.litterbox.ast.model.type.Type astType = param.getType();
                         if (astType instanceof BooleanType) {
                             analysisType = Type.BOOLEAN;
                         } else if (astType instanceof NumberType) {
                             analysisType = Type.NUMBER;
                         } else if (astType instanceof StringType) {
                             analysisType = Type.STRING;
                         }
                         updateFact(outFacts, param.getIdent(), analysisType);
                     }
                 }
            }

            return outFacts;
        }

        private void updateFact(Set<VariableTypeFact> facts, Identifier id, Type type) {
            Variable variable = new Variable(id);
            facts.removeIf(f -> f.variable() instanceof Variable v && idsEqual(v.getIdentifier(), id));
            facts.add(new VariableTypeFact(variable, type));
        }
    }

    /**
     * Merges dataflow facts from multiple control flow paths, using a Least Upper Bound (LUB) approach
     * to resolve type conflicts (e.g., merging NUMBER and STRING results in TOP or ANY).
     */
    private static class TypeJoinFunction implements JoinFunction<VariableTypeFact> {

        @Override
        public Set<VariableTypeFact> apply(Set<Set<VariableTypeFact>> inFacts) {
            if (inFacts.isEmpty()) {
                return Collections.emptySet();
            }

            // Map variable to joined Type
            Map<Defineable, Type> joinedTypes = new LinkedHashMap<>();
            
            for (Set<VariableTypeFact> factSet : inFacts) {
                for (VariableTypeFact fact : factSet) {
                    Defineable var = fact.variable();
                    Type type = fact.type();
                    
                    joinedTypes.merge(var, type, this::mergeTypes);
                }
            }
            
            Set<VariableTypeFact> result = new LinkedHashSet<>();
            for (Map.Entry<Defineable, Type> entry : joinedTypes.entrySet()) {
                result.add(new VariableTypeFact(entry.getKey(), entry.getValue()));
            }
            
            return result;
        }

        private Type mergeTypes(Type t1, Type t2) {
            if (t1 == t2) return t1;
            if (t1 == Type.TOP || t2 == Type.TOP) return Type.TOP;
            if (t1 == Type.ANY || t2 == Type.ANY) return Type.ANY; // Wildcard dominates specific types (safe assumption)
            return Type.TOP; // Conflict (e.g. NUMBER vs STRING)
        }
    }

    private static Type inferType(Expression expr, Function<Identifier, Type> resolver) {
        if (expr instanceof Identifier id) {
             return resolver.apply(id);
        } else if (expr instanceof DataExpr dataExpr) {
            return resolver.apply(dataExpr.getName());
        } else if (expr instanceof Answer) {
            return Type.ANY;
        } else if (expr instanceof NumberLiteral) {
            return Type.NUMBER;
        } else if (expr instanceof StringLiteral) {
            return Type.STRING;
        } else if (expr instanceof Color) {
            return Type.COLOR;
        } else if (expr instanceof BoolExpr) {
            return Type.BOOLEAN;
        } else if (expr instanceof NumExpr) {
            return Type.NUMBER;
        } else if (expr instanceof AsString asString) {
             if (asString.getOperand1() instanceof Touching || asString.getOperand1() instanceof Not) {
                 return Type.BOOLEAN;
             }
             return Type.STRING;
        } else if (expr instanceof StringExpr) {
            return Type.ANY; // e.g. JoinString can be anything
        } else if (expr instanceof ComparableExpr) {
            return Type.ANY; 
        } else if (expr instanceof AsNumber) {
            return Type.NUMBER;
        }
        return Type.ANY; // Implicit cast or unknown
    }
}
