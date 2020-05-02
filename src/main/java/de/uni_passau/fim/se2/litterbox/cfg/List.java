package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;

import java.util.Objects;

public class List implements Defineable {

    private Identifier identifier;

    public List(Identifier identifier) {
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        List variable = (List) o;
        return Objects.equals(identifier, variable.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

}
