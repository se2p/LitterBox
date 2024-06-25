grammar Scratchblocks;

/*
 * Parser Rules
 */

// Define the entry point for the parser
actor                   : scriptList (COMMENT)? EOF;

scriptList              : (script)*;

script                  : event (COMMENT)?
                        | (event (COMMENT)?)? stmt stmtList
                        | expressionStmt (COMMENT)?
                        | customBlock
                        ;

customBlock             : 'define 'STRING (parameter)*;

parameter               : boolParam
                        | stringParam
                        ;

boolParam               : '<'STRING'>';
stringParam             : '('STRING')';

// Block rules
stmt                    : motionStmt (COMMENT)?
                        | looksStmt (COMMENT)?
                        | soundStmt (COMMENT)?
                        | eventStmt (COMMENT)?
                        | controlStmt (COMMENT)?
                        | sensingStmt (COMMENT)?
                        | variableStmt (COMMENT)?
                        | STRING (exprOrLiteral)* //custom block call
                        ;

event                   : greenFlag
                        | keyEvent
                        | spriteClicked
                        | backDropSwitchEvent
                        | biggerEvent
                        | receptionMessage
                        | startAsClone
                        | stageClicked
                        ;

greenFlag               : 'when green flag clicked';
keyEvent                : 'when ['key' v] key pressed';
spriteClicked           : 'when this sprite clicked';
stageClicked            : 'when stage clicked';
startAsClone            : 'when I start as a clone';
receptionMessage        : 'when I receive ['STRING' v]';
biggerEvent             : 'when 'eventChoice' > 'exprOrLiteral;
backDropSwitchEvent     : 'when backdrop switches to ['STRING' v]';

stmtList                : (stmt)*;

motionStmt              : moveSteps
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

moveSteps               : 'move 'exprOrLiteral' steps';
turnRight               : 'turn right 'exprOrLiteral' degrees';
turnLeft                : 'turn left 'exprOrLiteral' degrees';
goToPos                 : 'go to 'position;
goToPosXY               : 'go to x: 'exprOrLiteral' y: 'exprOrLiteral;
glideToPos              : 'glide 'exprOrLiteral' secs to 'position;
glideToPosXY            : 'glide 'exprOrLiteral' secs to x: 'exprOrLiteral' y: 'exprOrLiteral;
pointInDir              : 'point in direction 'exprOrLiteral;
pointTowards            : 'point towards 'position;
changeX                 : 'change x by 'exprOrLiteral;
setX                    : 'set x to 'exprOrLiteral;
changeY                 : 'change y by 'exprOrLiteral;
setY                    : 'set y to 'exprOrLiteral;
onEdge                  : 'if on edge, bounce';
setRotation             : 'set rotation style ['rotation' v]';

looksStmt               : saySeconds
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

saySeconds              : 'say 'exprOrLiteral' for 'exprOrLiteral' seconds';
say                     : 'say 'exprOrLiteral;
thinkSeconds            : 'think 'exprOrLiteral' for 'exprOrLiteral' seconds';
think                   : 'think 'exprOrLiteral;
switchCostume           : 'switch costume to 'costumeSelect;
nextCostume             : 'next costume';
switchBackdrop          : 'switch backdrop to 'backdropSelect;
nextBackdrop            : 'next backdrop';
changeSize              : 'change size by 'exprOrLiteral;
setSize                 : 'set size to 'exprOrLiteral' %';
changeColorEffect       : 'change ['colorEffect' v] effect by 'exprOrLiteral;
setColorEffect          : 'set ['colorEffect' v] effect to 'exprOrLiteral;
clearColorEffect        : 'clear graphic effects';
show                    : 'show';
hide                    : 'hide';
goToLayer               : 'go to ['layerChoice' v] layer';
goForwardBackwardLayer  : 'go ['forwardBackwardChoice' v] 'exprOrLiteral' layers';
switchBackdropWait      : 'switch backdrop to 'backdropSelect' and wait';

soundStmt               : playSoundDone
                        | playSound
                        | stopSound
                        | changeSoundEffect
                        | setSoundEffect
                        | clearSoundEffect
                        | changeVolume
                        | setVolume
                        ;

playSoundDone           : 'play sound 'soundChoice' until done';
playSound               : 'play sound 'soundChoice;
stopSound               : 'stop all sounds';
changeSoundEffect       : 'change ['soundEffect' v] effect by 'exprOrLiteral;
setSoundEffect          : 'set ['soundEffect' v] effect to 'exprOrLiteral;
clearSoundEffect        : 'clear sound effects';
changeVolume            : 'change volume by 'exprOrLiteral;
setVolume               : 'set volume to 'exprOrLiteral' %';

controlStmt             : waitSeconds
                        | repeat
                        | forever
                        | if
                        | ifElse
                        | waitUntil
                        | repeatUntil
                        | stop
                        | createClone
                        | deleteClone
                        ;

waitSeconds             : 'wait 'exprOrLiteral' seconds';
repeat                  : 'repeat 'exprOrLiteral stmtList'end';
forever                 :  'forever 'stmtList'end';
if                      : 'if 'exprOrLiteral' then' stmtList'end';
ifElse                  : 'if 'exprOrLiteral' then' stmtList'else'stmtList'end';
waitUntil               : 'wait until 'exprOrLiteral;
repeatUntil             : 'repeat until 'exprOrLiteral stmtList'end';
stop                    : 'stop ['stopChoice' v]';
createClone             : 'create clone of 'cloneChoice;
deleteClone             : 'delete this clone';

eventStmt               : broadcast
                        | broadcastWait
                        ;

broadcast               : 'broadcast 'message;
broadcastWait           : 'broadcast 'message' and wait';

sensingStmt             : ask
                        | setDragMode
                        | resetTimer
                        ;

ask                     : 'ask 'exprOrLiteral' and wait';
setDragMode             : 'set drag mode ['dragmode' v]';
resetTimer              : 'reset timer';

expressionStmt          : expression;

variableStmt            : setVar
                        | changeVar
                        | showVar
                        | hideVar
                        | addToList
                        | deleteFromList
                        | deleteAllOfList
                        | insertToList
                        | replaceItemInList
                        | showList
                        | hideList
                        ;

setVar                  : 'set ['STRING' v] to 'exprOrLiteral;
changeVar               : 'change ['STRING' v] by 'exprOrLiteral;
showVar                 : 'show variable ['STRING' v]';
hideVar                 : 'hide variable ['STRING' v]';
addToList               : 'add 'exprOrLiteral' to ['STRING' v]';
deleteFromList          : 'delete 'exprOrLiteral' of ['STRING' v]';
deleteAllOfList         : 'delete all of ['STRING' v]';
insertToList            : 'insert 'exprOrLiteral' at 'exprOrLiteral' of ['STRING' v]';
replaceItemInList       : 'replace item 'exprOrLiteral' of ['STRING' v] with 'exprOrLiteral;
showList                : 'show list ['STRING' v]';
hideList                : 'hide list ['STRING' v]';

position                : '('fixedPosition' v)'
                        | exprOrLiteral
                        ;

fixedPosition           : 'random position'
                        | 'mouse-pointer'
                        | STRING
                        ;

rotation                : 'left-right'
                        | 'don\'t rotate'
                        | 'all around'
                        ;

costumeSelect           : '('STRING' v)' //costume
                        | exprOrLiteral
                        ;

backdropSelect          : '('STRING' v)' //backdrop
                        | 'next backdrop'
                        | 'previous backdrop'
                        | 'random backdrop'
                        ;

colorEffect             : 'color'
                        | 'fisheye'
                        | 'whirl'
                        | 'pixelate'
                        | 'mosaic'
                        | 'brightness'
                        | 'ghost'
                        ;

forwardBackwardChoice   : 'forward'
                        | 'backward'
                        ;

layerChoice             : 'front'
                        | 'back';

soundChoice             : '('STRING' v)' //sound
                        | exprOrLiteral
                        ;

soundEffect             : 'pitch'
                        | 'pan left/right'
                        ;

stopChoice              : 'all'
                        | 'this script'
                        | 'other scripts in sprite'
                        ;

cloneChoice             : '(myself v)'
                        | '('STRING' v)' //sprite
                        | exprOrLiteral
                        ;

message                 : '('STRING 'v)' //message
                        | exprOrLiteral
                        ;

dragmode                : 'draggable'
                        | 'not draggable'
                        ;

eventChoice             : 'loudness'
                        | 'timer'
                        ;

key                     : 'space'
                        | 'up arrow'
                        | 'down arrow'
                        | 'right arrow'
                        | 'left arrow'
                        | 'any'
                        | KEY_CHAR
                        | DIGIT
                        ;

exprOrLiteral           : numLiteral
                        | stringLiteral
                        |  expression
                        ;

numLiteral              : '('NUMBER')';
stringLiteral           : '['STRING']';

expression              : '('numExpr')'
                        | '<'boolExpr'>'
                        | '('STRING')'//variable
                        ;

boolExpr                : touching
                        | touchingColor
                        | colorTouchingColor
                        | keyPressed
                        | mouseDown
                        | greaterThan
                        | equal
                        | not
                        | contains
                        | stringContains
                        ;


touching                : 'touching 'touchingChoice'?';
touchingColor           : 'touching color 'touchingColorChoice'?';
colorTouchingColor      : 'color 'touchingColorChoice' is touching 'touchingColorChoice'?';
keyPressed              : 'key 'key' pressed?';
mouseDown               : 'mouse down?';
greaterThan             : exprOrLiteral' > 'exprOrLiteral;
equal                   : exprOrLiteral' = 'exprOrLiteral;
not                     : 'not 'exprOrLiteral;
contains                : exprOrLiteral' contains 'exprOrLiteral'?';
stringContains          : '['STRING' v] contains 'exprOrLiteral'?';

numExpr                 : xPosition
                        | yPosition
                        | direction
                        | numCostume
                        | numBackdrop
                        | size
                        | volume
                        | distanceTo
                        | answer
                        | mouseX
                        | mouseY
                        | loudness
                        | timer
                        | actorAttribute
                        | currentTime
                        | daysSince
                        | userName
                        | addition
                        | subtraction
                        | multiplication
                        | division
                        | pickRandom
                        | join
                        | getLetterAtIndex
                        | lengthOf
                        | modulo
                        | round
                        | mathFunction
                        | itemAtIndex
                        | indexOfItem
                        | lengtOfList
                        ;

xPosition               : 'x position';
yPosition               : 'y position';
direction               : 'direction';
numCostume              : 'costume ['nameNum' v]';
numBackdrop             : 'backdrop ['nameNum' v]';
size                    : 'size';
volume                  : 'volume';
distanceTo              : 'distance to 'distanceChoice;
answer                  : 'answer';
mouseX                  : 'mouse x';
mouseY                  : 'mouse y';
loudness                : 'loudness';
timer                   : 'timer';
actorAttribute          : attributeChoice' of ('STRING' v)';
currentTime             : 'current 'currentChoice;
daysSince               : 'days since 2000';
userName                : 'username';
addition                : exprOrLiteral' + 'exprOrLiteral;
subtraction             : exprOrLiteral' - 'exprOrLiteral;
multiplication          : exprOrLiteral' * 'exprOrLiteral;
division                : exprOrLiteral' / 'exprOrLiteral;
pickRandom              : 'pick random 'exprOrLiteral' to 'exprOrLiteral;
join                    : 'join 'exprOrLiteral exprOrLiteral;
getLetterAtIndex        : 'letter 'exprOrLiteral' of 'exprOrLiteral;
lengthOf                : 'length of 'exprOrLiteral;
modulo                  : exprOrLiteral' mod 'exprOrLiteral;
round                   : 'round 'exprOrLiteral;
mathFunction            : mathChoice' of 'exprOrLiteral;
itemAtIndex             : 'item 'exprOrLiteral' of ['STRING' v]';
indexOfItem             : 'item # of 'exprOrLiteral' in ['STRING' v]';
lengtOfList             : 'length of ['STRING' v]';


distanceChoice          : '(mouse-pointer v)'
                        | '('STRING' v)'
                        | exprOrLiteral
                        ;

nameNum                 : 'number'
                        | 'name'
                        ;

currentChoice           : 'year'
                        | 'month'
                        | 'date'
                        | 'day of the week'
                        | 'hour'
                        | 'minute'
                        | 'second'
                        ;

mathChoice              : 'abs'
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

attributeChoice         : '('STRING' v)' //variable
                        | '('fixedAttribute' v)'
                        | exprOrLiteral
                        ;

fixedAttribute          : 'backdrop #'
                        | 'backdrop name'
                        | 'volume'
                        | 'x position'
                        | 'y position'
                        | 'direction'
                        | 'costume #'
                        | 'costume name'
                        | 'size'
                        ;

touchingChoice          : '('fixedTouching' v)'
                        | '('STRING' v)'
                        | exprOrLiteral
                        ;

fixedTouching           : 'any'
                        | 'edge'
                        ;

touchingColorChoice     : exprOrLiteral
                        | '(' HEX ')'
                        ;

/*
 * Lexer Rules
 */

fragment HEX_DIGIT      : [0-9a-fA-F];

DIGIT                   : [0-9];

NUMBER                  : (DIGIT)+ ('.' (DIGIT)+)?;

KEY_CHAR                : [a-z];

STRING                  : '"' ~["]* '"';

WS                      : [ \t\r\n]+ -> skip;

COMMENT                 : '//' ~[\r\n]* ;

HEX                     : '#' (HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
                        | HEX_DIGIT HEX_DIGIT HEX_DIGIT) ;

