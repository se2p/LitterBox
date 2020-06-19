/*
 * Copyright (C) 2020 LitterBox contributors
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

import java.util.List;

public class Preconditions {

    public static <T> T checkNotNull(T o) {
        if (o == null) {
            throw new NullPointerException("Variable must not be null");
        }
        return o;
    }

    public static void checkArgument(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException("Invalid argument!");
        }
    }

    public static void checkArgument(boolean condition, String message, Object... args) {
        if (!condition) {
            throw new IllegalArgumentException(String.format(message, args));
        }
    }

    public static <T> List<T> checkAllArgsNotNull(List<T> args) {
        int i = 0;
        for (Object o : args) {
            if (o == null) {
                throw new NullPointerException(String.format("Argument %d must not be null", i));
            }
            i++;
        }
        return args;
    }

    public static <T> T[] checkAllArgsNotNull(T[] args) {
        int i = 0;
        for (Object o : args) {
            if (o == null) {
                throw new NullPointerException(String.format("Argument %d must not be null", i));
            }
            i++;
        }
        return args;
    }

    public static void checkState(boolean condition, String msg, Object... args) {
        if (!condition) {
            throw new IllegalStateException(String.format(msg, args));
        }
    }
}
