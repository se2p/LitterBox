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
                | 'if 'expression' then' stmtList'end'
                | 'if 'expression' then' stmtList'else'stmtList'end'
                | 'wait until 'expression
                | 'repeat until 'expression stmtList'end'
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

position        : '('fixedPosition' v)'
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

costumeSelect   : '('STRING' v)' //costume
                | expression
                ;

backdropSelect  : '('STRING' v)' //backdrop
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

soundChoice     : '('STRING' v)' //sound
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
                | '('STRING' v)' //sprite
                | expression
                ;

message         : '('STRING 'v)' //message
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

expression      : '('numExpr')'
                | stringLiteral
                | '<'boolExpr'>'
                | '('STRING')' //variable
                ;

boolExpr : 'touching 'touchingChoice'?'
         | 'touching color 'touchingColor'?'
         | 'color 'touchingColor' is touching 'touchingColor'?'
         | 'key 'key' pressed?'
         | 'mouse down?'
         | expression' > 'expression
         | expression' = 'expression
         | 'not 'expression
         | expression' contains 'expression'?'
         | '['STRING' v] contains 'expression'?'
         ;

numExpr     : NUMBER
            | 'x position'
            | 'y position'
            | 'direction'
            | 'costume ['nameNum' v]'
            | 'backdrop ['nameNum' v]'
            | 'size'
            | 'volume'
            | 'distance to 'distanceChoice
            | 'answer'
            | 'mouse x'
            | 'mouse y'
            | 'loudness'
            | 'timer'
            | attributeChoice' of ('STRING' v)'
            | 'current 'currentChoice
            | 'days since 2000'
            | 'username'
            | expression' + 'expression
            | expression' - 'expression
            | expression' * 'expression
            | expression' / 'expression
            | 'pick random 'expression' to 'expression
            | 'join 'expression expression
            | 'letter 'expression' of 'expression
            | 'length of 'expression
            | expression' mod 'expression
            | 'round 'expression
            | mathChoice' of 'expression
            | 'item 'expression' of ['STRING' v]'
            | 'item # of 'expression' in ['STRING' v]'
            | 'length of ['STRING' v]'
            ;

stringLiteral   : '['STRING']';

distanceChoice  : '(mouse-pointer v)'
                | '('STRING' v)'
                expression
                ;

nameNum     : 'number'
            | 'name'
            ;

currentChoice   : 'year'
                | 'month'
                | 'date'
                | 'day of the week'
                | 'hour'
                | 'minute'
                | 'second'
                ;

mathChoice      : 'abs'
                | 'floor'
                | 'ceiling'
                | 'sqrt'
                | 'sin'
                | 'cos'
                | 'tan'
                | 'asin'
                | 'acos'
                | 'atan'
                | 'ln'
                | 'log'
                | 'e ^'
                | '10 ^'
                ;

attributeChoice : '('STRING' v)' //variable
                | '('fixedAttribute' v)'
                | expression
                ;

fixedAttribute  : 'backdrop #'
                | 'backdrop name'
                | 'volume'
                | 'x position'
                | 'y position'
                | 'direction'
                | 'costume #'
                | 'costume name'
                | 'size'
                ;

touchingChoice  : '('fixedTouching' v)'
                | '('STRING' v)'
                | expression
                ;

fixedTouching   : 'any'
                | 'edge'
                ;

touchingColor   : expression
                | '(' HEX ')'
                ;

/*
 * Lexer Rules
 */

fragment HEX_DIGIT : [0-9a-fA-F];

NUMBER          : [0-9]+ ('.' [0-9]+)?;
STRING          : '"' ~["]* '"';
WS              : [ \t\r\n]+ -> skip;
COMMENT         : '//' ~[\r\n]* ;
HEX: '#' (HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
         | HEX_DIGIT HEX_DIGIT HEX_DIGIT) ;

