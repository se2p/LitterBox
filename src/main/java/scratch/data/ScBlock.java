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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for Scratch Blocks
 */
public class ScBlock {

    private String content;
    private String id;
    private Map<String, List<String>> inputs = new HashMap<>();
    private Map<String, List<String>> fields = new HashMap<>();
    private String createdClone;
    private String condition;
    private String procode;
    private List<ScBlock> nestedBlocks;
    private List<ScBlock> elseBlocks;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n    [ScBlock{" + "content='").append(content).append('\'').append("}");
        sb.append("\n Condition: ").append(condition);
//        for (String s : inputs.keySet()) {
//            sb.append("\nInputs: ").append(s).append(": ").append(inputs.get(s));
//        }
        if (nestedBlocks != null) {
            sb.append("\nnestedBlocks=");
            for (ScBlock b : getNestedBlocks()) {
                sb.append(b);
            }
        }
        if (elseBlocks != null) {
            sb.append("\nelseBlocks=");
            for (ScBlock b : getElseBlocks()) {
                sb.append(b);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public String getProcode() {
        return procode;
    }

    public void setProcode(String procode) {
        this.procode = procode;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getCreatedClone() {
        return createdClone;
    }

    public void setCreatedClone(String createdClone) {
        this.createdClone = createdClone;
    }

    public Map<String, List<String>> getFields() {
        return fields;
    }

    public void setFields(Map<String, List<String>> fields) {
        this.fields = fields;
    }

    public Map<String, List<String>> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, List<String>> inputs) {
        this.inputs = inputs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ScBlock> getNestedBlocks() {
        return nestedBlocks;
    }

    public void setNestedBlocks(List<ScBlock> nestedBlocks) {
        this.nestedBlocks = nestedBlocks;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<ScBlock> getElseBlocks() {
        return elseBlocks;
    }

    public void setElseBlocks(List<ScBlock> elseBlocks) {
        this.elseBlocks = elseBlocks;
    }
}
