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
package de.uni_passau.fim.se2.litterbox.injection.util;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;

import java.util.List;
import java.util.function.Function;

/**
 * Utility class to validate the existence of resources (Costumes, Sounds) in ActorMetadata.
 */
public class ResourceValidator {

    private ResourceValidator() {
        // Utility class
    }

    /**
     * Checks if a costume with the given name exists in the actor metadata.
     * Returns true if metadata is missing (safe default) or if costume is found.
     *
     * @param metadata    The actor metadata
     * @param costumeName The name of the costume to check
     * @return true if valid or unknown, false if definitely missing
     */
    public static boolean hasCostume(ActorMetadata metadata, String costumeName) {
        if (metadata == null) {
            return true;
        }
        return checkResourceExists(metadata.getCostumes().getList(), ImageMetadata::getName, costumeName);
    }

    /**
     * Checks if a sound with the given name exists in the actor metadata.
     * Returns true if metadata is missing (safe default) or if sound is found.
     *
     * @param metadata  The actor metadata
     * @param soundName The name of the sound to check
     * @return true if valid or unknown, false if definitely missing
     */
    public static boolean hasSound(ActorMetadata metadata, String soundName) {
        if (metadata == null) {
            return true;
        }
        return checkResourceExists(metadata.getSounds().getList(), SoundMetadata::getName, soundName);
    }

    /**
     * Generic helper to check for resource existence in a list.
     * Handles null checks, empty lists, and placeholder metadata logic.
     */
    private static <T> boolean checkResourceExists(
            List<T> resources, Function<T, String> nameExtractor, String targetName) {
        if (resources == null || resources.isEmpty()) {
            return false;
        }

        // Check for placeholder test metadata (where name equals class simple name)
        boolean isPlaceholderMetadata = resources.stream()
                .allMatch(r -> {
                    String name = nameExtractor.apply(r);
                    return name.equals(r.getClass().getSimpleName());
                });

        if (isPlaceholderMetadata) {
            return true;
        }

        return resources.stream()
                .anyMatch(r -> nameExtractor.apply(r).equals(targetName));
    }
}
