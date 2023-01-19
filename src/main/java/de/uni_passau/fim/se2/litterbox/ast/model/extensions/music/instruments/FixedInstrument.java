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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MusicExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class FixedInstrument extends AbstractNode implements Instrument, ASTLeaf {
    private final BlockMetadata metadata;
    private FixedInstrumentType type;

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

    public enum FixedInstrumentType {

        ONE("1"), TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"),
        SEVEN("7"), EIGHT("8"), NINE("9"), TEN("10"), ELEVEN("11"), TWELVE("12"),
        THIRTEEN("13"), FOURTEEN("14"), FIFTEEN("15"), SIXTEEN("16"), SEVENTEEN("17"), EIGHTEEN("18"),
        NINETEEN("19"), TWENTY("20"), TWENTYONE("21");

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

                case ONE:
                    return"(1) Piano";

                case TWO:
                    return"(2) Electric Piano";

                case THREE:
                    return"(3) Organ";

                case FOUR:
                    return"(4) Guitar";

                case FIVE:
                    return"(5) Electric Guitar";

                case SIX:
                    return"(6) Bass";

                case SEVEN:
                    return"(7) Pizzicato";

                case EIGHT:
                    return"(8) Cello";

                case NINE:
                    return"(9) Trombone";

                case TEN:
                    return"(10) Clarinet";

                case ELEVEN:
                    return"(11) Saxophone";

                case TWELVE:
                    return"(12) Flute";

                case THIRTEEN:
                    return"(13) Wooden Flute";

                case FOURTEEN:
                    return"(14) Bassoon";

                case FIFTEEN:
                    return"(15) Choir";

                case SIXTEEN:
                    return"(16) Vibraphone";

                case SEVENTEEN:
                    return"(17) Music Box";

                case EIGHTEEN:
                    return"(18) Steel Drum";

                case NINETEEN:
                    return"(19) Marimba";

                case TWENTY:
                    return"(20) Synth Lead";

                case TWENTYONE:
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
