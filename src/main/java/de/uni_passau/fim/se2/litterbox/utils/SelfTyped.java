/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

/**
 * An interface for classes that implement the so called Curiously Recurring Template pattern (also
 * known as F-bounded polymorphism). This pattern is useful to make up for the lack of self types in
 * Java while still maintaining type-safety, albeit at the expense of clean and short class
 * signatures. (The alternative is to resort to unchecked type casts or unbounded wildcards {@code
 * <?>} where a self type would be more appropriate.) Self types are commonly used in abstract super
 * classes or interfaces to refer to the subtype of {@code this}. (A prominent example for the use
 * of self-types in the JDK is the abstract {@link Enum} class.) It is imperative to follow the
 * general contract of {@code self()} as laid out below.
 *
 * @param <S> the implementing {@code SelfTyped} class
 */
public interface SelfTyped<S extends SelfTyped<S>> {

    /**
     * <p>
     * Returns the runtime type of the implementor (a.k.a. "self-type"). This method must only be
     * implemented in concrete, non-abstract subclasses by returning a reference to {@code this},
     * and nothing else. Returning a reference to any other runtime type other than {@code this}
     * breaks the contract.
     * </p>
     * <p>
     * In other words, every concrete subclass {@code Foo} that implements the interface {@code
     * SelfTyped} must implement this method as follows:
     * <pre>{@code
     * public final class Foo implements SelfTyped<Foo> {
     *     @Override
     *     public Foo self() {
     *         return this;
     *     }
     * }
     * }</pre>
     * </p>
     *
     * @return a reference to the self-type
     */
    S self();
}
