/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.pqgram;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;

import java.util.ArrayList;
import java.util.List;

public class PQGramProfile {

    public static final String NULL_NODE = "*";

    private int p = 2;

    private int q = 3;

    private final Multiset<LabelTuple> tuples = HashMultiset.create();

    public PQGramProfile(ASTNode node) {
        if (node != null) {
            populatePQGram(node);
        }
    }

    private void populatePQGram(ASTNode node) {
        List<Label> anc = new ArrayList<>();
        for (int i = 0; i < p; i++) {
            anc.add(new Label(NULL_NODE));
        }
        profileStep(node, getBlockName(node), anc);
    }

    private void addLabelTuple(LabelTuple tuple) {
        tuples.add(tuple);
    }

    // TODO: Remove
    public Multiset<LabelTuple> getTuples() {
        return tuples;
    }

    public int size() {
        return tuples.size();
    }

    public double calculateDistanceTo(PQGramProfile other) {
        if (tuples.isEmpty() && other.size() == 0) {
            return 0;
        }
        Multiset<LabelTuple> intersection = Multisets.intersection(tuples, other.getTuples());
        double division = (double) intersection.size() / (size() + other.size());
        return 1 - (2 * division);
    }

    private void profileStep(ASTNode root, String rootLabel, List<Label> anc) {
        List<Label> ancHere = new ArrayList<>(anc);
        shift(ancHere, new Label(rootLabel));
        List<Label> sib = new ArrayList<>();
        for (int i = 0; i < q; i++) {
            sib.add(new Label(NULL_NODE));
        }

        if (!root.hasChildren()) {
            addLabelTuple(new LabelTuple(ancHere, sib));
        } else {
            for (ASTNode child : root.getChildren()) {
                String blockName = getBlockName(child);
                shift(sib, new Label(blockName));
                addLabelTuple(new LabelTuple(ancHere, sib));
                profileStep(child, blockName, ancHere);
            }
            for (int k = 0; k < q - 1; k++) {
                shift(sib, new Label(NULL_NODE));
                addLabelTuple(new LabelTuple(ancHere, sib));
            }
        }
    }

    private void shift(List<Label> register, Label label) {
        register.remove(0);
        register.add(label);
    }

    private String getBlockName(ASTNode node) {
        return node.getClass().getSimpleName();
    }

    public void setP(int p) {
        this.p = p;
    }

    public void setQ(int q) {
        this.q = q;
    }

    public int getP() {
        return p;
    }

    public int getQ() {
        return q;
    }

    @Override
    public String toString() {
        return "PQGramProfile{"
                + "tuples=" + tuples + '}';
    }
}
