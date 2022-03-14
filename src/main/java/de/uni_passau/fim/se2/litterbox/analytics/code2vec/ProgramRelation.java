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
