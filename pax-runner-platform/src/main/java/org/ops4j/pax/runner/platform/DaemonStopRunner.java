package org.ops4j.pax.runner.platform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author dpishchukhin
 */
public class DaemonStopRunner implements JavaRunner {
    /**
     * logger.
     */
    private static final Log LOG = LogFactory.getLog(DaemonStopRunner.class);


    public void exec(String[] vmOptions, String[] classpath, String mainClass, String[] programOptions, String javaHome, File workingDir, String[] environmentVariables) throws PlatformException {
        stop(workingDir);
    }

    public void exec(String[] vmOptions, String[] classpath, String mainClass, String[] programOptions, String javaHome, File workingDir) throws PlatformException {
        exec(vmOptions, classpath, mainClass, programOptions, javaHome, workingDir, new String[0]);
    }

    private void stop(File workingDir) {
        if (DaemonCommons.isDaemonStarted(workingDir)) {
            try {
                File shutdownFile = new File(workingDir, DaemonCommons.SHUTDOWN_FILE);
                shutdownFile.deleteOnExit();
                shutdownFile.createNewFile();
            } catch (IOException e) {
                LOG.error("Couldn't connect to: localhost.");
                return;
            }

            do {
                LOG.info("Pax Runner Daemon: Shutdown in progress...");
                try {
                    Thread.sleep(1000 * 2);
                } catch (InterruptedException e) {
                    // ignore
                }
            } while (DaemonCommons.isDaemonStarted(workingDir));
            LOG.info("Pax Runner Daemon Stopped.");
        } else {
            LOG.warn("No Daemons yet launched!");
        }
    }
}
