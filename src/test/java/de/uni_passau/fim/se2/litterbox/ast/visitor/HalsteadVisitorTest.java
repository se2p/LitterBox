package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class HalsteadVisitorTest implements JsonTest {


    @ParameterizedTest
    @CsvSource({"src/test/fixtures/cfg/greenflag.json,1,1,2,2",
                "src/test/fixtures/cfg/twogreenflags.json,2,2,4,3",
                "src/test/fixtures/cfg/onclick.json,1,1,2,2",
                "src/test/fixtures/cfg/ifthen.json,2,2,4,4",
                "src/test/fixtures/cfg/ifelse.json,3,2,5,4",
                "src/test/fixtures/cfg/repeattimes.json,2,1,3,3",
                "src/test/fixtures/cfg/repeatuntil.json,2,2,4,4",
                "src/test/fixtures/cfg/repeatforever.json,1,1,3,3",
                "src/test/fixtures/cfg/ifelse_repeattimes.json,5,2,7,5",
                "src/test/fixtures/cfg/twoevents.json,3,2,4,3",
                "src/test/fixtures/cfg/broadcastnoreceiver.json,1,1,2,2",
                "src/test/fixtures/cfg/receivewithoutbroadcast.json,2,2,2,2",
                "src/test/fixtures/cfg/broadcastreceive.json,3,2,4,4",
                "src/test/fixtures/cfg/receivetwomessages.json,6,4,7,5",
                "src/test/fixtures/cfg/cloneinit.json,1,1,2,2",
                "src/test/fixtures/cfg/createclone.json,1,1,2,2",
                "src/test/fixtures/cfg/createclone.json,1,1,2,2",
                "src/test/fixtures/cfg/createclonefromother.json,1,1,2,2",
                "src/test/fixtures/cfg/createtwoclones.json,2,2,3,2",
                "src/test/fixtures/cfg/createandinittwoclones.json,4,3,7,4",
                "src/test/fixtures/cfg/variable.json,5,3,4,4",
                "src/test/fixtures/cfg/twosprites.json,2,1,4,2",
                "src/test/fixtures/cfg/customblock.json,1,1,2,2",
                "src/test/fixtures/cfg/callcustomblock.json,1,1,4,4",
                "src/test/fixtures/cfg/customblocktwocalls.json,1,1,5,4",
                "src/test/fixtures/cfg/calledcustomblock.json,3,1,6,4",
                "src/test/fixtures/cfg/nextbackdroponstage.json,0,0,2,2",
                "src/test/fixtures/cfg/nextbackdroponsprite.json,1,1,2,2",
                "src/test/fixtures/cfg/emptyloop.json,1,1,2,2",
                "src/test/fixtures/cfg/listoperations.json,26,4,16,11",
                "src/test/fixtures/cfg/clonemyself.json,5,3,6,4",
                "src/test/fixtures/cfg/cloneother.json,5,3,6,4",
                "src/test/fixtures/cfg/clonevariable.json,5,3,6,4",
                "src/test/fixtures/cfg/cloneexpression.json,6,4,7,5",
                "src/test/fixtures/cfg/broadcastmessage.json,7,4,6,4",
                "src/test/fixtures/cfg/broadcastmessageandwait.json,7,4,6,4",
                "src/test/fixtures/cfg/broadcastvariable.json,7,5,6,4",
                "src/test/fixtures/cfg/broadcastvariableandwait.json,7,5,6,4",
                "src/test/fixtures/scratchblocks/mathExprInTimerBlock.json,6,5,5,5",
                "src/test/fixtures/scratchblocks/arithmeticblocks.json,41,7,32,11",
                "src/test/fixtures/scratchblocks/touchingallblocks.json,50,35,101,25",
                "src/test/fixtures/scratchblocks/askallblocks.json,47,33,60,24",
                "src/test/fixtures/scratchblocks/booleanblocks.json,22,5,23,10",
                "src/test/fixtures/scratchblocks/stringblocks.json,10,8,11,9",
                "src/test/fixtures/scratchblocks/multipleunconnectedblocks.json,2,2,3,3"
    })
    public void testHalsteadComponents(String fileName, int totalOperands, int uniqueOperands, int totalOperators, int uniqueOperators) throws IOException, ParsingException {
        HalsteadVisitor visitor = new HalsteadVisitor();
        Program program = getAST(fileName);
        program.accept(visitor);

        assertThat(visitor.getTotalOperands()).isEqualTo(totalOperands);
        assertThat(visitor.getUniqueOperands()).isEqualTo(uniqueOperands);
        assertThat(visitor.getTotalOperators()).isEqualTo(totalOperators);
        assertThat(visitor.getUniqueOperators()).isEqualTo(uniqueOperators);
    }

}
