/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;

import java.util.Objects;

public class ListVariable implements Defineable {

    private final Identifier identifier;

    public ListVariable(Identifier identifier) {
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ListVariable variable = (ListVariable) o;
        if (identifier instanceof de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified q1
                && variable.identifier instanceof de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified q2) {
            return Objects.equals(q1.getFirst(), q2.getFirst())
                    && Objects.equals(q1.getSecond().getName(), q2.getSecond().getName());
        }
        return Objects.equals(identifier, variable.identifier);
    }

    @Override
    public int hashCode() {
        if (identifier instanceof de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified q) {
            return Objects.hash(q.getFirst(), q.getSecond().getName());
        }
        return Objects.hash(identifier);
    }
}
