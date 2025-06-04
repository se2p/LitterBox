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
package de.uni_passau.fim.se2.litterbox.ast.util;

import com.google.common.collect.Streams;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.EventAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumFunct;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.NameNum;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.FixedDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.FixedInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.FixedNote;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.GraphicEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SoundEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ForwardBackwardChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.LayerChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.DragMode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.RotationStyle;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.type.Type;
import de.uni_passau.fim.se2.litterbox.ast.visitor.*;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class StructuralEquality {

    private StructuralEquality() {
        throw new IllegalCallerException("utility class");
    }

    /**
     * Checks if two AST nodes are structurally the same.
     *
     * <p>I.e. their tree is the same not regarding metadata information like block IDs.
     *
     * <p>Note: literal values are considered to be part of the structure.
     * E.g. an expression {@code 1 + 2} is structurally different compared to {@code 1 + 3}.
     *
     * @param node1 Some node of the AST.
     * @param node2 Some other node of the AST.
     * @param <N> The type of the nodes.
     * @return True, if the two given nodes are structurally equal.
     */
    public static <N extends ASTNode> boolean areStructurallyEqual(final N node1, final N node2) {
        return areStructurallyEqual(node1, node2, false);
    }

    public static boolean areStructurallyEqual(final Program node1, final Program node2) {
        return areStructurallyEqual(node1, node2, false);
    }

    /**
     * Checks if two AST nodes are structurally the same.
     *
     * <p>I.e. their tree is the same not regarding metadata information like block IDs.
     *
     * <p>Note: literal values are not part of the structure.
     * E.g. an expression {@code 1 + 2} is structurally the same as {@code 1 + 3},
     * whereas an expression {@code 1 - 2} would be different.
     *
     * @param node1 Some node of the AST.
     * @param node2 Some other node of the AST.
     * @param <N> The type of the nodes.
     * @return True, if the two given nodes are structurally equal.
     */
    public static <N extends ASTNode> boolean areStructurallyEqualIgnoringLiterals(final N node1, final N node2) {
        return areStructurallyEqual(node1, node2, true);
    }

    public static boolean areStructurallyEqualIgnoringLiterals(final Program node1, final Program node2) {
        return areStructurallyEqual(node1, node2, true);
    }

    /**
     * Special case structural equality for programs.
     *
     * <p>We don’t care about other metadata in the project but only the actual sprite definitions.
     *
     * @param program1 Some program.
     * @param program2 Another program.
     * @param ignoreLiterals If literal values should not be considered to be part of the structure.
     * @return True, if the two programs are structurally equal.
     */
    private static boolean areStructurallyEqual(
            final Program program1, final Program program2, final boolean ignoreLiterals
    ) {
        final int spriteCount1 = program1.getActorDefinitionList().getDefinitions().size();
        final int spriteCount2 = program2.getActorDefinitionList().getDefinitions().size();

        if (spriteCount1 != spriteCount2) {
            return false;
        }

        return allHaveStructurallyEqualPartner(
                program1.getActorDefinitionList().getDefinitions(),
                program2.getActorDefinitionList().getDefinitions(),
                (s1, s2) -> areStructurallyEqual(s1, s2, ignoreLiterals)
        );
    }

    /**
     * Special case structural equality for actors.
     *
     * <p>They include {@link de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt SetStmts} to set the
     * current state of variables and sprite attributes. We ignore those as part of the comparison and only check the
     * actual structure of the sprites (ie scripts and procedures).
     *
     * @param actor1 Some sprite.
     * @param actor2 Another sprite.
     * @param ignoreLiterals If literal values should not be considered to be part of the structure.
     * @return True, if the two sprites are structurally equal.
     */
    private static boolean areStructurallyEqual(
            final ActorDefinition actor1, final ActorDefinition actor2, final boolean ignoreLiterals
    ) {
        final boolean scriptCountEqual = actor1.getScripts().getSize() == actor2.getScripts().getSize();
        final boolean procedureCountEqual = actor1.getProcedureDefinitionList().getList().size()
                == actor2.getProcedureDefinitionList().getList().size();

        if (!scriptCountEqual || !procedureCountEqual) {
            return false;
        }


        final boolean scriptsEqual = allHaveStructurallyEqualPartner(
                actor1.getScripts().getScriptList(),
                actor2.getScripts().getScriptList(),
                (sprite1, sprite2) -> areStructurallyEqual(sprite1, sprite2, ignoreLiterals)
        );

        if (!scriptsEqual) {
            return false;
        }

        return allHaveStructurallyEqualPartner(
                actor1.getProcedureDefinitionList().getList(),
                actor2.getProcedureDefinitionList().getList(),
                (sprite1, sprite2) -> areStructurallyEqual(sprite1, sprite2, ignoreLiterals)
        );
    }

    private static <N extends ASTNode> boolean areStructurallyEqual(
            final N node1, final N node2, final boolean ignoreLiterals
    ) {
        if (!node1.getClass().equals(node2.getClass())) {
            return false;
        } else if (node1.getChildren().size() != node2.getChildren().size()) {
            return false;
        } else if (node1 instanceof ASTLeaf leaf1 && node2 instanceof ASTLeaf leaf2) {
            return areStructurallyEqualLeafs(leaf1, leaf2, ignoreLiterals);
        } else if (node1 instanceof ProcedureDefinition p1 && node2 instanceof ProcedureDefinition p2) {
            return areStructurallyEqualProcedures(p1, p2, ignoreLiterals);
        } else {
            // zip safe: We know that the two nodes are of the same type and have the same number of children.
            // Therefore, they also have the same number of children when removing the metadata nodes.
            return Streams
                    .zip(
                            node1.getChildren().stream().filter(Predicate.not(AstNodeUtil::isMetadata)),
                            node2.getChildren().stream().filter(Predicate.not(AstNodeUtil::isMetadata)),
                            (c1, c2) -> areStructurallyEqual(c1, c2, ignoreLiterals)
                    )
                    .allMatch(b -> b);
        }
    }

    private static boolean areStructurallyEqualProcedures(
            final ProcedureDefinition def1, final ProcedureDefinition def2, final boolean ignoreLiterals
    ) {
        /*
         * We only compare parameters and statements, but ignore the procedure name.
         * The ‘name’ is actually just a randomly generated block ID. We would need the program’s procedure definition
         * mapping to check if the name also matches.
         * This simplification results in slightly incorrect results in case literals are to be considered to be part
         * of the structure, since procedures with different names might still be accepted as structurally equal.
         */

        final List<ParameterDefinition> params1 = def1.getParameterDefinitionList().getParameterDefinitions();
        final List<ParameterDefinition> params2 = def2.getParameterDefinitionList().getParameterDefinitions();

        if (params1.size() != params2.size()) {
            return false;
        }

        final boolean equalParameters = Streams
                .zip(params1.stream(), params2.stream(), (p1, p2) -> areStructurallyEqual(p1, p2, ignoreLiterals))
                .allMatch(b -> b);

        if (!equalParameters) {
            return false;
        }

        return areStructurallyEqual(def1.getStmtList(), def2.getStmtList(), ignoreLiterals);
    }

    private static <N extends ASTNode> boolean allHaveStructurallyEqualPartner(
            final Collection<N> nodes1, final Collection<N> nodes2, final BiFunction<N, N, Boolean> comparison
    ) {
        for (final N node1 : nodes1) {
            if (noneStructurallyEqual(node1, nodes2, comparison)) {
                return false;
            }
        }

        for (final N node2 : nodes2) {
            if (noneStructurallyEqual(node2, nodes1, comparison)) {
                return false;
            }
        }

        return true;
    }

    private static <N extends ASTNode> boolean noneStructurallyEqual(
            final N node, final Collection<N> nodes, final BiFunction<N, N, Boolean> comparison
    ) {
        return nodes.stream().noneMatch(otherNode -> comparison.apply(node, otherNode));
    }

    private static <L extends ASTLeaf> boolean areStructurallyEqualLeafs(
            final L leaf1, final L leaf2, final boolean ignoreLiterals
    ) {
        /*
         * Alternative implementation would be
         * ```
         * if (leaf1 instanceof BoolLiteral b1 && leaf2 instanceof BoolLiteral b2) {
         *     return b1.getValue() == b2.getValue();
         * } else if (...) { ... }
         * ```
         * which would save the repeated allocations of new visitor objects, but instead have one long if-elseif-chain
         * that is less readable than the current approach.
         *
         * In the future, switch patterns might make it nicer to represent this:
         * ```
         * record Tuple(AstLeaf a, AstLeaf b) {}
         *
         * return switch (new Tuple(leaf1, leaf2)) {
         *     case Tuple(BoolLiteral a, BoolLiteral b) -> a.getValue == b.getValue();
         *     // ...
         *     default -> leaf1.getClass().equals(leaf2.getClass());
         * };
         * ```
         */

        return LeafEqualityVisitor.areStructurallyEqual(leaf1, leaf2, ignoreLiterals);
    }

    private static class LeafEqualityVisitor implements
            ScratchVisitor, MusicExtensionVisitor, PenExtensionVisitor, TextToSpeechExtensionVisitor,
            TranslateExtensionVisitor {

        private static final double EPSILON = 1e-4;

        private final ASTLeaf compareTo;

        private final boolean ignoreLiterals;

        private boolean areEqual = false;

        private LeafEqualityVisitor(final ASTLeaf compareTo, final boolean ignoreLiterals) {
            this.compareTo = compareTo;
            this.ignoreLiterals = ignoreLiterals;
        }

        static boolean areStructurallyEqual(final ASTLeaf leaf1, final ASTLeaf leaf2, final boolean ignoreLiterals) {
            if (!leaf1.getClass().equals(leaf2.getClass())) {
                return false;
            }

            final LeafEqualityVisitor v = new LeafEqualityVisitor(leaf1, ignoreLiterals);
            leaf2.accept(v);
            return v.areEqual;
        }

        /*
         * Implementation note: we only call this visitor if we already know that the two leafs are of the same class.
         * Therefore, the `if instanceof` are technically not required since they are always true. However, this allows
         * for safe casting of `compareTo` to the target type using the `instanceof` pattern matching.
         */

        @Override
        public void visit(ASTNode node) {
            // for other leafs that do not have any attributes, we compare the class types
            // e.g. any GreenFlag is always structurally the same as another GreenFlag
            areEqual = compareTo.getClass().equals(node.getClass());
        }

        @Override
        public void visit(BoolLiteral node) {
            if (compareTo instanceof BoolLiteral b) {
                areEqual = ignoreLiterals || b.getValue() == node.getValue();
            }
        }

        @Override
        public void visit(StringLiteral node) {
            if (compareTo instanceof StringLiteral s) {
                areEqual = ignoreLiterals || s.getText().equals(node.getText());
            }
        }

        @Override
        public void visit(NumberLiteral node) {
            if (compareTo instanceof NumberLiteral n) {
                if (ignoreLiterals) {
                    areEqual = true;
                    return;
                }

                final boolean bothInfinite = Double.isInfinite(n.getValue()) && Double.isInfinite(node.getValue());
                final boolean bothNaN = Double.isNaN(n.getValue()) && Double.isNaN(node.getValue());

                areEqual = bothInfinite || bothNaN || Math.abs(n.getValue() - node.getValue()) < EPSILON;
            }
        }

        @Override
        public void visit(ColorLiteral node) {
            if (compareTo instanceof ColorLiteral c) {
                areEqual = ignoreLiterals || c.equals(node);
            }
        }

        @Override
        public void visit(ActorType node) {
            if (compareTo instanceof ActorType a) {
                areEqual = a.getType() == node.getType();
            }
        }

        @Override
        public void visit(EventAttribute node) {
            if (compareTo instanceof EventAttribute e) {
                areEqual = e.getType() == node.getType();
            }
        }

        @Override
        public void visit(NumFunct node) {
            if (compareTo instanceof NumFunct n) {
                areEqual = n.getType() == node.getType();
            }
        }

        @Override
        public void visit(NameNum node) {
            if (compareTo instanceof NameNum n) {
                areEqual = n.getType() == node.getType();
            }
        }

        @Override
        public void visit(FixedAttribute node) {
            if (compareTo instanceof FixedAttribute f) {
                areEqual = f.getType() == node.getType();
            }
        }

        @Override
        public void visit(GraphicEffect node) {
            if (compareTo instanceof GraphicEffect n) {
                areEqual = n.getType() == node.getType();
            }
        }

        @Override
        public void visit(SoundEffect node) {
            if (compareTo instanceof SoundEffect s) {
                areEqual = s.getType() == node.getType();
            }
        }

        @Override
        public void visit(ForwardBackwardChoice node) {
            if (compareTo instanceof ForwardBackwardChoice f) {
                areEqual = f.getType() == node.getType();
            }
        }

        @Override
        public void visit(LayerChoice node) {
            if (compareTo instanceof LayerChoice l) {
                areEqual = l.getType() == node.getType();
            }
        }

        @Override
        public void visit(DragMode node) {
            if (compareTo instanceof DragMode d) {
                areEqual = d.getType() == node.getType();
            }
        }

        @Override
        public void visit(RotationStyle node) {
            if (compareTo instanceof RotationStyle r) {
                areEqual = r.getType() == node.getType();
            }
        }

        @Override
        public void visit(TimeComp node) {
            if (compareTo instanceof TimeComp t) {
                areEqual = t.getType() == node.getType();
            }
        }

        @Override
        public void visit(Type node) {
            if (compareTo instanceof Type t) {
                areEqual = t.getClass().equals(node.getClass());
            }
        }

        @Override
        public void visit(TerminationStmt node) {
            if (compareTo instanceof TerminationStmt t) {
                areEqual = t.getClass().equals(node.getClass());
            }
        }

        @Override
        public void visit(FixedNote node) {
            if (compareTo instanceof FixedNote f) {
                areEqual = Math.abs(f.getNote() - node.getNote()) < EPSILON;
            }
        }

        @Override
        public void visit(FixedInstrument node) {
            if (compareTo instanceof FixedInstrument f) {
                areEqual = f.getType() == node.getType();
            }
        }

        @Override
        public void visit(FixedDrum node) {
            if (compareTo instanceof FixedDrum f) {
                areEqual = f.getType() == node.getType();
            }
        }
    }
}
