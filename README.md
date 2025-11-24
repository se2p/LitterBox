![LitterBox Logo](src/main/resources/litterbox.png "LitterBox Logo")

![License GPL v3](https://img.shields.io/github/license/se2p/LitterBox?color=blue&style=flat-square)
![Codecov](https://img.shields.io/codecov/c/github/se2p/LitterBox?style=flat-square)

LitterBox is a static code analysis tool for detecting bugs in
[Scratch](https://scratch.mit.edu/) projects.

Bugs in Scratch programs can spoil the fun and inhibit learning
success. Many common bugs are the result of recurring patterns of bad
code. LitterBox provides checks for a collection of common bug
patterns. Given a Scratch project ID or a file, LitterBox retrieves
and parses the source code of the project, and reports all instances
of bug patterns identified. LitterBox can also check for code smells
and provide metrics about selected Scratch projects.

LitterBox is developed at the
[Chair of Software Engineering II](https://www.fim.uni-passau.de/lehrstuhl-fuer-software-engineering-ii/)
and the [Didactics of Informatics](https://ddi.fim.uni-passau.de/) of the [University of Passau](https://www.uni-passau.de).

## Building LitterBox

LitterBox is built using [Maven](https://maven.apache.org/). To
produce an executable jar-file, run the following command:

```
mvn package
```

This will produce `target/Litterbox-1.10.full.jar`


## Using LitterBox

To see an overview of the command line options available in LitterBox type:

```bash
java -jar Litterbox-1.10.full.jar --help
```

### Basic usage

LitterBox parses the JSON file of a Scratch project, which contains
its source code. Given such a JSON file, LitterBox is invoked as follows:

```bash
java -jar Litterbox-1.10.full.jar check --path <path/to/project.json>
```

As a result, LitterBox will report any occurrences of bug patterns or
code smells in the project on the console.


### Downloading projects

If you want to check a specific project given its ID (which you can
find in the URL of the project), you can use the following command:

```bash
java -jar Litterbox-1.10.full.jar check --project-id <project-id> --path <path/to/store/downloaded/project>
```

When invoked this way, LitterBox will retrieve the JSON file
automatically from the Scratch-website, store it at the given path,
and then run checks on it. Note that the Scratch project to be
analyzed has to be shared publicly for this.


### Checking multiple projects

If you want to check several projects at once, you can put a list of
project IDs to check in a text file (one project ID per line) and
invoke LitterBox as follows:

```bash
java -jar Litterbox-1.10.full.jar check --project-list <path/to/projectidlist.txt> --path <path/to/projects>
```

LitterBox will check the given path for the projects.
If a project is not found at the given path, LitterBox
will download and store it at the given path, and then perform
the checks.


### Output options

In addition to the console output, LitterBox can produce output in
comma separated value (CSV) or JSON format. LitterBox uses the
filename specified in order to decide whether to produce CSV or JSON
output:

```bash
java -jar Litterbox-1.10.full.jar check --path <path/to/project.json> --output <result.csv>
```

The CSV file will contain a high-level summary of the number of
different bug patterns found in the project; the JSON file will
contain a detailed list of all instances of the bug

Furthermore, LitterBox can produce an annotated version of the
analyzed Scratch-project, where all occurrences of bug patterns are
highlighted with comments.

```bash
java -jar Litterbox-1.10.full.jar check --path <path/to/project.json> --annotate <results/>
```


You can choose the language used for hints and comments in the JSON
and Scratch output with the `--lang` option.


### Selecting bug finders

Using the `--detectors` command line parameter it is possible to
specify which bug patterns to check for. The option takes a
comma-separated list of bug patterns, e.g.:


```bash
java -jar Litterbox-1.10.full.jar \
    check \
    --path <path/to/project.json> \
    --detectors endless_recursion,call_without_definition
```

A full list of available bug checkers can be retrieved using:

```bash
java -jar Litterbox-1.10.full.jar --help
```

To select all bug patterns, you can also use the term `bugs` in the
list; to select all code smell checks use `smells`.


### Reporting the bug patterns per script

The bug patterns can be reported per scripts and procedures instead of for the whole program. In this case, only the bug patterns that can be detected through intra-scripts and intra-procedures analysis are considered.

```bash
java -jar Litterbox-1.10.full.jar check \
    --path <path/to/project.json> \
    --output <result.csv> \
    --detectors script-bugs \
    --scripts
```


### Deactivating robot finders

To deactivate finders for the mBlock and Codey Rocky robots set the flag
in the litterbox.properties file to false. This can reduce the run time of
the analysis and the size of a resulting CSV file.

```properties
issues.load_mblock=false
```


### Collecting statistics

LitterBox can produce statistics on code metrics of a project (e.g.,
number of blocks, number of sprites, weighted method count):

```bash
java -jar Litterbox-1.10.full.jar \
    stats \
    --path <path/to/project.json> \
    --output <statsfile.csv>
```


### Generating Graphviz files

LitterBox can generate a Graphviz `.dot` file representing various graphs of the project, such as the Abstract Syntax Tree (AST) or Control Flow Graph (CFG).

```bash
java -jar Litterbox-1.10.full.jar dot --path <path/to/project.json> --output <output.dot> [--graph <GraphType>]
```

Available graph types:
- `AST`: Abstract Syntax Tree (default)
- `CFG`: Control Flow Graph
- `CDG`: Control Dependence Graph
- `DDG`: Data Dependence Graph
- `DT`: Dominator Tree
- `PDT`: Post Dominator Tree
- `TDG`: Time Dependence Graph
- `PDG`: Program Dependence Graph


### Mining projects

LitterBox provides a dedicated `mine` command for bulk downloading of Scratch projects. This is useful for creating datasets.

```bash
java -jar Litterbox-1.10.full.jar mine --output <path/to/output/dir> [options]
```

Available options:
- `--project-id <id>`: Download a specific project.
- `--project-list <file>`: Download projects listed in a file (one ID per line).
- `--from <id> --to <id>`: Download a range of project IDs.
- `--recent <count>`: Download the X most recent projects.
- `--popular <count>`: Download the X most popular projects.
- `--user <username>`: Download all projects of a specific user.
- `--with-assets`: Download as `.sb`/`.sb2`/`.sb3` file (zipped JSON + assets).
- `--with-metadata`: Additionally download project metadata.


### Automatically refactoring projects

Since version 1.7 Litterbox can automatically refactor a given Scratch project to improve its readability:

```bash
java -jar Litterbox-1.10.full.jar \
    refactoring \
    --path <path/to/project.json> \
    --refactored-projects <path/to/output-dir>
```

To this end, Litterbox uses a multi-objective search-based approach to explore possible
refactorings that optimize code readability metrics such as size, complexity and entropy.
The resulting set of refactored versions of the original project will be placed in `path/to/output-dir`.


### LLM Interaction

Since version 1.10 LitterBox allows you to ask an LLM about a Scratch program and makes it possible to automatically integrate LLM-provided issue fixes into the program.
You can find all features under the `llm` subcommand:
```bash
java -jar Litterbox-1.10.full.jar llm --help
```

It supports either the OpenAI API, or a self-hosted Ollama instance.
To provide an OpenAI API key, start LitterBox like
```bash
java -Dlitterbox.llm.openai.api-key=KEY Litterbox-1.10.full.jar llm …
```
You can find other configuration options that can be passed via `-D` flags (e.g., model choice, Ollama API endpoint) in `src/main/resources/scratchllm.properties`.

For our integration of some of the LLM-features into the Scratch browser interface, see https://github.com/se2p/NuzzleBug and https://github.com/se2p/LitterBox-Web.


## Adding new bug patterns or code smells

To implement your own bug patterns, extend the `AbstractIssueFinder`
class which implements an AST visitor. The `check` method is expected
to return a set of all `Issue` instances encountered during the
traversal. Please use the `addIssue` method provided in the `AbstractIssueFinder`.

To enable the check, register it in the `IssueTool` class.
Add it to the `generateSmellFinders()` method via `registerSmellFinder(new NewFinder, smellFinders)` for smell finders or to the`generateBugFinders()` for bug finders via `registerBugFinder(new NewFinder, bugFinders)`.

Please also add the name of the finder to `IssueNames_de.properties` / `IssueNames_en.properties` and
provide hints in `IssueHints_de.properties` / `IssueHints_en.properties`, so that your finder helps programmers understand how to resolve the issue in their code to improve code quality.

## Website

We provide a website in which users can check their projects with LitterBox directly by uploading the project file or entering the project ID: [https://scratch-litterbox.org/](https://scratch-litterbox.org/)


## Publications

### LitterBox

G. Fraser, U. Heuer, N. Körber, E. Wasmeier, "LitterBox: A Linter for Scratch Programs", 
in Proceedings of the IEEE/ACM 43rd International Conference on Software Engineering: Software Engineering Education and Training (ICSE-SEET) (pp. 183-188). IEEE, 2021.
[https://doi.org/10.1109/ICSE-SEET52601.2021.00028](https://doi.org/10.1109/ICSE-SEET52601.2021.00028)

### Bug Patterns

C. Frädrich, F. Obermüller, N. Körber, U. Heuer, and G. Fraser, “Common bugs in scratch programs,” in Proceedings of
the 25th Annual Conference on Innovation and Technology in Computer
Science Education (ITiCSE), pages 89-95, ACM,
2020. [https://doi.org/10.1145/3341525.3387389](https://doi.org/10.1145/3341525.3387389)

### Code Perfumes

F. Obermüller, L. Bloch, L. Greifenstein, U. Heuer, and G. Fraser, "Code Perfumes: Reporting Good Code to Encourage
Learners", in Proceedings of the 16th Workshop in Primary and Secondary Computing Education (WiPSCE ’21). ACM,
2021. [https://arxiv.org/abs/2108.06289](https://arxiv.org/abs/2108.06289)

### Code Patterns in mBlock Programs

F. Obermüller, R. Pernerstorfer, L. Bailey, U. Heuer, and G. Fraser, "Common Patterns in Block-Based Robot Programs",
in Proceedings of the 17th Workshop in Primary and Secondary Computing Education (WiPSCE ’22). ACM,
2022. [https://doi.org/10.1145/3556787.3556859](https://doi.org/10.1145/3556787.3556859)

### Do Scratchers Fix Their Bugs?

F. Obermüller and G. Fraser, "Do Scratchers Fix Their Bugs? Detecting Fixes of Scratch Static Analysis Warnings",
in Proceedings of the 19th Workshop in Primary and Secondary Computing Education (WIPSCE ’24). ACM,
2024. [https://doi.org/10.1145/3677619.3678108](https://doi.org/10.1145/3677619.3678108)

### Question Generation

F. Obermüller and G. Fraser. "Automatically Generating Questions About Scratch Programs",
in Proceedings of the ACM Global Computing Education Conference 2025 Vol 1 (CompEd 2025). ACM,
2025. https://doi.org/10.1145/3736181.3747131

### LLM Interaction

B. Fein, F. Obermüller and G. Fraser, "LitterBox+: An Extensible Framework for LLM-enhanced Scratch Static Code Analysis",
in Proceedings of the 40th IEEE/ACM International Conference on Automated Software Engineering (ASE ’25). IEEE,
2025. https://doi.org/TBD ([arXiv:2509.12021](https://arxiv.org/abs/2509.12021))

The accompanying poster is available at https://doi.org/10.5281/zenodo.17532057.


## Contributors

LitterBox is developed at the
[Chair of Software Engineering II](https://www.fim.uni-passau.de/lehrstuhl-fuer-software-engineering-ii/)
and the [Didactics of Informatics](https://ddi.fim.uni-passau.de/) of
the [University of Passau](https://www.uni-passau.de).

Contributors:

Felix Adler\
Lisa Bailey\
Florian Beck\
Lena Bloch\
Emily Courtney\
Benedikt Fein\
Patric Feldmeier\
Christoph Frädrich\
Gordon Fraser\
Luisa Greifenstein\
Eva Gründinger\
Michael Grüner\
Ute Heuer\
Alaa Khalil\
Nina Körber\
Simon Labrenz\
Jonas Lerchenberger\
Stephan Lukasczyk\
Miriam Münch\
Florian Obermüller\
Robert Pernerstorfer\
Gregorio Robles\
Lisa-Marina Salvesen\
Sebastian Schweikl\
Andreas Stahlbauer\
Florian Sulzmeier\
Ewald Wasmeier

LitterBox is supported by the project FR 2955/3-1 funded by the
"Deutsche Forschungsgemeinschaft" and BMBF project
"primary::programming" as part of the Qualitätsoffensive
Lehrerbildung.
