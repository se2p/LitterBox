package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExtractSpriteVisitorTest implements JsonTest {

    @Test
    void testVisit() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        ExtractSpriteVisitor spriteVisitor = new ExtractSpriteVisitor(false);
        program.accept(spriteVisitor);
        Map<ActorDefinition, List<ASTNode>> leafsMap = spriteVisitor.getLeafsCollector();

        assertEquals(2, leafsMap.keySet().size());

        ASTNode[] sprites = getSpriteArrayFromLeafsMap(leafsMap);

        //check sprite abby
        assertEquals("abby", ((ActorDefinition)sprites[0]).getIdent().getName());
        assertEquals(2, leafsMap.get(sprites[0]).size());
        assertEquals("GreenFlag", leafsMap.get(sprites[0]).get(0).getUniqueName());
        assertEquals("StringLiteral", leafsMap.get(sprites[0]).get(1).getUniqueName());

        //check sprite cat
        assertEquals("cat", ((ActorDefinition)sprites[1]).getIdent().getName());
        assertEquals(3, leafsMap.get(sprites[1]).size());
        assertEquals("NumberLiteral", leafsMap.get(sprites[1]).get(0).getUniqueName());
        assertEquals("StringLiteral", leafsMap.get(sprites[1]).get(1).getUniqueName());
        assertEquals("Show", leafsMap.get(sprites[1]).get(2).getUniqueName());
    }

    @Test
    void testVisitIncludeStage() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        ExtractSpriteVisitor spriteVisitor = new ExtractSpriteVisitor(true);
        program.accept(spriteVisitor);

        Map<ActorDefinition, List<ASTNode>> leafsMap = spriteVisitor.getLeafsCollector();
        assertEquals(3, leafsMap.keySet().size());

        Optional<ActorDefinition> stage = leafsMap.keySet().stream().filter(ActorDefinition::isStage).findFirst();
        assertTrue(stage.isPresent());
    }

    private ASTNode[] getSpriteArrayFromLeafsMap(Map<ActorDefinition, List<ASTNode>> leafsMap) {
        ASTNode[] sprites = new ASTNode[2];
        for (ActorDefinition sprite : leafsMap.keySet()) {
            if (sprite.getIdent().getName().equals("abby")) {
                sprites[0] = sprite;
            } else if (sprite.getIdent().getName().equals("cat")){
                sprites[1] = sprite;
            } else {
                fail("Expected were 'abby' or 'cat' but was " + sprite.getIdent().getName());
            }
        }
        return sprites;
    }
}
