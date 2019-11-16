package newanalytics;

import scratch.newast.model.Program;

/**
 * Interface for all IssueFinders
 */
public interface IssueFinder {

    /**
     * @param program The project to check
     * @return a IssueReport object
     */

    IssueReport check(Program program);

    String getName();

}
