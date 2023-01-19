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
package de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MusicExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class FixedDrum extends AbstractNode implements Drum, ASTLeaf {
    private final BlockMetadata metadata;
    private final FixedDrum.FixedDrumType type;

    public FixedDrum(String typeName, BlockMetadata metadata) {
        super(metadata);
        this.type = FixedDrum.FixedDrumType.fromString(typeName);
        this.metadata = metadata;
    }

    public FixedDrum.FixedDrumType getType() {
        return type;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(MusicExtensionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    public enum FixedDrumType {

        SNARE_DRUM("1"), BASS_DRUM("2"), SIDE_STICK("3"), CRASH_CYMBAL("4"), OPEN_HI_HAT("5"), CLOSED_HI_HAT("6"),
        TAMBOURINE("7"), HAND_CLAP("8"), CLAVES("9"), WOOD_BLOCK("10"), COWBELL("11"), TRIANGLE("12"),
        BONGO("13"), CONGA("14"), CABASA("15"), GUIRO("16"), VIBRASLAP("17"), CUICA("18");

        private final String type;

        FixedDrumType(String type) {
            this.type = Preconditions.checkNotNull(type);
        }

        public static FixedDrum.FixedDrumType fromString(String type) {
            for (FixedDrum.FixedDrumType f : values()) {
                if (f.getType().equals(type.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown FixedDrum: " + type);
        }

        public String getName() {
            switch (this) {
                case SNARE_DRUM:
                    return "(1) Snare Drum";
                case BASS_DRUM:
                    return "(2) Bass Drum";

                case SIDE_STICK:
                    return "(3) Side Stick";

                case CRASH_CYMBAL:
                    return "(4) Crash Cymbal";

                case OPEN_HI_HAT:
                    return "(5) Open Hi-Hat";

                case CLOSED_HI_HAT:
                    return "(6) Closed Hi-Hat";
                case TAMBOURINE:
                    return "(7) Tambourine";
                case HAND_CLAP:
                    return "(8) Hand Clap";
                case CLAVES:
                    return "(9) Claves";

                case WOOD_BLOCK:
                    return "(10) Wood Block";

                case COWBELL:
                    return "(11) Cowbell";

                case TRIANGLE:
                    return "(12) Triangle";

                case BONGO:
                    return "(13) Bongo";

                case CONGA:
                    return "(14) Conga";

                case CABASA:
                    return "(15) Cabasa";

                case GUIRO:
                    return "(16) Guiro";

                case VIBRASLAP:
                    return "(17) Vibraslap";

                case CUICA:
                    return "(18) Cuica";
                default:
                    throw new IllegalArgumentException("Unknown FixedDrum: " + type);
            }
        }

        public String getType() {
            return type;
        }
    }
}
