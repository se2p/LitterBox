/**
 * Copyright (C) 2019 LitterBox contributors
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
package scratch.data;

import java.util.Arrays;
import java.util.List;

/**
 * Wrapper for Scratch Scripts
 */
public class Script {

    private double[] position;
    private List<ScBlock> blocks;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nScript{" + "position=").append(Arrays.toString(position)).append(", blocks:");
        for (ScBlock b : blocks) {
            sb.append(b.toString());
        }
        sb.append("\n");
        return sb.toString();
    }

    public double[] getPosition() {
        return position;
    }

    public void setPosition(double[] position) {
        this.position = position;
    }

    public List<ScBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<ScBlock> blocks) {
        this.blocks = blocks;
    }

}
