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
package de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.FixedNodeOption;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MusicExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class FixedInstrument extends AbstractNode implements Instrument, FixedNodeOption {
    private final BlockMetadata metadata;
    private final FixedInstrumentType type;

    public FixedInstrument(String typeName, BlockMetadata metadata) {
        super(metadata);
        this.type = FixedInstrumentType.fromString(typeName);
        this.metadata = metadata;
    }

    public FixedInstrumentType getType() {
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

    @Override
    public String getTypeName() {
        return type.getName();
    }

    public enum FixedInstrumentType {

        PIANO("1"), ELECTRIC_PIANO("2"), ORGAN("3"), GUITAR("4"), ELECTRIC_GUITAR("5"), BASS("6"),
        PIZZICATO("7"), CELLO("8"), TROMBONE("9"), CLARINET("10"), SAXOPHONE("11"), FLUTE("12"),
        WOODEN_FLUTE("13"), BASSOON("14"), CHOIR("15"), VIBRAPHONE("16"), MUSIC_BOX("17"), STEEL_DRUM("18"),
        MARIMBA("19"), SYNTH_LEAD("20"), SYNTH_PAD("21");

        private final String type;

        FixedInstrumentType(String type) {
            this.type = Preconditions.checkNotNull(type);
        }

        public static FixedInstrumentType fromString(String type) {
            for (FixedInstrumentType f : values()) {
                if (f.getType().equals(type.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown FixedInstrument: " + type);
        }

        public String getName(){
            switch (this) {

                case PIANO:
                    return"(1) Piano";

                case ELECTRIC_PIANO:
                    return"(2) Electric Piano";

                case ORGAN:
                    return"(3) Organ";

                case GUITAR:
                    return"(4) Guitar";

                case ELECTRIC_GUITAR:
                    return"(5) Electric Guitar";

                case BASS:
                    return"(6) Bass";

                case PIZZICATO:
                    return"(7) Pizzicato";

                case CELLO:
                    return"(8) Cello";

                case TROMBONE:
                    return"(9) Trombone";

                case CLARINET:
                    return"(10) Clarinet";

                case SAXOPHONE:
                    return"(11) Saxophone";

                case FLUTE:
                    return"(12) Flute";

                case WOODEN_FLUTE:
                    return"(13) Wooden Flute";

                case BASSOON:
                    return"(14) Bassoon";

                case CHOIR:
                    return"(15) Choir";

                case VIBRAPHONE:
                    return"(16) Vibraphone";

                case MUSIC_BOX:
                    return"(17) Music Box";

                case STEEL_DRUM:
                    return"(18) Steel Drum";

                case MARIMBA:
                    return"(19) Marimba";

                case SYNTH_LEAD:
                    return"(20) Synth Lead";

                case SYNTH_PAD:
                    return"(21) Synth Pad";
                default:
                    throw new IllegalArgumentException("Unknown FixedInstrument: " + type);
            }
        }

        public String getType() {
            return type;
        }
    }
}
