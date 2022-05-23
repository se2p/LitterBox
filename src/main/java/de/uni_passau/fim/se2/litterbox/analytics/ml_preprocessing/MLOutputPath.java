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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing;

import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.nio.file.Path;

/**
 * Machine learning preprocessors can either print their result to the console or write them to a file.
 */
public class MLOutputPath {
    private final MLOutputPathType pathType;
    private final Path path;

    private MLOutputPath(MLOutputPathType pathType, Path path) {
        this.pathType = pathType;
        this.path = path;
    }

    public static MLOutputPath console() {
        return new MLOutputPath(MLOutputPathType.CONSOLE, null);
    }

    public static MLOutputPath directory(Path dir) {
        Preconditions.checkArgument(!dir.toFile().exists() || dir.toFile().isDirectory(),
                "The output path for a machine learning preprocessor must be a directory!");
        return new MLOutputPath(MLOutputPathType.PATH, dir);
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        if (MLOutputPathType.CONSOLE.equals(pathType)) {
            return "CONSOLE";
        } else {
            return path.toString();
        }
    }

    public boolean isConsoleOutput() {
        return MLOutputPathType.CONSOLE.equals(pathType);
    }

    private enum MLOutputPathType {
        CONSOLE,
        PATH
    }
}
