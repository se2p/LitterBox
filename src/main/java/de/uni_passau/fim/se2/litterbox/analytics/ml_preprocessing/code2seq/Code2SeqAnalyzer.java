package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2seq;

import de.uni_passau.fim.se2.litterbox.analytics.MLPreprocessingAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec.Code2VecAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec.GeneratePathTask;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec.ProgramFeatures;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Code2SeqAnalyzer extends MLPreprocessingAnalyzer<ProgramFeatures> {

    private static final Logger log = Logger.getLogger(Code2VecAnalyzer.class.getName());
    private final int maxPathLength;

    public Code2SeqAnalyzer(final MLPreprocessorCommonOptions commonOptions, int maxPathLength) {
        super(commonOptions);
        this.maxPathLength = maxPathLength;
    }

    @Override
    public Stream<ProgramFeatures> process(File inputFile) {
        final Program program = extractProgram(inputFile);
        if (program == null) {
            log.warning("Program was null. File name was '" + inputFile.getName() + "'");
            return Stream.empty();
        }

        GeneratePathTask generatePathTask = new GeneratePathTask(program, maxPathLength, includeStage, wholeProgram,
                includeDefaultSprites);
        return generatePathTask.createContextForCode2Seq().stream();
    }

    @Override
    protected String resultToString(ProgramFeatures result) {
        return result.toString();
    }

    @Override
    protected Path outputFileName(File inputFile) {
        return Path.of(FilenameUtils.removeExtension(inputFile.getName()));
    }
}
