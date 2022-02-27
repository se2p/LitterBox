package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import java.util.List;
import java.util.function.Function;

public class ProgramRelation {
    private String m_Source;
    private String m_Target;
    private String m_HashedPath;
    private String m_Path;
    public static Function<String, String> s_Hasher = s -> Integer.toString(s.hashCode());

    public ProgramRelation(String sourceName, String targetName, String path) {
        m_Source = sourceName;
        m_Target = targetName;
        m_Path = path;
        m_HashedPath = s_Hasher.apply(path);
    }

    public static void setNoHash() {
        s_Hasher = (s) -> s;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s", m_Source, m_HashedPath,
                m_Target);
    }
}
