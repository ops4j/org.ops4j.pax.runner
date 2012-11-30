package org.ops4j.pax.runner.platform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

/**
 * @author dpishchukhin
 */
class DaemonCommons {
    private static final Log LOG = LogFactory.getLog(DaemonCommons.class);

    static final String NEWLINE = "\r\n";
    static final String LOCK_FILE = ".runner.platform.daemon.lock";
    static final String SHUTDOWN_FILE = ".runner.platform.daemon.shutdown";

    /**
     * Determines if any instance of the Daemon is already started.
     *
     * @param workingDir
     * @return True if any instance of this class is already started, false otherwise.
     */
    public static boolean isDaemonStarted(File workingDir) {
        File lock = new File(getRunnerHomeDir(workingDir, false), LOCK_FILE);
        if (lock.exists() && lock.isFile()) {
            return true;
        }
        return false;
    }

    /**
     * Returns the file reference of the Runner's home directory. Creates one if
     * it doesn't exist and if the create flag is set to <code>true</code>.
     *
     * @return
     */
    static File getRunnerHomeDir(File workingDir, boolean create) {
        if (!workingDir.exists() && create) {
            if (workingDir.mkdirs()) {
                LOG.debug("Created Pax Runner Home.");
            }
        }
        return workingDir;
    }
}
