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
package de.uni_passau.fim.se2.litterbox.export;

/**
 * Supported export formats for converting Scratch projects to text-based representations.
 */
public enum ExportFormat {

    /**
     * ScratchBlocks format - visual block notation for documentation.
     *
     * @see <a href="https://en.scratch-wiki.info/wiki/Block_Plugin/Syntax">ScratchBlocks Syntax</a>
     */
    SCRATCHBLOCKS(".txt"),

    /**
     * Goboscript format - C/Rust-style text language that compiles to Scratch.
     *
     * @see <a href="https://aspizu.github.io/goboscript/">Goboscript Documentation</a>
     */
    GOBOSCRIPT(".gs"),

    /**
     * Leila intermediate language for program analysis.
     */
    LEILA(".sc"),

    /**
     * PyStage format - Python code using the PyStage library for Scratch-like programming.
     *
     * @see <a href="https://github.com/pystage/pystage">PyStage Documentation</a>
     */
    PYSTAGE(".py");

    private final String extension;

    ExportFormat(String extension) {
        this.extension = extension;
    }

    /**
     * Returns the default file extension for this format.
     *
     * @return The file extension including the leading dot
     */
    public String getExtension() {
        return extension;
    }
}
