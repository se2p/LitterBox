# LitterBox

[![pipeline status](https://gitlab.infosun.fim.uni-passau.de/se2/litterbox/badges/master/pipeline.svg)](https://gitlab.infosun.fim.uni-passau.de/se2/litterbox/pipelines)
[![coverage report](https://gitlab.infosun.fim.uni-passau.de/se2/litterbox/badges/master/coverage.svg)](https://gitlab.infosun.fim.uni-passau.de/se2/litterbox/commits/master)


Static code analysis tool for detecting recurring bug patterns in Scratch projects. 
Currently holding 25 different detectors.

## Author

* **Florian Sulzmaier** - [Olf96](https://github.com/Olf96)

## Usage

LitterBox can analyze a single project (checkSingle()) and produce console output 
for every single Issue.

Also, there is a method (checkMultiple()) that takes a folder path as an input and analyzes all zip files within the directory. 
This method produces a csv file with all issue counts for every project.
##
To use LitterBox with the command line, build the Jar with mvn clean and mvn package.

### Command Line Options:

1. path (required) - the Scratch projects path or a folder path with multiple Scratch projects
2. folder - if you want to analyze multiple projects give a output path for the csv file
3. detectors - all the detectors you want to run (short names seperated by ","), if not set, all will be used
4. version (required) - the Scratch Version ('2' or '3')

#### Detectors short names:

mssfrev->missing_forever_loop

racecnd->race_condition

lggymve->laggy_movement

msstrm->missing_termination

emptyscrpt->empty_script

dplsprt->duplicated_sprite

glblstrt->has_global_start

noop->no_op

strt->sprite_starting_point

sprtname->sprite_naming

lngscr->long_script

brdcstsync->broadcast_sync

dblif->double_condition

vrblscp->variable_scope

clninit->clone_initialization

cnt->block_count

noopprjct->noop_project

unusedvar->unused_variable

mdlman->middle_man

nstloop->nested_loops

lsblck->loose_blocks

attrmod->multiple_attribute_modification

squact->sequential_actions

dplscrpt->duplicated_script

emptybd->empty_body

inappint->inappropriate_intimacy

clone->total_clones

#### CT Score Evaluation

logthink->logical_thinking

abstr->abstraction

para->parallelism

synch->synchronization

flow->flow_control

userint->user_interactivity

datarep->data_representation

#### Example:

java -cp C:\ScratchAnalytics-1.0.jar Main -path C:\scratchprojects\files\ -version 3 -folder C:\scratchprojects\files\test.csv -detectors cnt,glblstrt

This will run only BlockCount and GlobalStartingpoint on all projects in C:\scratchprojects\files\; and it will also save the test.csv file in the same location.

## Extendability

First of all, create a new IssueFinder and implement the corresponding interface. 
The check() method must return a IssueReport with the issue name, count for the current project, 
the position of all issue occurrences, the project path and notes about the issue.
Then, register the newly created IssueFinder in the IssueTool constructor ( finder.add(new NewFinder()) ).
The finder name will automatically be added to the printed csv file.