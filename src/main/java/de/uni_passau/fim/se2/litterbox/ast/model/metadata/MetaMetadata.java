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
package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class MetaMetadata extends AbstractNode implements Metadata, ASTLeaf {
    private String semver;
    private String vm;
    private String agent;

    /**
     * The constructor needs the semver, vm and agent attributes that are stated in the meta node of the json.
     *
     * @param semver semver of the project
     * @param vm     version of the scratch virtual machine
     * @param agent  information about the browser that was used for creating the project
     */
    public MetaMetadata(String semver, String vm, String agent) {
        super();
        this.semver = semver;
        this.vm = vm;
        this.agent = agent;
    }

    public String getSemver() {
        return semver;
    }

    public String getVm() {
        return vm;
    }

    public String getAgent() {
        return agent;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }
}
