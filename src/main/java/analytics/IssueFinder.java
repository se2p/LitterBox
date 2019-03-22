package analytics;

import scratch2.structure.Project;

/**
 * Interface for all IssueFinders
 */
public interface IssueFinder {

    /**
     * @param project The project to check
     * @return a Issue object
     */

    Issue check(Project project);
}
