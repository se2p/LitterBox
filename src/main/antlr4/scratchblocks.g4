grammar scratchblocks;

/*
 * Parser Rules
 */

// Define the entry point for the parser
script          : (block | COMMENT)* EOF;

// Block rules
block           : motionBlock
//              | looksBlock
//              | soundBlock
                | eventsBlock
//              | controlBlock
//              | sensingBlock
//              | operatorsBlock
//              | variablesBlock
//              | customBlock
                ;

motionBlock     : 'move 'numLiteral' steps'
                | 'turn @turnRight 'numLiteral' degrees'
                | 'turn @turnLeft 'numLiteral' degrees'
                | 'go to x: 'numLiteral' y: 'numLiteral
                ;

eventsBlock     : 'when @greenFlag clicked';

numLiteral      : ( 'NUMBER' );


/*
 * Lexer Rules
 */

NUMBER          : [0-9]+ ('.' [0-9]+)?;
STRING          : '"' ~["]* '"';
WS              : [ \t\r\n]+ -> skip;
COMMENT         : '//' ~[\r\n]* ;
