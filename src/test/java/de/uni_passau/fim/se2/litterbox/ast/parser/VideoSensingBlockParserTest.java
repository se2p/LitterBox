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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.SetVideoTransparency;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.TurnVideo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.VideoOn;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.WhenVideoMotionGreaterThan;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeFilteringVisitor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class VideoSensingBlockParserTest implements JsonTest {

    @Test
    void parseVideoSensingBlocks() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/videoSensing.json");

        // Test WhenVideoMotionGreaterThan
        List<WhenVideoMotionGreaterThan> events = NodeFilteringVisitor.getBlocks(program, WhenVideoMotionGreaterThan.class);
        assertEquals(1, events.size());
        WhenVideoMotionGreaterThan event = events.get(0);
        assertInstanceOf(NumberLiteral.class, event.getReference());
        assertEquals(50.0, ((NumberLiteral) event.getReference()).getValue(), 0.001);

        // Test VideoOn
        List<VideoOn> videoOns = NodeFilteringVisitor.getBlocks(program, VideoOn.class);
        assertEquals(1, videoOns.size());
        VideoOn videoOn = videoOns.get(0);
        assertEquals("motion", videoOn.getAttribute());
        assertEquals("this sprite", videoOn.getSubject());

        // Test TurnVideo
        List<TurnVideo> turnVideos = NodeFilteringVisitor.getBlocks(program, TurnVideo.class);
        assertEquals(1, turnVideos.size());
        TurnVideo turnVideo = turnVideos.get(0);
        assertEquals("on", turnVideo.getVideoState());

        // Test SetVideoTransparency
        List<SetVideoTransparency> setTransparencies = NodeFilteringVisitor.getBlocks(program, SetVideoTransparency.class);
        assertEquals(1, setTransparencies.size());
        SetVideoTransparency setTransparency = setTransparencies.get(0);
        assertInstanceOf(NumberLiteral.class, setTransparency.getTransparency());
        assertEquals(50.0, ((NumberLiteral) setTransparency.getTransparency()).getValue(), 0.001);
    }
}
