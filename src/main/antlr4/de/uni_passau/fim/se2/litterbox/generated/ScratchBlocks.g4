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
grammar ScratchBlocks;

/*
 * Parser Rules
 */

/*
 * IMPORTANT: Do not use string literals that end with (,, ), [, ], <, or > as part of the parser rules.
 *
 * Such strings are implicitly converted into lexemes for the lexer. Since Antlr tries to find the longest possible
 * matches for these, then the lexer rules for LPAREN, LBRACK, … break in case a longer string can be matched.
 *
 * This is for example problematic for custom block call statements since they can contain arbitrary labels. E.g. in
 * case an implicit ` of [` lexer rule exists, the custom block call `Left (123) of [abc]` is no longer parsed as a
 * call of the block `Left %s of %s` with one number and one string literal as argument, but instead as the block
 * `Left %s of [abc]` with only one number argument.
 */

// Define the entry point for the parser
program                 : actorList EOF
                        | actorContent EOF
                        | EOF
                        ;

actorList               : (actor)+;

actor                   : BEGIN_ACTOR actorContent NEWLINE*;

actorContent            : ((customBlock | script) (((COMMENT? NEWLINE)+) | EOF))*?;

script                  : expressionStmt NEWLINE
                        | event NEWLINE (nonEmptyStmtList | stmtList)
                        | nonEmptyStmtList
                        ;

customBlock             : 'define ' customBlockParameter* suffix=stringArgument COMMENT? NEWLINE stmtList;

customBlockParameter    : stringArgument parameter;

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
                        | penStmt (COMMENT)?
                        | customBlockCallStmt
                        ;

customBlockCallStmt     : customBlockCallPrefix customBlockCallParam* WS* (COMMENT)?;

customBlockCallParam    : exprOrLiteral stringArgument;

customBlockCallPrefix   : (ESC||NUMBER~(NEWLINE|'//'|BEGIN_ACTOR|DELIM))(ESC|NUMBER|~(NEWLINE|DELIM))+?;

nonEmptyStmtList        : (WS* stmt NEWLINE)+?;

stmtList                : (WS* stmt NEWLINE)*?;

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
keyEvent                : 'when' WS '[' key' v] key pressed';
spriteClicked           : 'when this sprite clicked';
stageClicked            : 'when stage clicked';
startAsClone            : 'when I start as a clone';
receptionMessage        : 'when I receive' WS '['stringArgument' v]';
biggerEvent             : 'when' WS '[' eventChoice ' v] > 'exprOrLiteral;
backDropSwitchEvent     : 'when backdrop switches to' WS '[' stringArgument' v]';

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
goToPos                 : 'go to' WS position;
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
setRotation             : 'set rotation style' WS '[' rotation' v]';

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
changeColorEffect       : 'change [color v] effect by 'exprOrLiteral
                        | 'change' WS '['colorEffect' v] effect by 'exprOrLiteral;
setColorEffect          : 'set [color v] effect to 'exprOrLiteral
                        | 'set' WS '['colorEffect' v] effect to 'exprOrLiteral;
clearColorEffect        : 'clear graphic effects';
show                    : 'show';
hide                    : 'hide';
goToLayer               : 'go to' WS '['layerChoice' v] layer';
goForwardBackwardLayer  : 'go' WS '['forwardBackwardChoice' v] 'exprOrLiteral' layers';
switchBackdropWait      : 'switch backdrop to 'backdropSelect' and wait';

colorEffect             : 'fisheye'
                        | 'whirl'
                        | 'pixelate'
                        | 'mosaic'
                        | 'brightness'
                        | 'ghost'
                        ;

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
changeSoundEffect       : 'change' WS '['soundEffect' v] effect by 'exprOrLiteral;
setSoundEffect          : 'set' WS '['soundEffect' v] effect to 'exprOrLiteral;
clearSoundEffect        : 'clear sound effects';
changeVolume            : 'change volume by 'exprOrLiteral;
setVolume               : 'set volume to 'exprOrLiteral' %';

controlStmt             : waitSeconds
                        | repeat
                        | forever
                        | ifStmt
                        | waitUntil
                        | repeatUntil
                        | stop
                        | createClone
                        | deleteClone
                        ;

ifStmt                  : 'if ' exprOrLiteral ' then' NEWLINE thenBlock=stmtList WS* ('else' NEWLINE elseBlock=stmtList WS*)? 'end';

waitSeconds             : 'wait 'exprOrLiteral' seconds';
repeat                  : 'repeat 'exprOrLiteral NEWLINE stmtList WS* 'end';
forever                 : 'forever' NEWLINE stmtList WS* 'end';
waitUntil               : 'wait until 'exprOrLiteral;
repeatUntil             : 'repeat until 'exprOrLiteral NEWLINE stmtList WS* 'end';
stop                    : 'stop' WS '['stopChoice' v]';
createClone             : 'create clone of 'cloneChoice;
deleteClone             : 'delete this clone';

eventStmt               : broadcast
                        ;

broadcast               : 'broadcast 'message wait=' and wait'?;

sensingStmt             : ask
                        | setDragMode
                        | resetTimer
                        ;

ask                     : 'ask 'exprOrLiteral' and wait';
setDragMode             : 'set drag mode' WS '['dragmode' v]';
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

setVar                  : 'set' WS '['stringArgument' v] to 'exprOrLiteral;
changeVar               : 'change' WS '['stringArgument' v] by 'exprOrLiteral;
showVar                 : 'show variable' WS '['stringArgument' v]';
hideVar                 : 'hide variable' WS '['stringArgument' v]';
addToList               : 'add' WS exprOrLiteral WS 'to' WS '['stringArgument' v]';
deleteFromList          : 'delete 'exprOrLiteral WS 'of' WS '['stringArgument' v]';
deleteAllOfList         : 'delete all of' WS '['stringArgument' v]';
insertToList            : 'insert 'insertion=exprOrLiteral' at 'location=exprOrLiteral WS 'of' WS '['stringArgument' v]';
replaceItemInList       : 'replace item 'oldItem=exprOrLiteral WS 'of' WS '['stringArgument' v] with 'newItem= exprOrLiteral;
showList                : 'show list' WS '['stringArgument' v]';
hideList                : 'hide list' WS '['stringArgument' v]';

penStmt                 : eraseAll
                        | stamp
                        | penDown
                        | penUp
                        | setPenColorToColor
                        | changePenColor
                        | setPenColorToValue
                        | changePenSize
                        | setPenSize
                        ;

eraseAll                : 'erase all';
stamp                   : 'stamp';
penDown                 : 'pen down';
penUp                   : 'pen up';
setPenColorToColor      : 'set pen color to' WS? touchingColorChoice;
changePenColor          : 'change pen' WS? penEffect WS? 'by' WS? exprOrLiteral;
setPenColorToValue      : 'set pen' WS? penEffect WS? 'to' WS? exprOrLiteral;
changePenSize           : 'change pen size by' WS? exprOrLiteral;
setPenSize              : 'set pen size to' WS? exprOrLiteral;

penEffect               : '('fixedEffect' v)'
                        | exprOrLiteral
                        ;

fixedEffect             : 'color'
                        | 'saturation'
                        | 'brightness'
                        | 'transparency'
                        ;

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

expression              : emptyNum='()'
                        | '('numExpr')'
                        | emptyBool='<>'
                        | '<'boolExpr'>'
                        | '(' stringArgument list=LIST_MARKER? WS* ')'
                        ;

boolExpr                : empty=WS*
                        | touching
                        | touchingColor
                        | colorTouchingColor
                        | keyPressed
                        | mouseDown
                        | binaryBoolExpr
                        | not
                        | contains
                        | listContains
                        | procDefParam=stringArgument
                        ;

binaryBoolExpr          : firstExpr=exprOrLiteral ((WS? (eq='=' | and='and' | or='or') WS?) | (lt=' < ' | gt=' > ')) secondExpr=exprOrLiteral;

touching                : 'touching 'touchingChoice WS? '?';
touchingColor           : 'touching color 'touchingColorChoice WS? '?';
colorTouchingColor      : 'color' WS? firstColor=touchingColorChoice' is touching 'secondColor=touchingColorChoice WS? '?';
keyPressed              : 'key 'keySelect' pressed?';
mouseDown               : 'mouse down?';
not                     : 'not 'exprOrLiteral;
contains                : firstExpr=exprOrLiteral' contains 'secondExpr=exprOrLiteral WS? '?';
listContains            : '['stringArgument' v] contains 'exprOrLiteral WS? '?';

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
                        // mathFunction and actorAttribute must not be swapped, otherwise math functions are parsed as
                        // attributeOf expressions (They share a common expression syntax with only the options for
                        // known choices in the left operand being different.)
                        | mathFunction
                        | actorAttribute
                        | currentTime
                        | daysSince
                        | userName
                        | binaryNumExpr
                        | pickRandom
                        | join
                        | getLetterAtIndex
                        | lengthOf
                        | round
                        | itemAtIndex
                        | indexOfItem
                        ;

binaryNumExpr           : firstExpr=exprOrLiteral WS? (add='+' | sub='-' | mult='*' | div='/' | mod='mod') WS? secondExpr=exprOrLiteral;

xPosition               : 'x position';
yPosition               : 'y position';
direction               : 'direction';
numCostume              : 'costume' WS '['nameNum' v]';
numBackdrop             : 'backdrop' WS '['nameNum' v]';
size                    : 'size';
volume                  : 'volume';
distanceTo              : 'distance to 'distanceChoice;
answer                  : 'answer';
mouseX                  : 'mouse x';
mouseY                  : 'mouse y';
loudness                : 'loudness';
timer                   : 'timer';
actorAttribute          : '['attributeChoice' v] of 'element;
currentTime             : 'current' WS '['currentChoice' v]';
daysSince               : 'days since 2000';
userName                : 'username';
pickRandom              : 'pick random 'firstExpr=exprOrLiteral WS 'to' WS secondExpr=exprOrLiteral;
join                    : 'join 'firstExpr=exprOrLiteral secondExpr=exprOrLiteral;
getLetterAtIndex        : 'letter 'firstExpr=exprOrLiteral WS 'of' WS secondExpr=exprOrLiteral;
lengthOf                : 'length of' WS (stringExpr=exprOrLiteral | '[' listVar=stringArgument ' v]');
round                   : 'round 'exprOrLiteral;
mathFunction            : '['mathChoice' v] of 'exprOrLiteral;
itemAtIndex             : 'item 'exprOrLiteral WS 'of' WS '['stringArgument' v]';
indexOfItem             : 'item # of 'exprOrLiteral WS 'in' WS '[' stringArgument' v]';

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

touchingColorChoice     : LBRACK HEX RBRACK
                        | LPAREN HEX RPAREN
                        | exprOrLiteral
                        ;

// escape sequences, or anything else that is not a newline or an unescaped special character
stringArgument          : (ESC | ~(NEWLINE|DELIM|LIST_MARKER))*?;

/*
 * Lexer Rules
 */

fragment HEX_DIGIT      : [0-9a-fA-F];

DIGIT                   : [0-9];

NUMBER                  : '-'? (DIGIT)+ ('.' (DIGIT)+)?;

NEWLINE                 : '\r\n' | '\n' ;

WS                      : [ \t]+;

BEGIN_ACTOR             : '//Sprite: ' ~[\r\n]+ NEWLINE;

COMMENT                 : WS* '//' ~[\r\n]*;

HEX                     : '#' HEX_DIGIT HEX_DIGIT HEX_DIGIT (HEX_DIGIT HEX_DIGIT HEX_DIGIT)?;

ESC                     : '\\(' | '\\)' | '\\[' | '\\]' | '\\<' | '\\>' | ':\\:';
CHOICE_END              : ' v]';
ROUND_CHOICE_END        : ' v)';
LIST_MARKER             : WS* '::' WS? 'list';
LPAREN                  : '(';
RPAREN                  : ')';
LBRACK                  : '[';
RBRACK                  : ']';
LANGLE                  : '<';
RANGLE                  : '>';
DELIM                   : LPAREN | RPAREN | LBRACK | RBRACK | LANGLE | RANGLE;

ANY                     : .;
