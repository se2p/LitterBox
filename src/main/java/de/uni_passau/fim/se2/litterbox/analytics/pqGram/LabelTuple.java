package de.uni_passau.fim.se2.litterbox.analytics.pqGram;

import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LabelTuple {
    private final List<Label> labels;

    public LabelTuple(List<Label> anc, List<Label> sib) {
//        Preconditions.checkArgument(anc.size() == PQGramProfileUtil.getP(),
//                "Too many ancestors for the specified p.");
//        Preconditions.checkArgument(sib.size() == PQGramProfileUtil.getQ(),
//                "Too many siblings for the specified q.");
        labels = new ArrayList<>();
        labels.addAll(anc);
        labels.addAll(sib);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LabelTuple that = (LabelTuple) o;
        return Objects.equals(getLabels(), that.getLabels());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLabels());
    }

    public List<Label> getLabels() {
        return labels;
    }

    @Override
    public String toString() {
        return labels.toString();
    }
}
