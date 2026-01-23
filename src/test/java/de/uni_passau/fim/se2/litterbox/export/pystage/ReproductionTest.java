package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import java.io.IOException;
import static com.google.common.truth.Truth.assertThat;

public class ReproductionTest implements JsonTest {

    @Test
    public void testRepro__400087814_22_05_2021_17_47_24_sb3() throws IOException, ParsingException {
        // Failed due to: Runtime Error: es/pystage/core/assets.py", line 198, in add_sound     sound = Sound(self, name)   File "/Users/gordon/.pyenv/versions/3.10.16/lib/python3.10/site-packages/pystage/core/assets.py", line 235, in __init__     if self.file.endswith(".mp3"): AttributeError: 'NoneType' object has no attribute 'endswith' 
        Program program = getAST("src/test/fixtures/repro/400087814_22-05-2021_17-47-24.sb3");
        String result = PyStageVisitor.of(program);
        assertThat(result).isNotEmpty();
    }


    @Test
    public void testRepro__400111977_23_05_2021_08_19_32_sb3() throws IOException, ParsingException {
        // Failed due to: Runtime Error: /site-packages/pystage/core/_sound.py", line 34, in sound_playuntildone     sound = self.sound_manager.get_sound(name)   File "/Users/gordon/.pyenv/versions/3.10.16/lib/python3.10/site-packages/pystage/core/assets.py", line 205, in get_sound     return self.sounds[name].sound KeyError: 'recording2' 
        Program program = getAST("src/test/fixtures/repro/400111977_23-05-2021_08-19-32.sb3");
        String result = PyStageVisitor.of(program);
        assertThat(result).isNotEmpty();
    }


    @Test
    public void testRepro__402141576_26_05_2021_23_16_29_sb3() throws IOException, ParsingException {
        // Failed due to: Runtime Error: eturn self._core.motion_goto_sprite(sprite)   File "/Users/gordon/.pyenv/versions/3.10.16/lib/python3.10/site-packages/pystage/core/_motion.py", line 65, in motion_goto_sprite     self.motion_gotoxy(sprite.x_position(), sprite.y_position()) AttributeError: 'int' object has no attribute 'x_position' 
        Program program = getAST("src/test/fixtures/repro/402141576_26-05-2021_23-16-29.sb3");
        String result = PyStageVisitor.of(program);
        assertThat(result).isNotEmpty();
    }


    @Test
    public void testRepro__501004945_13_05_2021_18_47_16_sb3() throws IOException, ParsingException {
        // Failed due to: Runtime Error: /temp_export_501004945_13-05-2021_18-47-16.sb3/501004945_13-05-2021_18-47-16/501004945_13-05-2021_18-47-16.py", line 206, in _________0_01_______     self.set_variable("0.01秒", math.floor(((self.days_since_2000() * 8640000) % 100))) AttributeError: 'Sprite' object has no attribute 'days_since_2000' 
        Program program = getAST("src/test/fixtures/repro/501004945_13-05-2021_18-47-16.sb3");
        String result = PyStageVisitor.of(program);
        assertThat(result).isNotEmpty();
    }

}
