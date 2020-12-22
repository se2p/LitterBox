/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.clonedetection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CloneAnalysisTest {

    @Test
    public void testIdentity() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/duplicatedScript.json");
        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        Script script = actor.getScripts().getScriptList().get(0);
        assertEquals(script, script);

        CloneAnalysis cloneAnalysis = new CloneAnalysis(actor, 3, 2);
        Set<CodeClone> clones = cloneAnalysis.check(script, script, CodeClone.CloneType.TYPE1);
        assertEquals(0, clones.size());
    }

    @Test
    public void testDuplicatedScript() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/duplicatedScript.json");
        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        Script script1 = actor.getScripts().getScriptList().get(0);
        Script script2 = actor.getScripts().getScriptList().get(1);
        assertEquals(script1, script2);

        CloneAnalysis cloneAnalysis = new CloneAnalysis(actor);
        Set<CodeClone> clones = cloneAnalysis.check(script1, script2, CodeClone.CloneType.TYPE1);
        assertEquals(1, clones.size());
        assertEquals(6, clones.iterator().next().size());
    }

    @Test
    public void testDuplicatedScriptDifferentLiteralsAndVariables() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/codecloneliteralsvariables.json");
        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        Script script1 = actor.getScripts().getScriptList().get(0);
        Script script2 = actor.getScripts().getScriptList().get(1);
        assertNotEquals(script1, script2);

        CloneAnalysis cloneAnalysis = new CloneAnalysis(actor, 3, 2);
        Set<CodeClone> type1Clones = cloneAnalysis.check(script1, script2, CodeClone.CloneType.TYPE1);
        Set<CodeClone> type2Clones = cloneAnalysis.check(script1, script2, CodeClone.CloneType.TYPE2);
        Set<CodeClone> type3Clones = cloneAnalysis.check(script1, script2, CodeClone.CloneType.TYPE3);
        assertEquals(0, type1Clones.size());
        assertEquals(1, type2Clones.size());
        assertEquals(0, type3Clones.size());
        assertEquals(4, type2Clones.iterator().next().size());
    }

    @Test
    public void testSubsequenceClone() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/codeclonesubsequence.json");
        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        Script script1 = actor.getScripts().getScriptList().get(0);
        Script script2 = actor.getScripts().getScriptList().get(1);
        assertNotEquals(script1, script2);

        CloneAnalysis cloneAnalysis = new CloneAnalysis(actor, 3 , 2);
        Set<CodeClone> clones = cloneAnalysis.check(script1, script2, CodeClone.CloneType.TYPE2);
        assertEquals(1, clones.size());
        assertEquals(4, clones.iterator().next().size());
    }

    @Test
    public void testVariableClone() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/codeclonevariableblocks.json");
        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        Script script1 = actor.getScripts().getScriptList().get(0);
        Script script2 = actor.getScripts().getScriptList().get(1);
        assertNotEquals(script1, script2);

        CloneAnalysis cloneAnalysis = new CloneAnalysis(actor, 3, 2);
        Set<CodeClone> clones = cloneAnalysis.check(script1, script2, CodeClone.CloneType.TYPE2);
        assertEquals(1, clones.size());
        assertEquals(4, clones.iterator().next().size());
    }

    @Test
    public void testListClone() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/codeclonelistblocks.json");
        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        Script script1 = actor.getScripts().getScriptList().get(0);
        Script script2 = actor.getScripts().getScriptList().get(1);
        assertNotEquals(script1, script2);

        CloneAnalysis cloneAnalysis = new CloneAnalysis(actor);
        Set<CodeClone> clones = cloneAnalysis.check(script1, script2, CodeClone.CloneType.TYPE2);
        assertEquals(1, clones.size());
        assertEquals(11, clones.iterator().next().size());
    }

    @Test
    public void testScriptWithItself() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/cloneScriptWithItself.json");
        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        Script script1 = actor.getScripts().getScriptList().get(0);

        CloneAnalysis cloneAnalysis = new CloneAnalysis(actor);
        Set<CodeClone> clonesType = cloneAnalysis.check(script1, CodeClone.CloneType.TYPE1);
        assertEquals(1, clonesType.size());
        assertEquals(7, clonesType.iterator().next().size());
    }

    @Test
    public void testMinimumCloneSizeIs6() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/cloneTypeMinSize.json");
        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        Script script1 = actor.getScripts().getScriptList().get(0);
        Script script2 = actor.getScripts().getScriptList().get(1);
        assertNotEquals(script1, script2);

        CloneAnalysis cloneAnalysis = new CloneAnalysis(actor);
        Set<CodeClone> clonesType = cloneAnalysis.check(script1, script2, CodeClone.CloneType.TYPE1);
        assertEquals(1, clonesType.size());
        assertEquals(6, clonesType.iterator().next().size());
    }

    @Test
    public void testCloneType2() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/cloneType2.json");
        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        Script script1 = actor.getScripts().getScriptList().get(0);
        Script script2 = actor.getScripts().getScriptList().get(1);
        assertNotEquals(script1, script2);

        CloneAnalysis cloneAnalysis = new CloneAnalysis(actor, 3, 2);
        Set<CodeClone> clonesType = cloneAnalysis.check(script1, script2, CodeClone.CloneType.TYPE2);
        assertEquals(1, clonesType.size());
        assertEquals(4, clonesType.iterator().next().size());
    }

    @Test
    public void testCloneType1And2InOneScript() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/cloneType1And2.json");
        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        Script script1 = actor.getScripts().getScriptList().get(0);
        Script script2 = actor.getScripts().getScriptList().get(1);
        assertNotEquals(script1, script2);

        CloneAnalysis cloneAnalysis = new CloneAnalysis(actor, 3, 2);
        Set<CodeClone> clonesType1 = cloneAnalysis.check(script1, script2, CodeClone.CloneType.TYPE1);
        Set<CodeClone> clonesType2 = cloneAnalysis.check(script1, script2, CodeClone.CloneType.TYPE2);
        assertEquals(1, clonesType1.size());
        assertEquals(4, clonesType1.iterator().next().size());
        assertEquals(2, clonesType2.size());
        assertEquals(4, clonesType2.iterator().next().size());
    }

    @Test
    public void testCloneType3WithGap() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/cloneType3WithGap.json");
        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        Script script1 = actor.getScripts().getScriptList().get(0);
        Script script2 = actor.getScripts().getScriptList().get(1);
        assertNotEquals(script1, script2);

        CloneAnalysis cloneAnalysis = new CloneAnalysis(actor);
        Set<CodeClone> clonesType1 = cloneAnalysis.check(script1, script2, CodeClone.CloneType.TYPE1);
        Set<CodeClone> clonesType2 = cloneAnalysis.check(script1, script2, CodeClone.CloneType.TYPE2);
        Set<CodeClone> clonesType3 = cloneAnalysis.check(script1, script2, CodeClone.CloneType.TYPE3);
        assertEquals(1, clonesType1.size());
        assertEquals(0, clonesType2.size());
        assertEquals(1, clonesType3.size());
        assertEquals(10, clonesType3.iterator().next().size());
    }

    @Test
    public void testCustomBlockClone() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/codeclonecustomblock.json");
        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        Script script = actor.getScripts().getScriptList().get(0);
        ProcedureDefinition procedure = actor.getProcedureDefinitionList().getList().get(0);

        CloneAnalysis cloneAnalysis = new CloneAnalysis(actor, 3, 2);
        Set<CodeClone> clones = cloneAnalysis.check(script, procedure, CodeClone.CloneType.TYPE1);
        assertEquals(1, clones.size());
        assertEquals(4, clones.iterator().next().size());
    }

    private Program getAST(String fileName) throws IOException, ParsingException {
        File file = new File(fileName);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode project = objectMapper.readTree(file);
        Program program = ProgramParser.parseProgram("TestProgram", project);
        return program;
    }
}
