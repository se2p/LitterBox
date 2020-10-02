package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StartSound;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;

import java.util.List;

/**
 * If the costume of a sprite should be set to one not available or play a sound that is not registered in the sprite it will not work.
 */
public class MissingResource extends AbstractIssueFinder {
    public static final String NAME = "missing_resource";
    private List<ImageMetadata> images;
    private List<ImageMetadata> backdrops;
    private List<SoundMetadata> sounds;

    @Override
    public void visit(Program node) {
        for (ActorDefinition actor : node.getActorDefinitionList().getDefinitions()) {
            if (actor.getActorType().isStage()) {
                backdrops = actor.getActorMetadata().getCostumes().getList();
                break;
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(ActorDefinition actor) {
        images = actor.getActorMetadata().getCostumes().getList();
        sounds = actor.getActorMetadata().getSounds().getList();
        super.visit(actor);
    }

    @Override
    public void visit(SwitchBackdrop node) {
        if (node.getElementChoice() instanceof WithExpr && ((WithExpr) node.getElementChoice()).getExpression() instanceof StrId) {
            String backdropName = ((StrId) ((WithExpr) node.getElementChoice()).getExpression()).getName();
            if (!imageExists(backdropName, backdrops)) {
                addIssue(node, node.getMetadata());
            }
        }
    }

    private boolean imageExists(String imageName, List<ImageMetadata> metadataList) {
        for (ImageMetadata meta : metadataList) {
            if (imageName.equals(meta.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        if (node.getElementChoice() instanceof WithExpr && ((WithExpr) node.getElementChoice()).getExpression() instanceof StrId) {
            String backdropName = ((StrId) ((WithExpr) node.getElementChoice()).getExpression()).getName();
            if (!imageExists(backdropName, backdrops)) {
                addIssue(node, node.getMetadata());
            }
        }
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        if (node.getCostumeChoice() instanceof WithExpr && ((WithExpr) node.getCostumeChoice()).getExpression() instanceof StrId) {
            String costume = ((StrId) ((WithExpr) node.getCostumeChoice()).getExpression()).getName();
            if (!imageExists(costume, images)) {
                addIssue(node, node.getMetadata());
            }
        }
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        if (node.getElementChoice() instanceof WithExpr && ((WithExpr) node.getElementChoice()).getExpression() instanceof StrId) {
            String sound = ((StrId) ((WithExpr) node.getElementChoice()).getExpression()).getName();
            if (!soundExists(sound)) {
                addIssue(node, node.getMetadata());
            }
        }
    }

    @Override
    public void visit(StartSound node) {
        if (node.getElementChoice() instanceof WithExpr && ((WithExpr) node.getElementChoice()).getExpression() instanceof StrId) {
            String sound = ((StrId) ((WithExpr) node.getElementChoice()).getExpression()).getName();
            if (!soundExists(sound)) {
                addIssue(node, node.getMetadata());
            }
        }
    }

    private boolean soundExists(String sound) {
        for (SoundMetadata meta : sounds) {
            if (sound.equals(meta.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
