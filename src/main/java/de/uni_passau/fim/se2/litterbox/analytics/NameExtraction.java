package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.List;

public interface NameExtraction {

    List<String> extractNames(Program program);

    String getName();
}
