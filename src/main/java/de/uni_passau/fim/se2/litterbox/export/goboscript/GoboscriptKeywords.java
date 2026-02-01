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
package de.uni_passau.fim.se2.litterbox.export.goboscript;

import java.util.Set;

/**
 * Constants for Goboscript keywords and reserved commands.
 */
public final class GoboscriptKeywords {

    private GoboscriptKeywords() {
        // Prevent instantiation
    }

    /**
     * Set of Goboscript built-in command names that could conflict with user-defined procedure names.
     * When a procedure has one of these names, it must be suffixed to avoid ambiguity.
     */
    public static final Set<String> RESERVED_COMMANDS = Set.of(
            // Motion blocks
            "move", "turn_left", "turn_right", "goto", "glide", "glide_to", "glide_to_mouse_pointer",
            "glide_to_random_position", "goto_mouse_pointer", "goto_random_position",
            "point_in_direction", "point_towards", "point_towards_mouse_pointer", "point_towards_random_direction",
            "change_x", "change_y", "set_x", "set_y", "if_on_edge_bounce",
            "set_rotation_style_left_right", "set_rotation_style_do_not_rotate", "set_rotation_style_all_around",
            // Looks blocks
            "say", "think", "show", "hide", "switch_costume", "next_costume", "switch_backdrop",
            "next_backdrop", "previous_backdrop", "random_backdrop", "change_size", "set_size",
            "change_color_effect", "set_color_effect", "change_brightness_effect", "set_brightness_effect",
            "change_ghost_effect", "set_ghost_effect", "change_fisheye_effect", "set_fisheye_effect",
            "change_whirl_effect", "set_whirl_effect", "change_pixelate_effect", "set_pixelate_effect",
            "change_mosaic_effect", "set_mosaic_effect", "clear_graphic_effects",
            "go_forward", "go_backward", "goto_front", "goto_back",
            // Sound blocks
            "start_sound", "play_sound_until_done", "stop_all_sounds",
            "change_volume", "set_volume", "change_pitch_effect", "set_pitch_effect",
            "change_pan_effect", "set_pan_effect", "clear_sound_effects",
            // Control blocks
            "wait", "clone", "delete_this_clone", "stop_all", "stop_this_script", "stop_other_scripts",
            // Sensing blocks
            "ask", "reset_timer", "set_drag_mode_draggable", "set_drag_mode_not_draggable",
            // Pen blocks
            "erase_all", "stamp", "pen_down", "pen_up", "set_pen_color", "change_pen_size", "set_pen_size",
            "change_pen_hue", "set_pen_hue", "change_pen_saturation", "set_pen_saturation",
            "change_pen_brightness", "set_pen_brightness", "change_pen_transparency", "set_pen_transparency",
            // Events
            "broadcast", "broadcast_and_wait",
            // Variables/Lists
            "add", "delete", "insert", "replace",
            // Music
            "rest", "play", "set_tempo", "change_tempo"
    );

    /**
     * Set of Goboscript keywords that cannot be used as variable or list names.
     * This includes language keywords, built-in functions, and operators.
     */
    public static final Set<String> KEYWORDS = Set.of(
            // Control flow
            "if", "else", "elif", "forever", "repeat", "until", "return",
            // Event handlers
            "on", "onflag", "onkey", "onclick", "onbackdrop", "onloudness", "ontimer", "onclone",
            // Function/procedure definitions
            "proc", "func", "var", "local", "nowarp",
            // Type keywords
            "list", "cloud", "enum", "struct",
            // Boolean and logic
            "true", "false", "not", "and", "or", "in",
            // Mathematical functions
            "length", "round", "abs", "floor", "ceil", "sqrt",
            "sin", "cos", "tan", "asin", "acos", "atan",
            "ln", "log", "antiln", "antilog",
            // List operations
            "add", "to", "delete", "insert", "at", "as",
            // Display/audio keywords
            "show", "hide", "costumes", "sounds",
            // Legacy/compatibility (kept for safety)
            "wait", "global"
    );
}
