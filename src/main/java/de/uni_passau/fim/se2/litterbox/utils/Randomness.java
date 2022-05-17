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
package de.uni_passau.fim.se2.litterbox.utils;

import java.io.Serializable;
import java.util.*;

/**
 * Unique random number accessor.
 *
 * <p>
 * Based on {@code Randomness.java} in EvoSuite.
 * </p>
 */
public final class Randomness implements Serializable {

    private static final long serialVersionUID = -5934455398558935937L;

    private static long seed = 0;

    private static Random random = null;

    private static Randomness instance = new Randomness();

    private Randomness() {
        Optional<Long> optionalSeed = PropertyLoader.getSeed();
        seed = optionalSeed.orElseGet(System::currentTimeMillis);
        random = new Random(seed);
    }

    public static Randomness getInstance() {
        if (instance == null) {
            instance = new Randomness();
        }
        return instance;
    }

    public static boolean nextBoolean() {
        return random.nextBoolean();
    }

    /**
     * Returns a pseudorandom, uniformly distributed int value between 0 (inclusive) and the
     * specified value {@code max} (exclusive).
     *
     * @param max the upper bound
     * @return a random number between 0 and {@code max - 1}
     * @see Random#nextInt(int)
     */
    public static int nextInt(int max) {
        return random.nextInt(max);
    }

    public static double nextGaussian() {
        return random.nextGaussian();
    }

    /**
     * Returns a pseudorandom, uniformly distributed int value between the lower bound {@code min}
     * (inclusive) and the upper bound {@code max} (exclusive).
     *
     * @param min the lower bound
     * @param max the upper bound
     * @return a random number between {@code min} and {@code max}
     */
    public static int nextInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    public static int nextInt() {
        return random.nextInt();
    }

    public static char nextChar() {
        return (char) (nextInt(32, 128));
        //return random.nextChar();
    }

    public static short nextShort() {
        return (short) (random.nextInt(2 * 32767) - 32767);
    }

    public static long nextLong() {
        return random.nextLong();
    }

    public static byte nextByte() {
        return (byte) (random.nextInt(256) - 128);
    }

    public static double nextDouble() {
        return random.nextDouble();
    }

    public static double nextDouble(double min, double max) {
        return min + (random.nextDouble() * (max - min));
    }

    public static float nextFloat() {
        return random.nextFloat();
    }

    public static void setSeed(long seed) {
        Randomness.seed = seed;
        random.setSeed(seed);
    }

    public static long getSeed() {
        return seed;
    }

    public static <T> T choice(List<T> list) {
        if (list.isEmpty()) {
            return null;
        }

        int position = random.nextInt(list.size());
        return list.get(position);
    }

    @SuppressWarnings("unchecked")
    public static <T> T choice(Collection<T> set) {
        if (set.isEmpty()) {
            return null;
        }

        int position = random.nextInt(set.size());
        return (T) set.toArray()[position];
    }

    public static <T> T choice(T... elements) {
        if (elements.length == 0) {
            return null;
        }

        int position = random.nextInt(elements.length);
        return elements[position];
    }

    public static void shuffle(List<?> list) {
        Collections.shuffle(list, random);
    }

    public static String nextString(int length) {
        char[] characters = new char[length];
        for (int i = 0; i < length; i++) {
            characters[i] = nextChar();
        }
        return new String(characters);
    }
}
