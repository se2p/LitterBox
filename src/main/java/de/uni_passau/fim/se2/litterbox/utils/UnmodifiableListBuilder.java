/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UnmodifiableListBuilder<E> {

    private List<E> list = new ArrayList<>();

    public static <E> UnmodifiableListBuilder<E> builder() {
        return new UnmodifiableListBuilder<>();
    }

    public UnmodifiableListBuilder<E> add(E e) {
        list.add(e);
        return this;
    }

    public UnmodifiableListBuilder<E> addAll(Collection<? extends E> collection) {
        list.addAll(collection);
        return this;
    }

    public List<E> build() {
        return Collections.unmodifiableList(list);
    }
}
