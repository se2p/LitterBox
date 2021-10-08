package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ProgramFeatures {
    private String name;

    private ArrayList<ProgramRelation> features = new ArrayList<>();

    public ProgramFeatures(String name) {
        this.name = name;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name).append(" ");
        stringBuilder.append(features.stream().map(ProgramRelation::toString).collect(Collectors.joining(" ")));

        return stringBuilder.toString();
    }

    public void addFeature(String source, String path, String target) {
        ProgramRelation newRelation = new ProgramRelation(source, target, path);
        features.add(newRelation);
    }

    public boolean isEmpty() {
        return features.isEmpty();
    }

    public String getName() {
        return name;
    }

    public ArrayList<ProgramRelation> getFeatures() {
        return features;
    }
}
