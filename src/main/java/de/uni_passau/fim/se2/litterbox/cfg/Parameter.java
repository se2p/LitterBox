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

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;

import java.util.Objects;

public class Parameter implements Defineable {

    private final LocalIdentifier identifier;
    private final String scopeId;

    public Parameter(LocalIdentifier identifier, String scopeId) {
        this.identifier = identifier;
        this.scopeId = scopeId;
    }

    public LocalIdentifier getIdentifier() {
        return identifier;
    }

    public String getScopeId() {
        return scopeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Parameter parameter = (Parameter) o;
        return Objects.equals(identifier, parameter.identifier)
                && Objects.equals(scopeId, parameter.scopeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, scopeId);
    }

    @Override
    public String toString() {
        return "Parameter{"
                + "identifier=" + identifier
                + ", scopeId='" + scopeId + '\''
                + '}';
    }
}
