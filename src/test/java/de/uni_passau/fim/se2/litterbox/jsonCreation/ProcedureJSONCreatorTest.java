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
package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ProcedureJSONCreatorTest implements JsonTest {
    private Program procedure;

    @BeforeEach
    public void setUp() throws IOException, ParsingException {
        procedure = getAST("src/test/fixtures/metadata/procedureDefinition.json");
    }

    @Test
    public void testProcedureWithoutParam() {
        ActorDefinition fig1 = procedure.getActorDefinitionList().getDefinitions().get(1);
        String jsonString = ProcedureJSONCreator.createProcedureJSONString(fig1.getProcedureDefinitionList().getList().get(0), fig1.getIdent().getName(), procedure.getSymbolTable(), procedure.getProcedureMapping());
        Assertions.assertEquals("\"+-|[~F#Y=UN]Gx:/FpZK\": {\"opcode\": \"procedures_definition\",\"next\": null,\"parent\": null,\"inputs\": {\"custom_block\": [1,\"Q9B2dBWC+EXqV13*TGJ+\"]},\"fields\": {},\"shadow\": false,\"topLevel\": true,\"x\": 234.0,\"y\": 161.0},\"Q9B2dBWC+EXqV13*TGJ+\": {\"opcode\": \"procedures_prototype\",\"next\": null,\"parent\": \"+-|[~F#Y=UN]Gx:/FpZK\",\"inputs\": {},\"fields\": {},\"shadow\": true,\"topLevel\": false,\"mutation\": {\"tagName\": \"mutation\",\"children\": [],\"proccode\": \"TestMethode\",\"argumentids\": \"[]\",\"argumentnames\": \"[]\",\"argumentdefaults\": \"[]\",\"warp\": false}}"
                , jsonString);
    }

    @Test
    public void testProcedureWithParams() {
        ActorDefinition fig1 = procedure.getActorDefinitionList().getDefinitions().get(1);
        String jsonString = ProcedureJSONCreator.createProcedureJSONString(fig1.getProcedureDefinitionList().getList().get(1), fig1.getIdent().getName(), procedure.getSymbolTable(), procedure.getProcedureMapping());
        Assertions.assertEquals("\"bch@i8.w%Rl]%8L6ch5E\": {\"opcode\": \"procedures_definition\",\"next\": null,\"parent\": null,\"inputs\": {\"custom_block\": [1,\"[7{.Qd1;lR2`(BT47(g+\"]},\"fields\": {},\"shadow\": false,\"topLevel\": true,\"x\": 572.0,\"y\": 159.0},\"=l|2nJpb+DKNyKnN2vWJ\": {\"opcode\": \"argument_reporter_string_number\",\"next\": null,\"parent\": \"[7{.Qd1;lR2`(BT47(g+\",\"inputs\": {},\"fields\": {\"VALUE\": [\"number or text\",null]},\"shadow\": true,\"topLevel\": false},\"hHF#O59[Z;v$W1c1sEn[\": {\"opcode\": \"argument_reporter_boolean\",\"next\": null,\"parent\": \"[7{.Qd1;lR2`(BT47(g+\",\"inputs\": {},\"fields\": {\"VALUE\": [\"boolean\",null]},\"shadow\": true,\"topLevel\": false},\"[7{.Qd1;lR2`(BT47(g+\": {\"opcode\": \"procedures_prototype\",\"next\": null,\"parent\": \"bch@i8.w%Rl]%8L6ch5E\",\"inputs\": {\"`8Iq@3pq:v59F(H1l;Tv\": [1,\"=l|2nJpb+DKNyKnN2vWJ\"],\"R=Ak(#?BNe`pQ]2ZbY[C\": [1,\"hHF#O59[Z;v$W1c1sEn[\"]},\"fields\": {},\"shadow\": true,\"topLevel\": false,\"mutation\": {\"tagName\": \"mutation\",\"children\": [],\"proccode\": \"TestMit %s %b\",\"argumentids\": \"[\\\"`8Iq@3pq:v59F(H1l;Tv\\\",\\\"R=Ak(#?BNe`pQ]2ZbY[C\\\"]\",\"argumentnames\": \"[\\\"number or text\\\",\\\"boolean\\\"]\",\"argumentdefaults\": \"[\\\"\\\",\\\"false\\\"]\",\"warp\": false}}"
                , jsonString);
    }

    @Test
    public void testProcedureWithStmts() {
        ActorDefinition stage = procedure.getActorDefinitionList().getDefinitions().get(0);
        String jsonString = ProcedureJSONCreator.createProcedureJSONString(stage.getProcedureDefinitionList().getList().get(0), stage.getIdent().getName(), procedure.getSymbolTable(), procedure.getProcedureMapping());
        Assertions.assertEquals("\"=kNyJn`W.A5t$~BaYm5h\": {\"opcode\": \"procedures_definition\",\"next\": \"u2URKg;#M^stpx}L_8(Q\",\"parent\": null,\"inputs\": {\"custom_block\": [1,\":mEz1QI+6Kooz]~KHkcX\"]},\"fields\": {},\"shadow\": false,\"topLevel\": true,\"x\": 545.0,\"y\": 326.0},\":mEz1QI+6Kooz]~KHkcX\": {\"opcode\": \"procedures_prototype\",\"next\": null,\"parent\": \"=kNyJn`W.A5t$~BaYm5h\",\"inputs\": {},\"fields\": {},\"shadow\": true,\"topLevel\": false,\"mutation\": {\"tagName\": \"mutation\",\"children\": [],\"proccode\": \"TestBlock\",\"argumentids\": \"[]\",\"argumentnames\": \"[]\",\"argumentdefaults\": \"[]\",\"warp\": false}},\"u2URKg;#M^stpx}L_8(Q\": {\"opcode\": \"motion_ifonedgebounce\",\"next\": null,\"parent\": \"=kNyJn`W.A5t$~BaYm5h\",\"inputs\": {},\"fields\": {},\"shadow\": false,\"topLevel\": false}", jsonString);
    }
}
