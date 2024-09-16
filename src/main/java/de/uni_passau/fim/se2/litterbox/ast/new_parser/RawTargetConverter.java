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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.StageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.*;
import de.uni_passau.fim.se2.litterbox.ast.model.type.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ProcedureOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

final class RawTargetConverter {
    private final ProgramParserState state;
    private final RawTarget target;

    private RawTargetConverter(final ProgramParserState state, final RawTarget target) {
        this.state = state;
        this.target = target;
    }

    public static ActorDefinition convertTarget(final ProgramParserState state, final RawTarget target) {
        final RawTargetConverter converter = new RawTargetConverter(state, target);
        return converter.convertTarget();
    }

    ActorDefinition convertTarget() {
        final LocalIdentifier ident = new StrId(target.name());
        state.setCurrentActor(ident);

        final ActorType actorType;
        if (target.isStage()) {
            actorType = ActorType.getStage();
        } else {
            actorType = ActorType.getSprite();
        }

        final ActorMetadata metadata = convertActorMetadata();
        final DeclarationStmtList decls = convertDecls();
        final SetStmtList setStmtList = convertSetStmtList();

        final BlocksByOpcode topLevelBlocks = getBlocksByOpcode();
        final ProcedureDefinitionList procedureDefinitionList = convertProcDefs(topLevelBlocks);
        final ScriptList scripts = convertScripts();

        return new ActorDefinition(actorType, ident, decls, setStmtList, procedureDefinitionList, scripts, metadata);
    }

    private BlocksByOpcode getBlocksByOpcode() {
        return new BlocksByOpcode(
                target.blocks().entrySet().stream()
                        .filter(entry -> entry.getValue() instanceof RawBlock.RawRegularBlock)
                        .collect(Collectors.groupingBy(
                                entry -> ((RawBlock.RawRegularBlock) entry.getValue()).opcode(),
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> (RawBlock.RawRegularBlock) entry.getValue()
                                )
                        ))
        );
    }

    private ActorMetadata convertActorMetadata() {
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

    private DeclarationStmtList convertDecls() {
        final List<DeclarationStmt> declarations = new ArrayList<>();

        for (var entry : target.lists().entrySet()) {
            declarations.add(convertList(entry.getKey(), entry.getValue()));
        }

        for (var entry : target.broadcasts().entrySet()) {
            declarations.add(convertBroadcast(entry.getKey(), entry.getValue()));
        }

        for (var entry : target.variables().entrySet()) {
            declarations.add(convertVariableDeclarations(entry.getKey(), entry.getValue()));
        }

        declarations.addAll(convertAttributeDeclarations());

        return new DeclarationStmtList(Collections.unmodifiableList(declarations));
    }

    private DeclarationIdentAsTypeStmt convertList(final RawBlockId id, final RawList list) {
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

    private DeclarationBroadcastStmt convertBroadcast(final RawBlockId id, final String broadcast) {
        final Message message = new Message(new StringLiteral(broadcast));
        state.getSymbolTable().addMessage(broadcast, message, target.isStage(), target.name(), id.id());

        return new DeclarationBroadcastStmt(new StrId(broadcast), new StringType());
    }

    private DeclarationIdentAsTypeStmt convertVariableDeclarations(final RawBlockId id, final RawVariable<?> variable) {
        final Type varType;

        if (variable.value() instanceof Boolean) {
            varType = new BooleanType();
        } else if (isNumericVariable(variable)) {
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

    private static boolean isNumericVariable(final RawVariable<?> variable) {
        return variable.value() instanceof Double
                || variable.value() instanceof Integer
                || variable.value() instanceof Long;
    }

    private List<DeclarationStmt> convertAttributeDeclarations() {
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

    private SetStmtList convertSetStmtList() {
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
        } else if (variable.value() instanceof Long l) {
            expr = new NumberLiteral(l);
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

    private ProcedureDefinitionList convertProcDefs(final BlocksByOpcode blocks) {
        final var procDefs = blocks.getBlocks(ProcedureOpcode.procedures_definition.getName());
        final var procProtos = blocks.getBlocks(ProcedureOpcode.procedures_prototype.getName());

        // each definition needs the prototype block because it holds the name of the procedure
        Preconditions.checkArgument(procDefs.size() == procProtos.size());

        final List<ProcedureDefinition> definitions = procDefs.entrySet().stream()
                .map(entry -> convertProcDef(entry.getKey(), entry.getValue()))
                .toList();

        return new ProcedureDefinitionList(definitions);
    }

    private ProcedureDefinition convertProcDef(
            final RawBlockId definitionId, final RawBlock.RawRegularBlock procedureDefinition
    ) {
        final var prototypeBlockInfo = getProcedurePrototypeForDefinition(procedureDefinition);
        final RawBlock.RawRegularBlock prototypeBlock = prototypeBlockInfo.getRight();

        final LocalIdentifier ident = parseProcedureIdentifier(definitionId, prototypeBlock);
        final ParameterDefinitionList parameters = convertProcedureParameters(prototypeBlock);

        // todo: parse statements inside procedure
        final StmtList stmts = new StmtList(Collections.emptyList());
        final ProcedureMetadata metadata = convertProcedureMetadata(
                definitionId, procedureDefinition, prototypeBlockInfo.getLeft(), prototypeBlock
        );

        final String methodName = getMethodName(prototypeBlock);
        addProcedureToState(ident, methodName, parameters);

        return new ProcedureDefinition(ident, parameters, stmts, metadata);
    }

    private void addProcedureToState(
            final LocalIdentifier ident, final String methodName, final ParameterDefinitionList parameterDefinitionList
    ) {
        final String currentActor = state.getCurrentActor().getName();
        state.getProcDefMap().addProcedure(ident, currentActor, methodName, parameterDefinitionList);
    }

    private Pair<RawBlockId, RawBlock.RawRegularBlock> getProcedurePrototypeForDefinition(
            final RawBlock.RawRegularBlock procedureDefinition
    ) {
        final RawInput input = procedureDefinition.inputs().get(Constants.CUSTOM_BLOCK_KEY);

        if (input.input() instanceof BlockRef.IdRef defId) {
            final RawBlockId prototypeBlockId = defId.id();
            final RawBlock prototype = target.blocks().get(prototypeBlockId);
            if (prototype instanceof RawBlock.RawRegularBlock p) {
                return Pair.of(prototypeBlockId, p);
            } else {
                throw new InternalParsingException(
                        "Unexpected format for procedure prototype: " + prototypeBlockId.id()
                );
            }
        } else {
            throw new InternalParsingException("Expected procedure definition to contain reference to prototype!");
        }
    }

    private String getMethodName(final RawBlock.RawRegularBlock procedurePrototype) {
        final String baseName = getProcedurePrototypeInformation(procedurePrototype).proccode();

        // %n exists from some methods (probably due to conversion from Scratch 2), but it is not possible to construct
        // such methods in Scratch any more. The number/text input is always created as %s using the Scratch UI.
        return baseName.replace("%n", "%s");
    }

    private LocalIdentifier parseProcedureIdentifier(
            final RawBlockId definitionId, final RawBlock.RawRegularBlock procedurePrototype
    ) {
        return new StrId(procedurePrototype.parent().orElse(definitionId).id());
    }

    private ParameterDefinitionList convertProcedureParameters(final RawBlock.RawRegularBlock procedurePrototype) {
        final RawMutation procInfo = getProcedurePrototypeInformation(procedurePrototype);
        final List<String> argumentNames = procInfo.argumentnames();
        final List<RawBlockId> argumentIds = procInfo.argumentids();
        final List<RawMutation.ArgumentDefault<?>> argumentDefaults = procInfo.argumentdefaults();

        final List<ParameterDefinition> definitions = new ArrayList<>();

        for (int i = 0; i < argumentIds.size(); ++i) {
            final StrId name = new StrId(argumentNames.get(i));
            final String argumentId = argumentIds.get(i).id();
            final RawInput argumentInput = procedurePrototype.inputs().get(argumentId);
            final Type parameterType = getParameterTypeFromInput(name, argumentInput, argumentDefaults.get(i));

            definitions.add(new ParameterDefinition(name, parameterType, new NoBlockMetadata()));
        }

        return new ParameterDefinitionList(Collections.unmodifiableList(definitions));
    }

    private static RawMutation getProcedurePrototypeInformation(final RawBlock.RawRegularBlock procedurePrototype) {
        return procedurePrototype.mutation()
                .orElseThrow(() -> new InternalParsingException("Incomplete procedure definition!"));
    }

    private Type getParameterTypeFromInput(
            final StrId name, final RawInput argumentInput, final RawMutation.ArgumentDefault<?> argumentDefault
    ) {
        if (argumentInput == null) {
            return getParameterType(name, argumentDefault);
        } else if (argumentInput.input() instanceof BlockRef.IdRef inputIdRef) {
            final RawBlock inputBlock = target.blocks().get(inputIdRef.id());

            if (inputBlock instanceof RawBlock.RawRegularBlock block) {
                if (ProcedureOpcode.argument_reporter_boolean.getName().equals(block.opcode())) {
                    return new BooleanType();
                } else if (ProcedureOpcode.argument_reporter_string_number.getName().equals(block.opcode())) {
                    return new StringType();
                }
            }
        }

        throw new InternalParsingException("Unknown parameter type format!");
    }

    private static Type getParameterType(final StrId name, final RawMutation.ArgumentDefault<?> defaultValue) {
        final Type parameterType;

        // implementation note: should be changed to pattern-matching switch when updating to Java 21
        if (
                defaultValue instanceof RawMutation.ArgumentDefault.StringArgumentDefault
                        || defaultValue instanceof RawMutation.ArgumentDefault.NumArgumentDefault
        ) {
            parameterType = new StringType();
        } else if (defaultValue instanceof RawMutation.ArgumentDefault.BoolArgumentDefault) {
            parameterType = new BooleanType();
        } else {
            throw new InternalParsingException("Parameter '" + name.getName() + "' has unknown type!");
        }

        return parameterType;
    }

    private ProcedureMetadata convertProcedureMetadata(
            final RawBlockId defId,
            final RawBlock.RawRegularBlock procedureDefinition,
            final RawBlockId protoId,
            final RawBlock.RawRegularBlock procedurePrototype
    ) {
        final BlockMetadata defMeta = RawBlockMetadataConverter.convertBlockMetadata(defId, procedureDefinition);
        final BlockMetadata protoMeta = RawBlockMetadataConverter.convertBlockMetadata(protoId, procedurePrototype);

        return new ProcedureMetadata(defMeta, protoMeta);
    }

    private ScriptList convertScripts() {
        // todo
        return new ScriptList(Collections.emptyList());
    }

    private record BlocksByOpcode(Map<String, Map<RawBlockId, RawBlock.RawRegularBlock>> blocks) {
        Map<RawBlockId, RawBlock.RawRegularBlock> getBlocks(final String opcode) {
            final var opCodeBlocks = blocks.get(opcode);
            return Objects.requireNonNullElse(opCodeBlocks, Collections.emptyMap());
        }
    }
}
