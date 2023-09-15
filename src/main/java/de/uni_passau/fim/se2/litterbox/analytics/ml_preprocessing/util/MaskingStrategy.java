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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util;

public class MaskingStrategy {
    private final MaskingType maskingType;
    private final String blockId;

    public MaskingStrategy(MaskingType maskingType, String blockId) {
        this.maskingType = maskingType;
        this.blockId = blockId;
    }

    public MaskingType getMaskingType() {
        return maskingType;
    }

    public String getBlockId() {
        return blockId;
    }
}
