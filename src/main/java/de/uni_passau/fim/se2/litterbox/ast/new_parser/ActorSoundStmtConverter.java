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
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ActorSoundStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

final class ActorSoundStmtConverter extends StmtConverter<ActorSoundStmt> {
    ActorSoundStmtConverter(ProgramParserState state) {
        super(state);
    }

    @Override
    ActorSoundStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final ActorSoundStmtOpcode opcode = ActorSoundStmtOpcode.valueOf(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case sound_playuntildone -> {
                final ElementChoice sound = getSoundElement(block);
                yield new PlaySoundUntilDone(sound, metadata);
            }
            case sound_play -> {
                final ElementChoice sound = getSoundElement(block);
                yield new StartSound(sound, metadata);
            }
            case sound_cleareffects -> new ClearSoundEffects(metadata);
            case sound_stopallsounds -> new StopAllSounds(metadata);
            case sound_setvolumeto -> {
                final NumExpr to = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.VOLUME_KEY_CAPS)
                );
                yield new SetVolumeTo(to, metadata);
            }
            case sound_changevolumeby -> {
                final NumExpr by = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.VOLUME_KEY_CAPS)
                );
                yield new ChangeVolumeBy(by, metadata);
            }
            case sound_seteffectto -> {
                final NumExpr to = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.VALUE_KEY)
                );
                final String effectName = block.fields().get(Constants.EFFECT_KEY).value().toString();
                final SoundEffect effect = new SoundEffect(effectName);

                yield new SetSoundEffectTo(effect, to, metadata);
            }
            case sound_changeeffectby -> {
                final NumExpr by = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.VALUE_KEY)
                );
                final String effectName = block.fields().get(Constants.EFFECT_KEY).value().toString();
                final SoundEffect effect = new SoundEffect(effectName);

                yield new ChangeSoundEffectBy(effect, by, metadata);
            }
        };
    }

    private ElementChoice getSoundElement(final RawBlock.RawRegularBlock block) {
        final RawInput soundInput = block.inputs().get(Constants.SOUND_MENU);

        if (
                ShadowType.SHADOW.equals(soundInput.shadowType())
                        && soundInput.input() instanceof BlockRef.IdRef blockIdRef
        ) {
            return convertSoundChoiceMenu(blockIdRef.id());
        } else {
            final BlockMetadata metadata = new NoBlockMetadata();
            final Expression expr = ExpressionConverter.convertExpr(state, block, soundInput);
            return new WithExpr(expr, metadata);
        }
    }

    private ElementChoice convertSoundChoiceMenu(final RawBlockId menuRef) {
        final RawBlock menu = state.getBlock(menuRef);
        if (!(menu instanceof RawBlock.RawRegularBlock menuBlock)) {
            throw new InternalParsingException("Unknown menu representation.");
        }

        final String elementName = menuBlock.fields().get(Constants.SOUND_MENU).value().toString();
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(menuRef, menu);

        return new WithExpr(new StrId(elementName), metadata);
    }
}
