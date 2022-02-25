package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {
    public static String normalizeName(String original) {
        original = original.toLowerCase().replaceAll("\\\\n", "") // escaped new lines
                .replaceAll("//s+", "") // whitespaces
                .replaceAll("[\"',]", "") // quotes, apostrophies, commas
                .replaceAll("\\P{Print}", ""); // unicode weird characters
        String stripped = original.replaceAll("[^A-Za-z]", "");
        if (stripped.length() == 0) {
            return "";
        } else {
            return stripped;
        }
    }

    public static List<String> splitToSubtokens(String str1) {
        String str2 = str1.trim();
        return Stream.of(str2.split("(?<=[a-z])(?=[A-Z])|_|-|[0-9]|(?<=[A-Z])(?=[A-Z][a-z])|\\s+"))
                .filter(s -> s.length() > 0).map(StringUtil::normalizeName)
                .filter(s -> s.length() > 0).collect(Collectors.toCollection(ArrayList::new));
    }
}
