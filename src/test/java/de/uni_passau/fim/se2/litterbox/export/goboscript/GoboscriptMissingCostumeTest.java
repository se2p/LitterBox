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

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class GoboscriptMissingCostumeTest {

    @Test
    public void testMissingCostumeIsSkipped() {
        NoBlockMetadata meta = new NoBlockMetadata();
        
        // Define a costume "costume1" with md5 "missing_asset.svg"
        List<ImageMetadata> costumes = new ArrayList<>();
        costumes.add(new ImageMetadata("missing_asset.svg", "costume1", "missing_asset.svg", "svg", 1.0, 0, 0));
        ImageMetadataList costumeList = new ImageMetadataList(costumes);
        
        ActorMetadata actorMeta = new ActorMetadata(
            new CommentMetadataList(new ArrayList<>()), 
            0, 
            costumeList, 
            new SoundMetadataList(new ArrayList<>())
        );

        // Define script: switch costume to "costume1"
        SwitchCostumeTo switchCostume = new SwitchCostumeTo(
            new WithExpr(
                new StringLiteral("costume1"),
                meta
            ),
            meta
        );
        
        // Create minimal ActorDefinition
        // We cheat slightly by not putting the script in a full Script/ScriptList structure 
        // because we can just visit the SwitchCostumeTo node directly, 
        // BUT we need the visitor to have visited the ActorDefinition to populate metadata.
        // So we might need to visit the whole actor.
        
        // Let's create a full actor with one script or procedure to hold the statement if needed, 
        // or we can just visit the statement AFTER validCostumes is populated (if we could inject it).
        // Better: create full structure.
        
        // Actually, SwitchCostumeTo is a Stmt. We can't put it directly in ActorDefinition scripts list easily without correct wrapping.
        // But for this test, we can:
        // 1. Visit ActorDefinition to populate "validCostumes" (we need to modify visitor to do this first).
        // 2. Visit the SwitchCostumeTo node.
        
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        
        // Set available assets to exclude "missing_asset.svg"
        Set<String> availableAssets = new HashSet<>();
        availableAssets.add("existing_asset.svg");
        visitor.setAvailableAssets(availableAssets);
        
        // First, visit actor definition to populate context (if we implement the fix)
        ActorDefinition actor = new ActorDefinition(
            ActorType.getSprite(),
            new StrId("Sprite1"),
            new DeclarationStmtList(new ArrayList<>()),
            new SetStmtList(new ArrayList<>()),
            new ProcedureDefinitionList(new ArrayList<>()),
            new ScriptList(new ArrayList<>()), // Empty scripts for now
            actorMeta
        );
        
        visitor.visit(actor);
        
        // Now visit the statement manually using the SAME visitor instance
        switchCostume.accept(visitor);
        
        String result = visitor.getOutput();
        
        // BEFORE FIX: usage of missing costume IS emitted
        // assertThat(result).contains("switch_costume \"costume1\";");
        
        // AFTER FIX: it should be emitted as a comment
        assertThat(result).contains("# switch_costume \"costume1\";");
    }
}
