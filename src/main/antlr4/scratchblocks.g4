grammar scratchblocks;

/*
 * Parser Rules
 */

// Define the entry point for the parser
script          : (stmt | COMMENT)* EOF;  //wir sollten ein script als combination aus 0 oder 1 event und einer stmt list machen oder 0 event und ein expressionStmt

// Block rules
stmt           : motionStmt
                | looksStmt
                | soundStmt
                | eventStmt
                | controlStmt
                | sensingStmt
                | expressionStmt  // kann nur ein einzelnes davon geben, also evtl auf script ebene setzen
                | variableStmt
//               | customBlock
                ;

event           : 'when green flag clicked'
                | 'when ['key' v] key pressed'
                | 'when this sprite clicked'
                | 'when backdrop switches to ['STRING' v]'
                | 'when 'eventChoice' > 'expression
                | 'when I receive ['STRING' v]'
                | 'when I start as a clone'
                | 'when stage clicked'
                ;

stmtList        : (stmt)*;

motionStmt     : 'move 'expression' steps'
                | 'turn @turnRight 'expression' degrees'  //was hat es mit dem @turnRight auf sich? ist das nicht einfach right in scratchblocks?
                | 'turn @turnLeft 'expression' degrees'
                | 'go to 'position
                | 'go to x: 'expression' y: 'expression
                | 'glide 'expression' secs to 'position
                | 'glide 'expression' secs to x: 'expression' y: 'expression
                | 'point in direction 'expression
                | 'point towards 'position
                | 'change x by 'expression
                | 'set x to 'expression
                | 'change y by 'expression
                | 'set y to 'expression
                | 'if on edge, bounce'
                | 'set rotation style ['rotation' v]'
                ;

looksStmt      : 'say 'expression' for 'expression' seconds'
                | 'say 'expression
                | 'think 'expression' for 'expression' seconds'
                | 'think 'expression
                | 'switch costume to 'costumeSelect
                | 'next costume'
                | 'switch backdrop to 'backdropSelect
                | 'next backdrop'
                | 'change size by 'expression
                | 'set size to 'expression' %'
                | 'change ['colorEffect' v] effect by 'expression
                | 'set ['colorEffect' v] effect to 'expression
                | 'clear graphic effects'
                | 'show'
                | 'hide'
                | 'go to ['layerChoice' v] layer'
                | 'go ['forwardBackwardChoice' v] 'expression' layers'
                | 'switch backdrop to 'backdropSelect' and wait'
                ;

soundStmt      : 'play sound 'soundChoice' until done'
                | 'play sound 'soundChoice
                | 'stop all sounds'
                | 'change ['soundEffect' v] effect by 'expression
                | 'set ['soundEffect' v] effect to 'expression
                | 'clear sound effects'
                | 'change colume by 'expression
                | 'set colume to 'expression' %'
                ;

controlStmt    : 'wait 'expression' seconds'
                | 'repeat 'expression stmtList'end'
                | 'forever 'stmtList'end'
                | 'if 'boolExpression' then' stmtList'end'
                | 'if 'boolExpression' then' stmtList'else'stmtList'end'
                | 'wait until 'boolExpression
                | 'repeat until 'boolExpression stmtList'end'
                | 'stop ['stopChoice' v]'
                | 'create clone of 'cloneChoice
                | 'delete this clone'
                ;

eventStmt       : 'broadcast 'message
                | ' broadcast 'message' and wait'
                ;

sensingStmt     : 'ask 'expression' and wait'
                | 'set drag mode ['dragmode' v]'
                | 'reset timer'
                ;

expressionStmt  : expression;

variableStmt    : 'set ['STRING' v] to 'expression
                | 'change ['STRING' v] by 'expression
                | 'show variable ['STRING' v]'
                | 'hide variable ['STRING' v]'
                | 'add 'expression' to ['STRING' v]'
                | 'delete 'expression' of ['STRING' v]'
                | 'delete all of ['STRING' v]'
                | 'insert 'expression' at 'expression' of ['STRING' v]'
                | 'replace item 'expression' of ['STRING' v] with 'expression
                | 'show list ['STRING' v]'
                | 'hide list ['STRING' v]'
                ;

numLiteral      : '('NUMBER')';
stringLiteral   : '['STRING']';

position        : '('  fixedPosition' v)'
                | expression
                ;

fixedPosition   : 'random position'
                | 'mouse-pointer'
                | 'STRING'
                ;

rotation        : 'left-right'
                | 'don\'t rotate'
                | 'all around'
                ;

costumeSelect   : '('STRING' v)'
                | expression
                ;

backdropSelect  : '('STRING' v)'
                | 'next backdrop'
                | 'previous backdrop'
                | 'random backdrop'
                ;

colorEffect     : 'color'
                | 'fisheye'
                | 'whirl'
                | 'pixelate'
                | 'mosaic'
                | 'brightness'
                | 'ghost'
                ;

forwardBackwardChoice: 'forward'
                     | 'backward'
                     ;

layerChoice     : 'front'
                | 'back';

soundChoice     : '('STRING' v)'
                | expression
                ;

soundEffect     : 'pitch'
                | 'pan left/right'
                ;

stopChoice      : 'all'
                | 'this script'
                | 'other scripts in sprite'
                ;

cloneChoice     : '(myself v)'
                | '('STRING' v)'
                | expression
                ;

message         : '('STRING 'v)'
                | expression
                ;

dragmode        : 'draggable'
                | 'not draggable'
                ;

eventChoice     : 'loudness'
                | 'timer'
                ;

key             : 'space'
                | 'up arrow'
                | 'down arrow'
                | 'right arrow'
                | 'left arrow'
                | 'any'
                //TODO: other keys as regex?
                ;

expression      : numLiteral
                | stringLiteral
                | boolExpression
                | 'expression' //placeholder
                ;

boolExpression : 'bool'
               ;



/*
 * Lexer Rules
 */

NUMBER          : [0-9]+ ('.' [0-9]+)?;
STRING          : '"' ~["]* '"';
WS              : [ \t\r\n]+ -> skip;
COMMENT         : '//' ~[\r\n]* ;
