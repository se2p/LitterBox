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
package de.uni_passau.fim.se2.litterbox.ast.new_parser;

import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.CommentMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.StageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.*;
import de.uni_passau.fim.se2.litterbox.ast.model.type.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class RawTargetConverter {
    private final ProgramParserState state;

    RawTargetConverter(final ProgramParserState state) {
        this.state = state;
    }

    ActorDefinition convertTarget(final RawTarget target) {
        final LocalIdentifier ident = new StrId(target.name());
        state.setCurrentActor(ident);

        final ActorType actorType;
        if (target.isStage()) {
            actorType = ActorType.getStage();
        } else {
            actorType = ActorType.getSprite();
        }

        final ActorMetadata metadata = convertActorMetadata(target);
        final DeclarationStmtList decls = convertDecls(target);
        final SetStmtList setStmtList = convertSetStmtList(target);
        final ProcedureDefinitionList procedureDefinitionList = convertProcDefs(target);
        final ScriptList scripts = convertScripts(target);

        return new ActorDefinition(actorType, ident, decls, setStmtList, procedureDefinitionList, scripts, metadata);
    }

    private ActorMetadata convertActorMetadata(final RawTarget target) {
        final CommentMetadataList commentsMetadataList = convertComments(target.comments());
        final ImageMetadataList costumes = convertCostumes(target.costumes());
        final SoundMetadataList sounds = convertSounds(target.sounds());

        if (target.isStage()) {
            final String textToSpeechLanguage = target.stageAttributes().textToSpeechLanguage();
            return new StageMetadata(
                    commentsMetadataList, target.currentCostume(), costumes, sounds, textToSpeechLanguage
            );
        } else {
            return new ActorMetadata(commentsMetadataList, target.currentCostume(), costumes, sounds);
        }
    }

    private CommentMetadataList convertComments(final Map<RawBlockId, RawComment> comments) {
        final List<CommentMetadata> commentMetadata = comments.entrySet().stream()
                .map(entry -> {
                    final RawComment comment = entry.getValue();
                    return new CommentMetadata(
                            entry.getKey().id(),
                            comment.blockId().map(RawBlockId::id).orElse(null),
                            comment.x(),
                            comment.y(),
                            comment.width(),
                            comment.height(),
                            comment.minimized(),
                            comment.text()
                    );
                })
                .toList();
        return new CommentMetadataList(commentMetadata);
    }

    private ImageMetadataList convertCostumes(final List<RawCostume> costumes) {
        final List<ImageMetadata> images = costumes.stream()
                .map(costume -> new ImageMetadata(
                        costume.assetId(),
                        costume.name(),
                        costume.md5ext(),
                        costume.dataFormat(),
                        costume.bitmapResolution(),
                        costume.rotationCenterX(),
                        costume.rotationCenterY()
                ))
                .toList();
        return new ImageMetadataList(images);
    }

    private SoundMetadataList convertSounds(final List<RawSound> sounds) {
        final List<SoundMetadata> soundMetadata = sounds.stream()
                .map(sound -> new SoundMetadata(
                        sound.assetId(),
                        sound.name(),
                        sound.md5ext(),
                        sound.dataFormat(),
                        sound.rate(),
                        sound.sampleCount()
                ))
                .toList();
        return new SoundMetadataList(soundMetadata);
    }

    private DeclarationStmtList convertDecls(final RawTarget target) {
        final List<DeclarationStmt> declarations = new ArrayList<>();

        for (var entry : target.lists().entrySet()) {
            declarations.add(convertList(target, entry.getKey(), entry.getValue()));
        }

        for (var entry : target.broadcasts().entrySet()) {
            declarations.add(convertBroadcast(target, entry.getKey(), entry.getValue()));
        }

        for (var entry : target.variables().entrySet()) {
            declarations.add(convertVariableDeclarations(target, entry.getKey(), entry.getValue()));
        }

        declarations.addAll(convertAttributeDeclarations(target));

        return new DeclarationStmtList(Collections.unmodifiableList(declarations));
    }

    private DeclarationIdentAsTypeStmt convertList(final RawTarget target, final RawBlockId id, final RawList list) {
        final ExpressionList listValues = buildExpressionList(list);

        state.getSymbolTable().addExpressionListInfo(
                id.id(),
                list.name(),
                listValues,
                target.isStage(),
                target.name()
        );

        final LocalIdentifier name = new StrId(list.name());
        return new DeclarationIdentAsTypeStmt(new ScratchList(name), new ListType());
    }

    private ExpressionList buildExpressionList(final RawList list) {
        final List<Expression> items = list.values().stream()
                .map(item -> new StringLiteral(item.toString()))
                .map(Expression.class::cast)
                .toList();
        return new ExpressionList(items);
    }

    private DeclarationBroadcastStmt convertBroadcast(
            final RawTarget target, final RawBlockId id, final String broadcast
    ) {
        final Message message = new Message(new StringLiteral(broadcast));
        state.getSymbolTable().addMessage(broadcast, message, target.isStage(), target.name(), id.id());

        return new DeclarationBroadcastStmt(new StrId(broadcast), new StringType());
    }

    private DeclarationIdentAsTypeStmt convertVariableDeclarations(
            final RawTarget target, final RawBlockId id, final RawVariable<?> variable
    ) {
        final Type varType;

        if (variable.value() instanceof Boolean) {
            varType = new BooleanType();
        } else if (variable.value() instanceof Double || variable.value() instanceof Integer) {
            varType = new NumberType();
        } else if (variable.value() instanceof String) {
            varType = new StringType();
        } else {
            throw new InternalParsingException(
                    "Unknown variable type: '"
                        + variable.value().toString()
                        + "' for variable "
                        + variable.variableName()
            );
        }

        state.getSymbolTable().addVariable(id.id(), variable.variableName(), varType, target.isStage(), target.name());
        return new DeclarationIdentAsTypeStmt(new Variable(new StrId(variable.variableName())), varType);
    }

    private List<DeclarationStmt> convertAttributeDeclarations(final RawTarget target) {
        final List<DeclarationStmt> stmts = new ArrayList<>();

        if (target.volume() != null) {
            final var name = new StringLiteral(Constants.VOLUME_KEY);
            stmts.add(new DeclarationAttributeAsTypeStmt(name, new NumberType()));
        }

        if (target.layerOrder() != null) {
            final var name = new StringLiteral(Constants.LAYERORDER_KEY);
            stmts.add(new DeclarationAttributeAsTypeStmt(name, new NumberType()));
        }

        if (target.stageAttributes() != null && target.isStage()) {
            final RawTarget.StageAttributes attrs = target.stageAttributes();
            if (attrs.tempo() != null) {
                final var name = new StringLiteral(Constants.TEMPO_KEY);
                stmts.add(new DeclarationAttributeAsTypeStmt(name, new NumberType()));
            }
            if (attrs.videoState() != null) {
                final var name = new StringLiteral(Constants.VIDSTATE_KEY);
                stmts.add(new DeclarationAttributeAsTypeStmt(name, new StringType()));
            }
            if (attrs.videoTransparency() != null) {
                final var name = new StringLiteral(Constants.VIDTRANSPARENCY_KEY);
                stmts.add(new DeclarationAttributeAsTypeStmt(name, new StringType()));
            }
            if (attrs.textToSpeechLanguage() != null) {
                final var name = new StringLiteral(Constants.TEXT_TO_SPEECH_KEY);
                stmts.add(new DeclarationAttributeAsTypeStmt(name, new StringType()));
            }
        }

        if (target.spriteAttributes() != null && !target.isStage()) {
            stmts.add(new DeclarationAttributeAsTypeStmt(new StringLiteral(Constants.VISIBLE_KEY), new BooleanType()));
            stmts.add(new DeclarationAttributeAsTypeStmt(new StringLiteral(Constants.X_KEY), new NumberType()));
            stmts.add(new DeclarationAttributeAsTypeStmt(new StringLiteral(Constants.Y_KEY), new NumberType()));
            stmts.add(new DeclarationAttributeAsTypeStmt(new StringLiteral(Constants.SIZE_KEY), new NumberType()));
            stmts.add(new DeclarationAttributeAsTypeStmt(new StringLiteral(Constants.DIRECTION_KEY), new NumberType()));
            stmts.add(new DeclarationAttributeAsTypeStmt(new StringLiteral(Constants.DRAG_KEY), new BooleanType()));
            if (target.spriteAttributes().rotationStyle() != null) {
                stmts.add(new DeclarationAttributeAsTypeStmt(
                        new StringLiteral(Constants.ROTATIONSTYLE_KEY),
                        new StringType())
                );
            }
        }

        return stmts;
    }

    private SetStmtList convertSetStmtList(final RawTarget target) {
        final List<SetStmt> setStmts = new ArrayList<>();

        for (final RawList list : target.lists().values()) {
            final SetVariableTo setVariableTo = new SetVariableTo(
                    new Qualified(new StrId(target.name()), new ScratchList(new StrId(list.name()))),
                    buildExpressionList(list),
                    new NoBlockMetadata()
            );
            setStmts.add(setVariableTo);
        }

        for (final RawVariable<?> variable : target.variables().values()) {
            final Expression expr = initialValue(variable);

            final SetVariableTo setVariableTo = new SetVariableTo(
                    new Qualified(new StrId(target.name()), new Variable(new StrId(variable.variableName()))),
                    expr,
                    new NoBlockMetadata()
            );
            setStmts.add(setVariableTo);
        }

        return new SetStmtList(Collections.unmodifiableList(setStmts));
    }

    private static Expression initialValue(final RawVariable<?> variable) {
        final Expression expr;

        if (variable.value() instanceof String s) {
            expr = new StringLiteral(s);
        } else if (variable.value() instanceof Boolean b) {
            expr = new BoolLiteral(b);
        } else if (variable.value() instanceof Double d) {
            expr = new NumberLiteral(d);
        } else if (variable.value() instanceof Integer i) {
            expr = new NumberLiteral(i);
        } else {
            throw new InternalParsingException(
                    "Unsupported initial type for variable '"
                            + variable.variableName()
                            + "': "
                            + variable.value()
            );
        }

        return expr;
    }

    private ProcedureDefinitionList convertProcDefs(final RawTarget target) {
        // todo
        return new ProcedureDefinitionList(Collections.emptyList());
    }

    private ScriptList convertScripts(final RawTarget target) {
        // todo
        return new ScriptList(Collections.emptyList());
    }
}
