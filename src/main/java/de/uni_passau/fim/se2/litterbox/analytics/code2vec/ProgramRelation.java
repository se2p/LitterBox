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
package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import java.util.function.Function;

public class ProgramRelation {
    private final String mSource;
    private final String mTarget;
    private final String mHashedPath;
    private String mPath;
    public static Function<String, String> sHasher = s -> Integer.toString(s.hashCode());

    public ProgramRelation(String sourceName, String targetName, String path) {
        mSource = sourceName;
        mTarget = targetName;
        mPath = path;
        mHashedPath = sHasher.apply(path);
    }

    public static void setNoHash() {
        sHasher = Function.identity();
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s", mSource, mHashedPath,
                mTarget);
    }
}
