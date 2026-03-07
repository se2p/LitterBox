/*
 * Copyright (C) 2019-2026 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.PickRandom;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.TranslateTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.SetVideoTransparency;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.TurnVideo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.VideoOn;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.WhenVideoMotionGreaterThan;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.DataExpr;
import de.uni_passau.fim.se2.litterbox.ast.util.NumberUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TranslateExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.VideoSensingExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.dataflow.*;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;

import java.util.*;
import java.util.function.Function;

/**
 * Detects type errors using dataflow analysis and type inference.
 */
public class TypeError extends AbstractIssueFinder implements TextToSpeechExtensionVisitor, TranslateExtensionVisitor, VideoSensingExtensionVisitor {
    public static final String NAME = "type_error";
    public static final String WEIRD_DISTANCE = "type_error_weird_distance";

    public static final String TYPE_ERROR_COMPARISON = "type_error_comparison";
    public static final String TYPE_ERROR_MISMATCH = "type_error_mismatch";

    public enum Type {
        BOOLEAN, NUMBER, STRING, LOUDNESS, POSITION, DIRECTION, COLOR, ANY, TOP,
        /**
         * Bottom element of the lattice (unreachable code / uninitialized).
         */
        BOTTOM
    }

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
        if (facts == null) {
            return id -> Type.ANY;
        }

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

            if (actual == Type.STRING && expr instanceof StringLiteral lit) {
                if (lit.getText().isEmpty()) {
                    return;
                }
                // Allow valid number strings if Number is expected
                if (expected == Type.NUMBER && NumberUtil.VALID_NUMBER.matcher(lit.getText().trim()).matches()) {
                    return;
                }
            }

            if (!isCompatible(expected, actual)) {
                Hint hint = Hint.fromKey(TYPE_ERROR_MISMATCH);
                hint.setParameter("BLOCK", getFriendlyNodeName(node));
                hint.setParameter("EXPECTED", new HintPlaceholder.Translatable(getTypeTranslationKey(expected)));
                hint.setParameter("ACTUAL", new HintPlaceholder.Translatable(getTypeTranslationKey(actual)));
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    private void checkComparison(BinaryExpression<?, ?> node) {
        // In comparisons, we expect operands to be compatible with each other.
        // Scratch is loosely typed, but some things are clearly odd.
        if (node.getChildren().size() >= 2) {
            if (node.getChildren().get(0) instanceof Expression left
                    && node.getChildren().get(1) instanceof Expression right) {
                Type leftType = inferType(left, createNodeResolver(node));
                Type rightType = inferType(right, createNodeResolver(node));

                checkComparisonCompatibility(node, left, right, leftType, rightType);
            }
        }
    }

    private void checkComparisonCompatibility(
            ASTNode node, Expression leftNode, Expression rightNode, Type left, Type right
    ) {
        if (left == Type.ANY || right == Type.ANY) {
            return;
        }
        if (left == Type.TOP || right == Type.TOP) {
            return;
        }
        if (left == Type.BOTTOM || right == Type.BOTTOM) {
            return;
        }

        boolean mismatch = false;
        if (left == Type.BOOLEAN && right == Type.NUMBER) {
            mismatch = true;
        } else if (left == Type.NUMBER && right == Type.BOOLEAN) {
            mismatch = true;
        } else if (left == Type.BOOLEAN && right == Type.STRING) {
            mismatch = true;
        } else if (left == Type.STRING && right == Type.BOOLEAN) {
            mismatch = true;
        } else if (left == Type.NUMBER && right == Type.STRING) {
            mismatch = true;
        } else if (left == Type.STRING && right == Type.NUMBER) {
            mismatch = true;
        } else if (left == Type.BOOLEAN && right == Type.BOOLEAN) {
            if (node instanceof LessThan || node instanceof BiggerThan) {
                mismatch = true;
            }
        }

        if (mismatch) {
            Hint hint = Hint.fromKey(TYPE_ERROR_COMPARISON);
            hint.setParameter("LEFT", getFriendlyNodeName(leftNode));
            hint.setParameter("RIGHT", getFriendlyNodeName(rightNode));
            hint.setParameter("TYPE_LEFT", new HintPlaceholder.Translatable(getTypeTranslationKey(left)));
            hint.setParameter("TYPE_RIGHT", new HintPlaceholder.Translatable(getTypeTranslationKey(right)));
            addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
        }
    }

    private boolean isCompatible(Type expected, Type actual) {
        if (actual == Type.ANY || actual == Type.TOP || actual == Type.BOTTOM) {
            return true;
        }
        if (expected == actual) {
            return true;
        }

        if (expected == Type.NUMBER) {
            return actual == Type.POSITION || actual == Type.DIRECTION || actual == Type.LOUDNESS;
        }

        return false;
    }

    private String getTypeTranslationKey(Type type) {
        return switch (type) {
            case BOOLEAN -> "type_boolean";
            case NUMBER -> "type_number";
            case STRING -> "type_string";
            case COLOR -> "type_color";
            case POSITION -> "type_position";
            case DIRECTION -> "type_direction";
            case LOUDNESS -> "type_loudness";
            default -> "type_void";
        };
    }

    private String getFriendlyNodeName(ASTNode node) {
        String sb = ScratchBlocksVisitor.ofIgnoringLooseBlocks(node);
        // Literals (Number/String) produce output like (4) or [text] which should not be wrapped in [sbi].
        // Blocks produce output that needs [sbi] to be rendered correctly.
        if (node instanceof NumberLiteral || node instanceof StringLiteral) {
            return sb;
        }
        return "[sbi]" + sb + "[/sbi]";
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
    public void visit(GlideSecsTo node) {
        checkType(node.getPosition(), Type.POSITION);
        visitChildren(node);
    }

    @Override
    public void visit(GlideSecsToXY node) {
        checkType(node.getX(), Type.NUMBER);
        checkType(node.getY(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(GoToPos node) {
        checkType(node.getPosition(), Type.POSITION);
        visitChildren(node);
    }

    @Override
    public void visit(GoToPosXY node) {
        checkType(node.getX(), Type.NUMBER);
        checkType(node.getY(), Type.NUMBER);
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
    public void visit(PointTowards node) {
        checkType(node.getPosition(), Type.POSITION);
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
    public void visit(ChangeLayerBy node) {
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
    public void visit(PenBlock node) {
        if (node instanceof SetPenColorToColorStmt stmt) {
            checkType(stmt.getColorExpr(), Type.COLOR);
            visitChildren(node);
        }
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
    public void visit(SayForSecs node) {
        // Don't need to check "thought" as anything can be interpreted as text
        checkType(node.getSecs(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(ThinkForSecs node) {
        // Don't need to check "thought" as anything can be interpreted as text
        checkType(node.getSecs(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(PickRandom node) {
        checkType(node.getOperand1(), Type.NUMBER);
        checkType(node.getOperand2(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(Speak node) {
        checkType(node.getText(), Type.STRING);
        visitChildren(node);
    }

    @Override
    public void visit(SetVoice node) {
        if (node.getVoice() instanceof ExprVoice exprVoice) {
            checkType(exprVoice.getExpr(), Type.STRING);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SetLanguage node) {
        if (node.getLanguage() instanceof ExprLanguage exprLanguage) {
            checkType(exprLanguage.getExpr(), Type.STRING);
        }
        visitChildren(node);
    }

    @Override
    public void visit(TranslateTo node) {
        checkType(node.getText(), Type.STRING);
        if (node.getLanguage() instanceof TExprLanguage tExprLanguage) {
            checkType(tExprLanguage.getExpr(), Type.STRING);
        }
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
        keys.add(TYPE_ERROR_COMPARISON);
        keys.add(TYPE_ERROR_MISMATCH);
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
            if (t1 == t2) {
                return t1;
            }
            if (t1 == Type.TOP || t2 == Type.TOP) {
                return Type.TOP;
            }
            if (t1 == Type.ANY || t2 == Type.ANY) {
                return Type.ANY; // Wildcard dominates specific types (safe assumption)
            }
            return Type.TOP; // Conflict (e.g. NUMBER vs STRING)
        }
    }

    private static Type inferType(Expression expr, Function<Identifier, Type> resolver) {
        return switch (expr) {
            case Identifier id -> resolver.apply(id);
            case DataExpr dataExpr -> resolver.apply(dataExpr.getName());
            case Answer ignored -> Type.ANY;
            case NumberLiteral ignored -> Type.NUMBER;
            case StringLiteral ignored -> Type.STRING;
            case Color ignored -> Type.COLOR;
            case BoolExpr ignored -> Type.BOOLEAN;
            case NumExpr ignored -> Type.NUMBER;
            case AsString asString -> {
                if (asString.getOperand1() instanceof Touching || asString.getOperand1() instanceof Not) {
                    yield Type.BOOLEAN;
                }
                yield Type.STRING;
            }
            case StringExpr ignored -> Type.ANY; // e.g. JoinString can be anything
            case ComparableExpr ignored -> Type.ANY;
            default -> Type.ANY; // Implicit cast or unknown
        };
    }

    @Override
    public void visit(WhenVideoMotionGreaterThan node) {
        checkType(node.getReference(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(VideoOn node) {
        visitChildren(node);
    }

    @Override
    public void visit(TurnVideo node) {
        visitChildren(node);
    }

    @Override
    public void visit(SetVideoTransparency node) {
        checkType(node.getTransparency(), Type.NUMBER);
        visitChildren(node);
    }

    /*
     * Blocks not handled yet because they might require further types
     * - GoToLayer
     * - SwitchCostumeTo
     * - SwitchBackdrop
     * - SwitchBackdropAndWait
     */
}
