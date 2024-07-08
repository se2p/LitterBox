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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

public abstract class AbstractIssueFinder implements IssueFinder, ScratchVisitor {

    protected ActorDefinition currentActor;
    protected Script currentScript;
    protected ProcedureDefinition currentProcedure;
    protected Set<Issue> issues = new LinkedHashSet<>();
    protected Map<LocalIdentifier, ProcedureInfo> procMap;
    protected Program program;
    protected boolean ignoreLooseBlocks = false;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        program.accept(this);
        return Collections.unmodifiableSet(issues);
    }

    @Override
    public void visit(ActorDefinition actor) {
        Preconditions.checkNotNull(program);
        currentActor = actor;
        procMap = program.getProcedureMapping().getProceduresForActor(actor.getIdent().getName());
        visitChildren(actor);
    }

    @Override
    public void visit(Script script) {
        if (ignoreLooseBlocks && script.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        currentScript = script;
        currentProcedure = null;
        visitChildren(script);
    }

    @Override
    public void visit(ProcedureDefinition procedure) {
        currentProcedure = procedure;
        currentScript = null;
        visitChildren(procedure);
    }

    protected IssueBuilder prepareIssueBuilder() {
        return new IssueBuilder().withFinder(this).withProgram(program).withActor(currentActor).withScriptOrProcedure(getCurrentScriptEntity());
    }

    protected IssueBuilder prepareIssueBuilder(ASTNode node) {
        return prepareIssueBuilder().withCurrentNode(node).withMetadata(node.getMetadata());
    }

    protected void addIssue(IssueBuilder issueBuilder) {
        issues.add(issueBuilder.build());
    }

    protected void addIssue(ASTNode node, Metadata metadata, IssueSeverity severity) {
        addIssue(node, metadata, severity, new Hint(getName()));
    }

    protected void addIssue(ASTNode node, Metadata metadata) {
        addIssue(node, metadata, IssueSeverity.HIGH, new Hint(getName()));
    }

    protected void addIssue(ASTNode node, Metadata metadata, Hint hint) {
        addIssue(node, metadata, IssueSeverity.HIGH, hint);
    }

    protected void addIssue(Issue issue) {
        issues.add(issue);
    }

    protected void addIssue(ASTNode node, IssueSeverity severity) {
        addIssue(node, node.getMetadata(), severity, new Hint(getName()));
    }

    protected void addIssue(ASTNode node, IssueSeverity severity, Hint hint) {
        addIssue(node, node.getMetadata(), severity, hint);
    }

    protected void addIssue(ASTNode node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH, new Hint(getName()));
    }

    protected void addIssue(ASTNode node, Metadata metadata, IssueSeverity severity, Hint hint) {
        if (currentScript != null) {
            issues.add(new Issue(this, severity, program, currentActor, currentScript, node, metadata, hint));
        } else {
            assert (currentProcedure != null);
            issues.add(new Issue(this, severity, program, currentActor, currentProcedure, node, metadata, hint));
        }
    }

    protected void addIssueForSynthesizedScript(Script theScript, ASTNode node, Metadata metadata, Hint hint) {
        issues.add(new Issue(this, IssueSeverity.HIGH, program, currentActor, theScript, node, metadata, hint));
    }

    protected void addIssueWithLooseComment() {
        issues.add(new Issue(this, IssueSeverity.HIGH, program, currentActor,
                null, // TODO: There is no script
                null, // TODO: There is no node?
                null,  // TODO: There is no metadata
                new Hint(getName())));
    }

    protected void addIssueWithLooseComment(Hint hint) {
        issues.add(new Issue(this, IssueSeverity.HIGH, program, currentActor,
                null, // TODO: There is no script
                null, // TODO: There is no node?
                null,  // TODO: There is no metadata
                hint));
    }

    @Override
    public void setIgnoreLooseBlocks(boolean value) {
        ignoreLooseBlocks = value;
    }

    // TODO: Clean this up
    public String getDefineableName(Defineable def) {
        StringBuilder builder = new StringBuilder();

        if (def instanceof Variable variable) {
            builder.append("[var]");
            builder.append(IssueTranslator.getInstance().getInfo(IssueTranslator.GeneralTerm.VARIABLE));
            builder.append(" \"");
            if (variable.getIdentifier() instanceof LocalIdentifier localIdentifier) {
                builder.append(localIdentifier.getName());
            } else {
                builder.append(((Qualified) variable.getIdentifier()).getSecond().getName().getName());
            }
            builder.append("\"");
            builder.append("[/var]");
        } else if (def instanceof ListVariable variable) {
            builder.append("[list]");
            builder.append(IssueTranslator.getInstance().getInfo(IssueTranslator.GeneralTerm.LIST));
            builder.append(" \"");
            if (variable.getIdentifier() instanceof LocalIdentifier localIdentifier) {
                builder.append(localIdentifier.getName());
            } else {
                builder.append(((Qualified) variable.getIdentifier()).getSecond().getName().getName());
            }
            builder.append("\"");
            builder.append("[/list]");
        } else if (def instanceof Attribute attr) {
            builder.append(IssueTranslator.getInstance().getInfo(IssueTranslator.GeneralTerm.ATTRIBUTE));
            builder.append(" \"");
            IssueTranslator.GeneralTerm attributeType = getTermFromAttributeType(attr.getAttributeType());
            builder.append(IssueTranslator.getInstance().getInfo(attributeType));
            builder.append("\"");
        } else if (def instanceof RobotAttribute attr) {
            builder.append(IssueTranslator.getInstance().getInfo(IssueTranslator.GeneralTerm.ATTRIBUTE));
            builder.append(" \"");
            IssueTranslator.GeneralTerm attributeType = getTermFromAttributeType(attr.getAttributeType());
            builder.append(IssueTranslator.getInstance().getInfo(attributeType));
            builder.append("\"");
        }

        return builder.toString();
    }

    private IssueTranslator.GeneralTerm getTermFromAttributeType(final Attribute.AttributeType attributeType) {
        return switch (attributeType) {
            case POSITION -> IssueTranslator.GeneralTerm.POSITION;
            case ROTATION -> IssueTranslator.GeneralTerm.ROTATION;
            case SIZE -> IssueTranslator.GeneralTerm.SIZE;
            case VISIBILITY -> IssueTranslator.GeneralTerm.VISIBILITY;
            case GRAPHIC_EFFECT -> IssueTranslator.GeneralTerm.GRAPHIC_EFFECT;
            case SOUND_EFFECT -> IssueTranslator.GeneralTerm.SOUND_EFFECT;
            case VOLUME -> IssueTranslator.GeneralTerm.VOLUME;
            case LAYER -> IssueTranslator.GeneralTerm.LAYER;
            case COSTUME -> IssueTranslator.GeneralTerm.COSTUME;
            case BACKDROP -> IssueTranslator.GeneralTerm.BACKDROP;
            case TIMER -> IssueTranslator.GeneralTerm.TIMER;
        };
    }

    private IssueTranslator.GeneralTerm getTermFromAttributeType(final RobotAttribute.AttributeType attributeType) {
        return switch (attributeType) {
            case LED -> IssueTranslator.GeneralTerm.LED;
            case ROCKY_LIGHT -> IssueTranslator.GeneralTerm.ROCKY_LIGHT;
            case MOTOR_POWER -> IssueTranslator.GeneralTerm.MOTOR_POWER;
            case MATRIX -> IssueTranslator.GeneralTerm.MATRIX;
        };
    }

    @Override
    public Collection<String> getHintKeys() {
        // Default: Only one key with the name of the finder
        return Arrays.asList(getName());
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            // Don't check against self
            return false;
        }

        if (first.getFinder() != other.getFinder()) {
            // Can only be a duplicate if it's the same finder
            return false;
        }

        if ((first.getScriptOrProcedureDefinition() == null) || (other.getScriptOrProcedureDefinition() == null)) {
            // Need to refer to same script
            return false;
        }

        if (!first.getScriptOrProcedureDefinition().equals(other.getScriptOrProcedureDefinition())) {
            // Need to refer to same script
            return false;
        }

        return first.getCodeLocation().equals(other.getCodeLocation());
    }

    @Override
    public int getDistanceTo(Issue first, Issue other) {

        //if two issues are duplicates of one another, they can be considered the same
        if (!first.isDuplicateOf(other)) {
            if (first.getCodeLocation() != null && other.getCodeLocation() != null) {
                ASTNode firstNormalizedLocation = first.getNormalizedCodeLocation();
                ASTNode secondNormalizedLocation = other.getNormalizedCodeLocation();

                //if a different script or procedure has the issue, distance is increased
                if (first.getScriptOrProcedureDefinition() != other.getScriptOrProcedureDefinition()) {

                    ASTNode firstNormalizedScriptProcedure = first.getNormalizedScriptOrProcedureDefinition();
                    ASTNode secondNormalizedScriptProcedure = other.getNormalizedScriptOrProcedureDefinition();
                    if (first.getScriptOrProcedureDefinition().equals(other.getScriptOrProcedureDefinition())) {
                        if (first.getCodeLocation().equals(other.getCodeLocation())) {
                            //scripts are equal and location is equal
                            return 3;
                        } else if (firstNormalizedLocation.equals(secondNormalizedLocation)) {
                            //scripts are equal and normalised location is equal
                            return 4;
                        } else {
                            //scripts are equal and location is different
                            return 5;
                        }
                    } else if (firstNormalizedScriptProcedure.equals(secondNormalizedScriptProcedure)) {
                        if (first.getCodeLocation().equals(other.getCodeLocation())) {
                            //scripts are normalised equal and location is equal
                            return 6;
                        } else if (firstNormalizedLocation.equals(secondNormalizedLocation)) {
                            //scripts are normalised equal and normalised location is equal
                            return 7;
                        } else {
                            //scripts are normalised equal and location is different
                            return 8;
                        }
                    } else {
                        if (first.getCodeLocation().equals(other.getCodeLocation())) {
                            //scripts are different and location is equal
                            return 9;
                        } else if (firstNormalizedLocation.equals(secondNormalizedLocation)) {
                            //scripts are different and normalised location is equal
                            return 10;
                        } else {
                            //scripts are different and location is different
                            return 11;
                        }
                    }
                } else {
                    if (firstNormalizedLocation.equals(secondNormalizedLocation)) {
                        //same script but code location is normalised the same
                        return 1;
                    } else {
                        //same script but code location is normalised not the same
                        return 2;
                    }
                }
            } else {
                //Issues don't have location so distance has to be very high
                return 12;
            }
        }
        //the issues are duplicates and can be considered the same
        return 0;
    }

    @Override
    public boolean isSubsumedBy(Issue first, Issue other) {
        return false;
    }

    @Override
    public boolean areCoupled(Issue first, Issue other) {
        return false;
    }

    protected ScriptEntity getCurrentScriptEntity() {
        if (currentScript != null) {
            return currentScript;
        } else {
            return currentProcedure;
        }
    }
}
