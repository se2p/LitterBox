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

    public String pathAsString() {
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
        PATH;
    }
}
