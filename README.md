![License GPL v3](https://img.shields.io/github/license/se2p/LitterBox?color=blue&style=flat-square)

> Code embeddings are used in machine learning to map source code into a dense vector space.
> Various models have been proposed to learn this mapping.
> They use different information from the code (eg just tokens, AST, control/data flow, …) as model input.
> EmbeddedKittens can be used to extract this information from Scratch projects and transform it into the required format to be used as input for the machine learning model.

EmbeddedKittens[^1] is developed at the
[Chair of Software Engineering II](https://www.fim.uni-passau.de/lehrstuhl-fuer-software-engineering-ii/)
of the [University of Passau](https://www.uni-passau.de).

It originally started as a fork/extension of the Scratch static code analysis tool [LitterBox](https://github.com/se2p/LitterBox).
Internally, it uses the parser, AST and data flow information as obtained from LitterBox.


# Getting started

## Building EmbeddedKittens

EmbeddedKittens is built using [Maven](https://maven.apache.org/).
To produce an executable JAR file, run the following command:

```bash
mvn package
```

This will produce `target/embedded-kittens-1.0.full.jar`

Pre-built JARs are also available for each release on GitHub.


## Using EmbeddedKittens

To see an overview of the available command line options, type:

```bash
java -jar embedded-kittens-1.0.full.jar --help
```

All the subcommands also accept the `--help` flag to show information about the specific parameters.
Eg
```bash
java -jar embedded-kittens-1.0.full.jar code2vec --help
```


## Output formats

The currently supported formats are suitable for the following models:

- [ASTNN](https://doi.org/10.1109/ICSE.2019.00086)
- [code2vec](https://doi.org/10.1145/3291636)
- [code2seq](https://arxiv.org/abs/1808.01400)
- [GGNN](https://arxiv.org/abs/1711.00740)
- and tokenised sequences for Transformers/LSTM/…


### Example: code2vec output

To be able to use the code2vec model with the programming language Scratch, a scratch parser is needed to generate the required input representation.
According to the description on https://github.com/tech-srl/code2vec#extending-to-other-languages,
EmbeddedKittens produces for each Scratch program a file with these rules.
EmbeddedKittens needs a path to a single file or a folder with multiple projects and produces the output to the declared output folder. 

```bash
java -jar embedded-kittens-1.0.full.jar code2vec \
  --output "<path/to/folder/for/the/output>" \
  --path "<path/to/json/project/or/folder/with/projects>"
```

There are some differences between Scratch and "normal" programming languages like Java.
The most important is that sprites are primarily split into *unnamed* scripts rather than *named* methods.
Because of that, Litterbox uses sprite names like method names and creates path contexts from every single sprite in a project.


#### Code2vec output per script

EmbeddedKittens can generate the context paths per scripts and procedures.
Given a Scratch program as input, it produces for each script and procedure a file containing the needed input representation for the code2vec model. 

```bash
java -jar embedded-kittens-1.0.full.jar code2vec \
  --output "<path/to/folder/for/the/output>" \
  --path "<path/to/json/project/or/folder/with/projects>" \
  --scripts
``` 


## Contributing

Please open an issue if you find a bug.
We are open to pull requests both for fixes and the support of new model formats.
For larger features or restructurings, please open an issue first to discuss the best approach on how to best achieve this.
If possible, please split larger changes into smaller pull/merge requests to make them easier to review and integrate step-by-step.


# Licence

EmbeddedKittens is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.

EmbeddedKittens is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.


[^1]: Neither atomic, nor [exploding](https://www.explodingkittens.com/), but peacefully sleeping in vector space.
