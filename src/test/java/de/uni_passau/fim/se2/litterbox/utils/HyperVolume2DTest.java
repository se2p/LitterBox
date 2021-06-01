package de.uni_passau.fim.se2.litterbox.utils;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Chromosome;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.MaximizingFitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.MinimizingFitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

class HyperVolume2DTest {

    private static final double epsilon = 1e-12;

    @Test
    void testHyperVolume1() {
        final double x1 = 0.5, y1 = 0.02;
        final var c1 = new C(String.format("c1(%f, %f)", x1, y1));
        final double x2 = 0.58, y2 = 0.04;
        final var c2 = new C(String.format("c2(%f, %f)", x2, y2));
        final double x3 = 0.96, y3 = 0.64;
        final var c3 = new C(String.format("c3(%f, %f)", x3, y3));

        final var f1 = new MaximizingFitnessFunction<C>() {
            @Override
            public double getFitness(final C c) throws NullPointerException {
                if (c == c1) return x1;
                else if (c == c2) return x2;
                else if (c == c3) return x3;
                else throw new IllegalArgumentException("Undefined for " + c);
            }
        };

        final double xr = 0.0; // x-coordinate of the reference point

        final var f2 = new MinimizingFitnessFunction<C>() {
            @Override
            public double getFitness(final C c) throws NullPointerException {
                if (c == c1) return y1;
                else if (c == c2) return y2;
                else if (c == c3) return y3;
                else throw new IllegalArgumentException("Undefined for " + c);
            }
        };

        final double yr = 1.0; // y-coordinate of the reference point

        final var front = List.of(c1, c2, c3);

        final var hv = new HyperVolume2D<>(f1, f2, xr, yr);
        final double actual = hv.compute(front);

        final double a1 = Math.abs((xr - x1) * (yr - y1));
        final double a2 = Math.abs((x1 - x2) * (yr - y2));
        final double a3 = Math.abs((x2 - x3) * (yr - y3));
        final double expected = a1 + a2 + a3;

        assertWithMessage("Hyper-volume should have been computed correctly")
                .that(actual)
                .isWithin(epsilon)
                .of(expected);

        final var front2 = new ArrayList<>(front);
        Collections.shuffle(front2);
        final double actual2 = hv.compute(front2);
        assertWithMessage("Front should have been sorted before computing hyper-volume")
                .that(actual2)
                .isWithin(epsilon)
                .of(expected);

        final var hv2 = new HyperVolume2D<>(f2, f1, yr, xr);
        final double actual3 = hv2.compute(front);
        assertWithMessage("Hyper-volume of front should have been invariant w.r.t. rotation")
                .that(actual3)
                .isWithin(epsilon)
                .of(expected);
    }

    @Test
    void testHyperVolume2() {
        final double x1 = 0.1, y1 = 0.9;
        final var c1 = new C(String.format("c1(%f, %f)", x1, y1));
        final double x2 = 0.2, y2 = 0.4;
        final var c2 = new C(String.format("c2(%f, %f)", x2, y2));
        final double x3 = 0.3, y3 = 0.3;
        final var c3 = new C(String.format("c3(%f, %f)", x3, y3));
        final double x4 = 0.5, y4 = 0.2;
        final var c4 = new C(String.format("c4(%f, %f)", x4, y4));
        final double x5 = 0.9, y5 = 0.1;
        final var c5 = new C(String.format("c5(%f, %f)", x5, y5));

        final var f1 = new MinimizingFitnessFunction<C>() {
            @Override
            public double getFitness(final C c) throws NullPointerException {
                if (c == c1) return x1;
                else if (c == c2) return x2;
                else if (c == c3) return x3;
                else if (c == c4) return x4;
                else if (c == c5) return x5;
                else throw new IllegalArgumentException("Undefined for " + c);
            }
        };

        final double xr = 1.0; // x-coordinate of the reference point

        final var f2 = new MinimizingFitnessFunction<C>() {
            @Override
            public double getFitness(final C c) throws NullPointerException {
                if (c == c1) return y1;
                else if (c == c2) return y2;
                else if (c == c3) return y3;
                else if (c == c4) return y4;
                else if (c == c5) return y5;
                else throw new IllegalArgumentException("Undefined for " + c);
            }
        };

        final double yr = 1.0; // y-coordinate of the reference point

        final var front = List.of(c2, c1, c3, c5, c4); // deliberately not sorted by f1 or f2

        final var hv = new HyperVolume2D<>(f1, f2, xr, yr);
        final double actual = hv.compute(front);
        final double expected = 0.09 + 0.4 + 0.07 + 0.05 + 0.01; // = 0.62

        assertWithMessage("Hyper-volume should have been computed correctly")
                .that(actual)
                .isWithin(epsilon)
                .of(expected);

        final var front2 = new ArrayList<>(front);
        front2.addAll(front);

        final double actual2 = hv.compute(front2);
        assertWithMessage("Duplicates in front should have been ignored during HV computation")
                .that(actual2)
                .isWithin(epsilon)
                .of(expected);

        final var hv2 = new HyperVolume2D<>(f2, f1, yr, xr);
        final double actual3 = hv2.compute(front);
        assertWithMessage("Hyper-volume of front should have been invariant w.r.t. rotation")
                .that(actual3)
                .isWithin(epsilon)
                .of(expected);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void testEmptyFront() {
        final var f1 = mock(FitnessFunction.class);
        final var f2 = mock(FitnessFunction.class);
        final var hv = new HyperVolume2D(f1, f2, 1, 0);
        assertThrows(
                "Computing hyper-volume of empty front should have caused NoSuchElementException",
                NoSuchElementException.class,
                () -> hv.compute(Collections.emptyList()));
    }

    @Test
    void testReferencePoint() {
        final double xr = 1.0;
        final double yr = 1.0;
        final var c = new C("reference point");
        final var fx = new MinimizingFitnessFunction<C>() {
            @Override
            public double getFitness(final C o) throws NullPointerException {
                if (o ==  c) {
                    return xr;
                } else {
                    throw new IllegalArgumentException("Undefined for " + o);
                }
            }
        };
        final var fy = new MaximizingFitnessFunction<C>() {
            @Override
            public double getFitness(final C o) throws NullPointerException {
                if (o == c) {
                    return yr;
                } else {
                    throw new IllegalArgumentException("Undefined for " + o);
                }
            }
        };
        final var hv = new HyperVolume2D<>(fx, fy, xr, yr);
        final var front = List.of(c);
        final double actual = hv.compute(front);
        final double expected = 0.0;
        assertWithMessage("Singleton front containing the reference point should have had HV of 0")
                .that(actual)
                .isEqualTo(expected);
    }

    @Test
    void testSingletonFront() {
        final double refX1 = 1.0;
        final double refY1 = 1.0;

        final var p = new C("singleton");
        final double x = 0.25;
        final double y = 0.75;

        final var f1 = new MaximizingFitnessFunction<C>() {
            @Override
            public double getFitness(final C c) throws NullPointerException {
                if (c == p) return x;
                else throw new IllegalArgumentException("Undefined for " + c);
            }
        };

        final var f2 = new MaximizingFitnessFunction<C>() {
            @Override
            public double getFitness(final C c) throws NullPointerException {
                if (c == p) return y;
                else throw new IllegalArgumentException("Undefined for " + c);
            }
        };

        final var hv1 = new HyperVolume2D<>(f1, f2, refX1, refY1);
        final var front1 = List.of(p);

        final double actual1 = hv1.compute(front1);
        final double expected1 = Math.abs((refX1 - x) * (refY1 - y));

        assertWithMessage("Hyper-volume of singleton should have been computed correctly")
                .that(actual1)
                .isEqualTo(expected1);

        final double refX2 = 7.0;
        final double refY2 = 42.0;

        final var hv2 = new HyperVolume2D<>(f1, f2, refX2, refY2);
        final double actual2 = hv2.compute(front1);
        final double expected2 = Math.abs((refX2 - x) * (refY2 - y));

        assertWithMessage("Hyper-volume computation should have taken reference point into account")
                .that(actual2)
                .isEqualTo(expected2);

        final var front3 = new ArrayList<>(front1);
        front3.addAll(front1);  // Duplicates!

        final double actual3 = hv2.compute(front3);

        @SuppressWarnings("UnnecessaryLocalVariable")
        final double expected3 = expected2;

        assertWithMessage("Duplicates in the front should have been handled properly")
                .that(actual3)
                .isEqualTo(expected3);
    }

    @Test
    void testFrontWithTwoElements() {
        final double xr = 0.1;
        final double yr = -0.1;

        final C c1 = new C("c1");
        final double f1c1 = 2.5, f2c1 = 0.5;
        final C c2 = new C("c2");
        final double f1c2 = 1.5, f2c2 = 3.5;
        final var front = List.of(c1, c2);

        final var f1 = new MaximizingFitnessFunction<C>() {
            @Override
            public double getFitness(final C c) throws NullPointerException {
                if (c == c1) return f1c1;
                else if (c == c2) return f1c2;
                else throw new IllegalArgumentException("Undefined for " + c);
            }
        };

        final var f2 = new MaximizingFitnessFunction<C>() {
            @Override
            public double getFitness(final C c) throws NullPointerException {
                if (c == c1) return f2c1;
                else if (c == c2) return f2c2;
                else throw new IllegalArgumentException("Undefined for " + c);
            }
        };

        final var hv = new HyperVolume2D<>(f1, f2, xr, yr);
        final double expected = (f2c2 - f2c1) * (f1c2 - xr) + (f1c1 - xr) * (f2c1 - yr);
        final double actual = hv.compute(front);

        assertWithMessage("Hyper-volume should have been computed correctly")
                .that(actual)
                .isWithin(epsilon)
                .of(expected);
    }

    private static final class C extends Chromosome<C> {
        private final String name;

        public C(final String name) throws NullPointerException {
            super(Mutation.identity(), Crossover.identity());
            this.name = name;
        }

        @Override
        public C copy() {
            return this;
        }

        @Override
        public boolean equals(final Object other) {
            return this == other;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public C self() {
            return this;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
