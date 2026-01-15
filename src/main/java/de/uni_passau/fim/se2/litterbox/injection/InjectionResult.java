/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.injection;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

/**
 * Holds the result of a single bug injection, including metadata for reporting.
 *
 * @param mutatedProgram              The program with the injected bug.
 * @param spriteName                  The name of the sprite in which the bug was injected.
 * @param originalScriptId            The block ID of the script in the original program.
 * @param mutatedScriptId             The block ID of the script in the mutated program
 *                                    (for targeting with --script after re-loading the saved file).
 * @param originalScriptScratchBlocks The ScratchBlocks text of the original script that was mutated.
 * @param mutatedScriptScratchBlocks  The ScratchBlocks text of the script after mutation.
 */
public record InjectionResult(
        Program mutatedProgram,
        String spriteName,
        String originalScriptId,
        String mutatedScriptId,
        String originalScriptScratchBlocks,
        String mutatedScriptScratchBlocks
) {
}
