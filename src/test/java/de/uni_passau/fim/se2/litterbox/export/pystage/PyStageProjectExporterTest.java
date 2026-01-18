package de.uni_passau.fim.se2.litterbox.export.pystage;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class PyStageProjectExporterTest {

    @Test
    public void testExportWithNullMd5ext(@TempDir Path tempDir) throws Exception {
        // Create a dummy sb3 zip file
        Path sb3File = tempDir.resolve("project.sb3");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(sb3File))) {
            zos.putNextEntry(new ZipEntry("project.json"));
            zos.write("{}".getBytes()); 
            zos.closeEntry();
        }

        // JSON with null md5ext in costumes. 
        // Note: md5ext is explicitly null.
        String json = "{\"targets\":[{\"isStage\":true,\"name\":\"Stage\",\"variables\":{},\"lists\":{},\"broadcasts\":{},\"blocks\":{},\"comments\":{},\"currentCostume\":0,\"costumes\":[{\"assetId\":\"id1\",\"name\":\"c1\",\"dataFormat\":\"png\",\"md5ext\":null,\"rotationCenterX\":0,\"rotationCenterY\":0}],\"sounds\":[],\"volume\":100,\"layerOrder\":1,\"visible\":true,\"x\":0,\"y\":0,\"size\":100,\"direction\":90,\"draggable\":false,\"rotationStyle\":\"all around\"}], \"meta\": {\"semver\": \"3.0.0\", \"vm\": \"0.2.0\", \"agent\": \"Mozilla\"}}";

        Scratch3Parser parser = new Scratch3Parser();
        Program program = parser.parseString("testproject", json);

        // Create output directory
        Path outputDir = tempDir.resolve("output");

        PyStageProjectExporter exporter = new PyStageProjectExporter();
        
        // This should not throw NullPointerException anymore
        assertDoesNotThrow(() -> exporter.export(sb3File, program, outputDir));
    }
}
