package de.uni_passau.fim.se2.litterbox.ast.visitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.fail;

public class SpriteVisitorTest {

    private static JsonNode project;

    // @BeforeAll
    public static void setup() {
        String path = "/path/to/scratchproject.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }


    // @Test
    public void testSpriteVisitor() {
        ExtractSpriteVisitor visitor = new ExtractSpriteVisitor(false);
        try {
            Program program = ProgramParser.parseProgram("All", project);
            program.accept(visitor);

            Map<ActorDefinition, List<ASTNode>> leafsMap = visitor.getLeafsCollector();
            System.out.println("Anzahl der Keys: " + leafsMap.keySet().size());

            for(ASTNode key : leafsMap.keySet()) {
                for(ASTNode value : leafsMap.get(key)){
                    System.out.println("Blatt (Test): " + value.getUniqueName());
                }
            }

            /*
            List<ScriptList> scriptListList = visitor.getScriptList();
            for(ScriptList scriptList : scriptListList) {
                for(Script script : scriptList.getScriptList()) {

                    for(StmtList stmtList : script.getStmtList()){
                        for(Stmt stmt : stmtList.getStmts()){
                            //System.out.println(stmt.getUniqueName() + "  " + stmt.toString());
                        }
                    }

                    System.out.println(script.getStmtList());
                }
            }*/
        } catch (ParsingException e) {
            fail();
        }
    }

}
