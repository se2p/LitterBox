package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class OverwrittenVisualEffectTest implements JsonTest {

    private final ScratchBlocksParser parser = new ScratchBlocksParser();

    @Test
    public void testOverwrittenCostume() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            switch costume to (costume1 v)
            switch costume to (costume2 v)
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).hasSize(1);
    }

    @Test
    public void testOverwrittenCostumeWithWait() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            switch costume to (costume1 v)
            wait (1) seconds
            switch costume to (costume2 v)
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).isEmpty();
    }
    
    @Test
    public void testOverwrittenEffect() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            set [color v] effect to (10)
            set [color v] effect to (20)
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).hasSize(1);
    }

    @Test
    public void testDifferentEffects() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            set [color v] effect to (10)
            set [ghost v] effect to (50)
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).isEmpty();
    }

    @Test
    public void testClearEffects() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            set [color v] effect to (10)
            clear graphic effects
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).hasSize(1);
    }
    
    @Test
    public void testClearEffectsOverwritten() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            clear graphic effects
            clear graphic effects
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).hasSize(1);
    }

    @Test
    public void testSayOverwritten() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            say [Hello]
            say [World]
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).hasSize(1);
    }

    @Test
    public void testSayForSecsHalts() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            say [Hello]
            say [World] for (2) seconds
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).isEmpty();
    }

    @Test
    public void testSetEffectThenClear() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            set [brightness v] effect to (50)
            clear graphic effects
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).hasSize(1);
    }

    @Test
    public void testConditionalOverwrite() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            switch costume to (costume1 v)
            if <key (space v) pressed?> then
                switch costume to (costume2 v)
            end
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).isEmpty();
    }

    @Test
    public void testConditionalHalting() throws IOException, ParsingException {
        Program base = getAST("src/test/fixtures/emptyProject.json");
        String code = """
            when green flag clicked
            switch costume to (costume1 v)
            if <key (space v) pressed?> then
                wait (1) seconds
            end
            switch costume to (costume2 v)
            """;
        Program prog = parser.extendProject(base, "Sprite1", code);
        assertThat(new OverwrittenVisualEffect().check(prog)).isEmpty();
    }
}
