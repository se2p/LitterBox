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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

/**
 * Checks if all Sprites have a starting point.
 */
public class EmptyScript extends AbstractIssueFinder {

    public static final String NAME = "empty_script";

    @Override
    public void visit(ProcedureDefinition node) {
        //NOP should not be detected in Procedures
    }

    @Override
    public void visit(Script node) {
        currentScript = node;
        currentProcedure = null;
        if (!(node.getEvent() instanceof Never) && node.getStmtList().getStmts().isEmpty()) {
            node.getEvent().accept(this);
        }
    }

    private void createHint(ASTNode node, String eventName) {
        Hint hint = new Hint(getName());
        hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo(eventName));
        addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
    }

    @Override
    public void visit(GreenFlag node) {
        createHint(node, "greenflag");
    }

    @Override
    public void visit(KeyPressed node) {
        createHint(node, "keypressed");
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        createHint(node, "backdropswitchto");
    }

    @Override
    public void visit(AttributeAboveValue node) {
        createHint(node, "eventattribute");
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        createHint(node, "receptionofmessage");
    }

    @Override
    public void visit(SpriteClicked node) {
        createHint(node, "spriteclicked");
    }

    @Override
    public void visit(StageClicked node) {
        createHint(node, "stageclicked");
    }

    @Override
    public void visit(StartedAsClone node) {
        createHint(node, "startedasclone");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }
}

