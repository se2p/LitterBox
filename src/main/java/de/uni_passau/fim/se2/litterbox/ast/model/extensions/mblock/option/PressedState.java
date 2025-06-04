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
package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Objects;

public class PressedState extends AbstractNode implements MBlockOption {

    private final EventPressState pressed;

    public PressedState(String pressed) {
        this.pressed = EventPressState.fromString(pressed);
    }

    public EventPressState getPressed() {
        return pressed;
    }

    public String getPressedState() {
        return pressed.getState();
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(MBlockVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getUniqueName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public BlockMetadata getMetadata() {
        return new NoBlockMetadata();
    }

    @Override
    public String[] toSimpleStringArray() {
        String[] result = new String[1];
        result[0] = pressed.getState();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PressedState that)) return false;
        return pressed == that.pressed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pressed);
    }

    public enum EventPressState {
        IS_PRESSED_TRUE("0"),
        IS_PRESSED_FALSE("1"),
        ;

        private final String state;

        EventPressState(String state) {
            this.state = Preconditions.checkNotNull(state);
        }

        public static EventPressState fromString(String state) {
            for (EventPressState f : values()) {
                if (f.getState().equals(state.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown PressedState: " + state);
        }

        public String getState() {
            return state;
        }
    }
}
