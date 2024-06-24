grammar scratchblocks;

/*
 * Parser Rules
 */

// Define the entry point for the parser
actor           : scriptList (COMMENT)? EOF;

scriptList      : (script)*;

script          : event (COMMENT)?
                | (event (COMMENT)?)? stmt stmtList
                | expressionStmt (COMMENT)?
                | 'define 'STRING (parameter)* //custom block
                ;

parameter       : '<'STRING'>'
                | '('STRING')'
                ;

// Block rules
stmt            : motionStmt (COMMENT)?
                | looksStmt (COMMENT)?
                | soundStmt (COMMENT)?
                | eventStmt (COMMENT)?
                | controlStmt (COMMENT)?
                | sensingStmt (COMMENT)?
                | variableStmt (COMMENT)?
                | STRING (exprOrLiteral)* //custom block call
                ;

event           : 'when green flag clicked'
                | 'when ['key' v] key pressed'
                | 'when this sprite clicked'
                | 'when backdrop switches to ['STRING' v]'
                | 'when 'eventChoice' > 'exprOrLiteral
                | 'when I receive ['STRING' v]'
                | 'when I start as a clone'
                | 'when stage clicked'
                ;

stmtList        : (stmt)*;

motionStmt     : 'move 'exprOrLiteral' steps'
                | 'turn right 'exprOrLiteral' degrees'
                | 'turn left 'exprOrLiteral' degrees'
                | 'go to 'position
                | 'go to x: 'exprOrLiteral' y: 'exprOrLiteral
                | 'glide 'exprOrLiteral' secs to 'position
                | 'glide 'exprOrLiteral' secs to x: 'exprOrLiteral' y: 'exprOrLiteral
                | 'point in direction 'exprOrLiteral
                | 'point towards 'position
                | 'change x by 'exprOrLiteral
                | 'set x to 'exprOrLiteral
                | 'change y by 'exprOrLiteral
                | 'set y to 'exprOrLiteral
                | 'if on edge, bounce'
                | 'set rotation style ['rotation' v]'
                ;

looksStmt      : 'say 'exprOrLiteral' for 'exprOrLiteral' seconds'
                | 'say 'exprOrLiteral
                | 'think 'exprOrLiteral' for 'exprOrLiteral' seconds'
                | 'think 'exprOrLiteral
                | 'switch costume to 'costumeSelect
                | 'next costume'
                | 'switch backdrop to 'backdropSelect
                | 'next backdrop'
                | 'change size by 'exprOrLiteral
                | 'set size to 'exprOrLiteral' %'
                | 'change ['colorEffect' v] effect by 'exprOrLiteral
                | 'set ['colorEffect' v] effect to 'exprOrLiteral
                | 'clear graphic effects'
                | 'show'
                | 'hide'
                | 'go to ['layerChoice' v] layer'
                | 'go ['forwardBackwardChoice' v] 'exprOrLiteral' layers'
                | 'switch backdrop to 'backdropSelect' and wait'
                ;

soundStmt      : 'play sound 'soundChoice' until done'
                | 'play sound 'soundChoice
                | 'stop all sounds'
                | 'change ['soundEffect' v] effect by 'exprOrLiteral
                | 'set ['soundEffect' v] effect to 'exprOrLiteral
                | 'clear sound effects'
                | 'change colume by 'exprOrLiteral
                | 'set colume to 'exprOrLiteral' %'
                ;

controlStmt    : 'wait 'exprOrLiteral' seconds'
                | 'repeat 'exprOrLiteral stmtList'end'
                | 'forever 'stmtList'end'
                | 'if 'exprOrLiteral' then' stmtList'end'
                | 'if 'exprOrLiteral' then' stmtList'else'stmtList'end'
                | 'wait until 'exprOrLiteral
                | 'repeat until 'exprOrLiteral stmtList'end'
                | 'stop ['stopChoice' v]'
                | 'create clone of 'cloneChoice
                | 'delete this clone'
                ;

eventStmt       : 'broadcast 'message
                | ' broadcast 'message' and wait'
                ;

sensingStmt     : 'ask 'exprOrLiteral' and wait'
                | 'set drag mode ['dragmode' v]'
                | 'reset timer'
                ;

expressionStmt  : expression;

variableStmt    : 'set ['STRING' v] to 'exprOrLiteral
                | 'change ['STRING' v] by 'exprOrLiteral
                | 'show variable ['STRING' v]'
                | 'hide variable ['STRING' v]'
                | 'add 'exprOrLiteral' to ['STRING' v]'
                | 'delete 'exprOrLiteral' of ['STRING' v]'
                | 'delete all of ['STRING' v]'
                | 'insert 'exprOrLiteral' at 'exprOrLiteral' of ['STRING' v]'
                | 'replace item 'exprOrLiteral' of ['STRING' v] with 'exprOrLiteral
                | 'show list ['STRING' v]'
                | 'hide list ['STRING' v]'
                ;

position        : '('fixedPosition' v)'
                | exprOrLiteral
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
                | exprOrLiteral
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
                | exprOrLiteral
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
                | exprOrLiteral
                ;

message         : '('STRING 'v)' //message
                | exprOrLiteral
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

exprOrLiteral      : '('NUMBER')'
                | '['STRING']'
                |  expression
                ;

expression      : '('numExpr')'
                | '<'boolExpr'>'
                | '('STRING')'//variable
                ;

boolExpr : 'touching 'touchingChoice'?'
         | 'touching color 'touchingColor'?'
         | 'color 'touchingColor' is touching 'touchingColor'?'
         | 'key 'key' pressed?'
         | 'mouse down?'
         | exprOrLiteral' > 'exprOrLiteral
         | exprOrLiteral' = 'exprOrLiteral
         | 'not 'exprOrLiteral
         | exprOrLiteral' contains 'exprOrLiteral'?'
         | '['STRING' v] contains 'exprOrLiteral'?'
         ;

numExpr     : 'x position'
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
            | exprOrLiteral' + 'exprOrLiteral
            | exprOrLiteral' - 'exprOrLiteral
            | exprOrLiteral' * 'exprOrLiteral
            | exprOrLiteral' / 'exprOrLiteral
            | 'pick random 'exprOrLiteral' to 'exprOrLiteral
            | 'join 'exprOrLiteral exprOrLiteral
            | 'letter 'exprOrLiteral' of 'exprOrLiteral
            | 'length of 'exprOrLiteral
            | exprOrLiteral' mod 'exprOrLiteral
            | 'round 'exprOrLiteral
            | mathChoice' of 'exprOrLiteral
            | 'item 'exprOrLiteral' of ['STRING' v]'
            | 'item # of 'exprOrLiteral' in ['STRING' v]'
            | 'length of ['STRING' v]'
            ;

distanceChoice  : '(mouse-pointer v)'
                | '('STRING' v)'
                exprOrLiteral
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
                | exprOrLiteral
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
                | exprOrLiteral
                ;

fixedTouching   : 'any'
                | 'edge'
                ;

touchingColor   : exprOrLiteral
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

