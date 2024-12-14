/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
grammar ScratchBlocksGrammar;

/*
 * Parser Rules
 */

// Define the entry point for the parser
program                 : actorList EOF
                        | scriptList EOF
                        | EOF
                        ;

actorList               : (actor)+;

actor                   : BEGIN_ACTOR scriptList NEWLINE*;


scriptList              : (script (NEWLINE+)?)*;

script                  : expressionStmt NEWLINE?
                        | customBlock NEWLINE?
                        | event NEWLINE (stmtList)?
                        | stmtList
                        ;

customBlock             : 'define 'stringArgument (parameter)* (COMMENT)?;

parameter               : boolParam
                        | stringParam
                        ;

boolParam               : '<'stringArgument'>';
stringParam             : '('stringArgument')';

// Block rules
stmt                    : motionStmt (COMMENT)?
                        | looksStmt (COMMENT)?
                        | soundStmt (COMMENT)?
                        | eventStmt (COMMENT)?
                        | controlStmt (COMMENT)?
                        | sensingStmt (COMMENT)?
                        | variableStmt (COMMENT)?
                        | stringArgument (exprOrLiteral)+ (COMMENT)?                            //custom block call
                        | ~(NEWLINE|'//'|BEGIN_ACTOR)~(NEWLINE)+? (exprOrLiteral)* (COMMENT)?   //custom block call
                        ;



stmtList                : (stmt NEWLINE)+;

event                   : greenFlag (COMMENT)?
                        | keyEvent (COMMENT)?
                        | spriteClicked (COMMENT)?
                        | backDropSwitchEvent (COMMENT)?
                        | biggerEvent (COMMENT)?
                        | receptionMessage (COMMENT)?
                        | startAsClone (COMMENT)?
                        | stageClicked (COMMENT)?
                        ;

greenFlag               : 'when green flag clicked';
keyEvent                : 'when ['key' v] key pressed';
spriteClicked           : 'when this sprite clicked';
stageClicked            : 'when stage clicked';
startAsClone            : 'when I start as a clone';
receptionMessage        : 'when I receive ['stringArgument' v]';
biggerEvent             : 'when 'eventChoice' > 'exprOrLiteral;
backDropSwitchEvent     : 'when backdrop switches to ['stringArgument' v]';

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
goToPosXY               : 'go to x: 'x=exprOrLiteral' y: 'y=exprOrLiteral;
glideToPos              : 'glide 'time=exprOrLiteral' secs to 'position;
glideToPosXY            : 'glide 'time=exprOrLiteral' secs to x: 'x=exprOrLiteral' y: 'y=exprOrLiteral;
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

saySeconds              : 'say 'text=exprOrLiteral' for 'time=exprOrLiteral' seconds';
say                     : 'say 'exprOrLiteral;
thinkSeconds            : 'think 'text=exprOrLiteral' for 'time=exprOrLiteral' seconds';
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
playSound               : 'start sound 'soundChoice;
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
repeat                  : 'repeat 'exprOrLiteral (stmtList)?'end';
forever                 : 'forever' NEWLINE (stmtList)? 'end';
if                      : 'if 'exprOrLiteral' then' NEWLINE (stmtList)? 'end';
ifElse                  : 'if 'exprOrLiteral' then' NEWLINE (then=stmtList)? 'else' NEWLINE (else=stmtList)? 'end';
waitUntil               : 'wait until 'exprOrLiteral;
repeatUntil             : 'repeat until 'exprOrLiteral NEWLINE (stmtList)? 'end';
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

expressionStmt          : expression (COMMENT)?;

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

setVar                  : 'set ['stringArgument' v] to 'exprOrLiteral;
changeVar               : 'change ['stringArgument' v] by 'exprOrLiteral;
showVar                 : 'show variable ['stringArgument' v]';
hideVar                 : 'hide variable ['stringArgument' v]';
addToList               : 'add 'exprOrLiteral' to ['stringArgument' v]';
deleteFromList          : 'delete 'exprOrLiteral' of ['stringArgument' v]';
deleteAllOfList         : 'delete all of ['stringArgument' v]';
insertToList            : 'insert 'insertion=exprOrLiteral' at 'location=exprOrLiteral' of ['stringArgument' v]';
replaceItemInList       : 'replace item 'oldItem=exprOrLiteral' of ['stringArgument' v] with 'newItem= exprOrLiteral;
showList                : 'show list ['stringArgument' v]';
hideList                : 'hide list ['stringArgument' v]';

position                : '('fixedPosition' v)'
                        | exprOrLiteral
                        ;

fixedPosition           : 'random position'
                        | mousePointer
                        | stringArgument
                        ;

rotation                : 'left-right'
                        | 'don\'t rotate'
                        | 'all around'
                        ;

costumeSelect           : '('stringArgument' v)' //costume
                        | exprOrLiteral
                        ;

backdropSelect          : '('fixedBackdrop' v)'
                        | '('stringArgument' v)' //backdrop
                        | exprOrLiteral
                        ;


fixedBackdrop           : 'next backdrop'
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

soundChoice             : '('stringArgument' v)' //sound
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
                        | '('stringArgument' v)' //sprite
                        | exprOrLiteral
                        ;

message                 : '('stringArgument' v)' //message
                        | exprOrLiteral
                        ;

dragmode                : 'draggable'
                        | 'not draggable'
                        ;

eventChoice             : 'loudness'
                        | 'timer'
                        ;

keySelect               : '('key' v)'
                        | exprOrLiteral;

key                     : 'space'
                        | 'up arrow'
                        | 'down arrow'
                        | 'right arrow'
                        | 'left arrow'
                        | 'any'
                        | 'a'
                        | 'b'
                        | 'c'
                        | 'd'
                        | 'e'
                        | 'f'
                        | 'g'
                        | 'h'
                        | 'i'
                        | 'j'
                        | 'k'
                        | 'l'
                        | 'm'
                        | 'n'
                        | 'o'
                        | 'p'
                        | 'q'
                        | 'r'
                        | 's'
                        | 't'
                        | 'u'
                        | 'v'
                        | 'w'
                        | 'x'
                        | 'y'
                        | 'z'
                        | DIGIT
                        ;

exprOrLiteral           : numLiteral
                        | stringLiteral
                        | expression
                        ;

numLiteral              : '('(NUMBER|DIGIT)')';
stringLiteral           : '['stringArgument']';

expression              : '('numExpr')'
                        | '<'boolExpr'>'
                        | '('stringArgument')'//variable
                        ;

boolExpr                : touching
                        | touchingColor
                        | colorTouchingColor
                        | keyPressed
                        | mouseDown
                        | greaterThan
                        | equal
                        | lessThan
                        | and
                        | or
                        | not
                        | contains
                        | listContains
                        ;


touching                : 'touching 'touchingChoice'?';
touchingColor           : 'touching color 'touchingColorChoice'?';
colorTouchingColor      : 'color 'firstColor=touchingColorChoice' is touching 'secondColor=touchingColorChoice'?';
keyPressed              : 'key 'keySelect' pressed?';
mouseDown               : 'mouse down?';
greaterThan             : firstExpr=exprOrLiteral' > 'secondExpr=exprOrLiteral;
equal                   : firstExpr=exprOrLiteral' = 'secondExpr=exprOrLiteral;
lessThan                : firstExpr=exprOrLiteral' < 'secondExpr=exprOrLiteral;
and                     : firstExpr=exprOrLiteral' and 'secondExpr=exprOrLiteral;
or                       : firstExpr=exprOrLiteral' or 'secondExpr=exprOrLiteral;
not                     : 'not 'exprOrLiteral;
contains                : firstExpr=exprOrLiteral' contains 'secondExpr=exprOrLiteral'?';
listContains          : '['stringArgument' v] contains 'exprOrLiteral'?';

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
                        | lengthOfList
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
actorAttribute          : '['attributeChoice' v] of 'element;
currentTime             : 'current ['currentChoice' v]';
daysSince               : 'days since 2000';
userName                : 'username';
addition                : firstExpr=exprOrLiteral WS? '+' WS? secondExpr=exprOrLiteral;
subtraction             : firstExpr=exprOrLiteral WS? '-' WS? secondExpr=exprOrLiteral;
multiplication          : firstExpr=exprOrLiteral WS? '*' WS? secondExpr=exprOrLiteral;
division                : firstExpr=exprOrLiteral WS? '/' WS? secondExpr=exprOrLiteral;
pickRandom              : 'pick random 'firstExpr=exprOrLiteral' to 'secondExpr=exprOrLiteral;
join                    : 'join 'firstExpr=exprOrLiteral secondExpr=exprOrLiteral;
getLetterAtIndex        : 'letter 'firstExpr=exprOrLiteral' of 'secondExpr=exprOrLiteral;
lengthOf                : 'length of 'exprOrLiteral;
modulo                  : firstExpr=exprOrLiteral' mod 'secondExpr=exprOrLiteral;
round                   : 'round 'exprOrLiteral;
mathFunction            : '['mathChoice' v] of 'exprOrLiteral;
itemAtIndex             : 'item 'exprOrLiteral' of ['stringArgument' v]';
indexOfItem             : 'item # of 'exprOrLiteral' in ['stringArgument' v]';
lengthOfList            : 'length of ['stringArgument' v]';

element                 : '('stringArgument' v)'
                        | exprOrLiteral;

distanceChoice          : '('mousePointer' v)'
                        | '('stringArgument' v)'
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

attributeChoice         : fixedAttribute
                        | stringArgument //variable
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
                        | '('stringArgument' v)'
                        | exprOrLiteral
                        ;

mousePointer            : 'mouse-pointer';

fixedTouching           : mousePointer
                        | 'edge'
                        ;

touchingColorChoice     :'(' HEX ')'
                        | exprOrLiteral
                        ;

stringArgument          : ~(NEWLINE)*?;

/*
 * Lexer Rules
 */

fragment HEX_DIGIT      : [0-9a-fA-F];

DIGIT                   : [0-9];

NUMBER                  : '-'? (DIGIT)+ ('.' (DIGIT)+)?;

NEWLINE                 : '\r\n' | '\n' ;

WS                      : [ \t]+;

BEGIN_ACTOR             : '//;Act ' ~[\r\n]+ NEWLINE;

COMMENT                 : '//' ~[\r\n]* NEWLINE;

HEX                     : '#' (HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
                        | HEX_DIGIT HEX_DIGIT HEX_DIGIT) ;

ANY                     : .;
