grammar Scratchblocks;

/*
 * Parser Rules
 */

// Define the entry point for the parser
actor           : scriptList (COMMENT)? EOF;

scriptList      : (script)*;

script          : event (COMMENT)?
                | (event (COMMENT)?)? stmt stmtList
                | expressionStmt (COMMENT)?
                | customBlock
                ;

customBlock     : 'define 'STRING (parameter)*;

parameter       : boolParam
                | stringParam
                ;

boolParam       : '<'STRING'>';
stringParam     : '('STRING')';

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

event           : greenFlag
                | keyEvent
                | spriteClicked
                | backDropSwitchEvent
                | biggerEvent
                | receptionMessage
                | startAsClone
                | stageClicked
                ;

greenFlag       : 'when green flag clicked';
keyEvent        : 'when ['key' v] key pressed';
spriteClicked   : 'when this sprite clicked';
stageClicked    : 'when stage clicked';
startAsClone    : 'when I start as a clone';
receptionMessage: 'when I receive ['STRING' v]';
biggerEvent     : 'when 'eventChoice' > 'exprOrLiteral;
backDropSwitchEvent: 'when backdrop switches to ['STRING' v]';

stmtList        : (stmt)*;

motionStmt     : moveSteps
                | turnRight
                | turnLeft
                | goToPos
                | goToPosXY
                | glideToPos
                | glideToPosXY
                | pointInDir
                | pointTowards
                | changeX
                | setX
                | changeY
                | setY
                | onEdge
                | setRotation
                ;

moveSteps       : 'move 'exprOrLiteral' steps';
turnRight       : 'turn right 'exprOrLiteral' degrees';
turnLeft        : 'turn left 'exprOrLiteral' degrees';
goToPos         : 'go to 'position;
goToPosXY       : 'go to x: 'exprOrLiteral' y: 'exprOrLiteral;
glideToPos      : 'glide 'exprOrLiteral' secs to 'position;
glideToPosXY    : 'glide 'exprOrLiteral' secs to x: 'exprOrLiteral' y: 'exprOrLiteral;
pointInDir      : 'point in direction 'exprOrLiteral;
pointTowards    : 'point towards 'position;
changeX         : 'change x by 'exprOrLiteral;
setX            : 'set x to 'exprOrLiteral;
changeY         : 'change y by 'exprOrLiteral;
setY            : 'set y to 'exprOrLiteral;
onEdge          : 'if on edge, bounce';
setRotation     : 'set rotation style ['rotation' v]';

looksStmt      : saySeconds
                | say
                | thinkSeconds
                | think
                | switchCostume
                | nextCostume
                | switchBackdrop
                | nextBackdrop
                | changeSize
                | setSize
                | changeColorEffect
                | setColorEffect
                | clearColorEffect
                | show
                | hide
                | goToLayer
                | goForwardBackwardLayer
                | switchBackdropWait
                ;

saySeconds      : 'say 'exprOrLiteral' for 'exprOrLiteral' seconds';
say             : 'say 'exprOrLiteral;
thinkSeconds    : 'think 'exprOrLiteral' for 'exprOrLiteral' seconds';
think           : 'think 'exprOrLiteral;
switchCostume   : 'switch costume to 'costumeSelect;
nextCostume     : 'next costume';
switchBackdrop  : 'switch backdrop to 'backdropSelect;
nextBackdrop    : 'next backdrop';
changeSize      : 'change size by 'exprOrLiteral;
setSize         : 'set size to 'exprOrLiteral' %';
changeColorEffect: 'change ['colorEffect' v] effect by 'exprOrLiteral;
setColorEffect  : 'set ['colorEffect' v] effect to 'exprOrLiteral;
clearColorEffect: 'clear graphic effects';
show            : 'show';
hide            : 'hide';
goToLayer       : 'go to ['layerChoice' v] layer';
goForwardBackwardLayer: 'go ['forwardBackwardChoice' v] 'exprOrLiteral' layers';
switchBackdropWait: 'switch backdrop to 'backdropSelect' and wait';

soundStmt      : playSoundDone
                | playSound
                | stopSound
                | changeSoundEffect
                | setSoundEffect
                | clearSoundEffect
                | changeVolume
                | setVolume
                ;

playSoundDone   : 'play sound 'soundChoice' until done';
playSound       : 'play sound 'soundChoice;
stopSound       : 'stop all sounds';
changeSoundEffect: 'change ['soundEffect' v] effect by 'exprOrLiteral;
setSoundEffect  : 'set ['soundEffect' v] effect to 'exprOrLiteral;
clearSoundEffect: 'clear sound effects';
changeVolume    : 'change volume by 'exprOrLiteral;
setVolume       : 'set volume to 'exprOrLiteral' %';

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

