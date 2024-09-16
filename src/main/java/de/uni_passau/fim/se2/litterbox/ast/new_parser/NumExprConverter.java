package de.uni_passau.fim.se2.litterbox.ast.new_parser;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawInput;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawTarget;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

final class NumExprConverter {
    static NumExpr convertNumExpr(
            final ProgramParserState state,
            final RawTarget target,
            final RawBlock.RawRegularBlock containingBlock,
            final RawInput exprBlock
    ) {
        throw new UnsupportedOperationException("todo");
    }
}
