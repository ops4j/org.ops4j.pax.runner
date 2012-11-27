package org.ops4j.pax.runner.platform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.platform.JavaRunner;
import org.ops4j.pax.runner.platform.PlatformException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

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
            Socket socket;
            PrintWriter out;
            try {
                socket = new Socket("localhost", DaemonCommons.getShutdownPort(workingDir));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (UnknownHostException e) {
                LOG.error("Unknown address: localhost.");
                return;
            } catch (IOException e) {
                LOG.error("Couldn't connect to: localhost.");
                return;
            }

            final String shutdownCmd = DaemonCommons.getShutdown(workingDir);
            out.write(shutdownCmd + "\n");
            out.flush();
            LOG.debug("Pax Runner Daemon: Shutdown command issued:" + shutdownCmd);
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
