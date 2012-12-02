package org.ops4j.pax.runner.platform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author dpishchukhin
 */
public class DaemonStartRunner implements StoppableJavaRunner {
    /**
     * logger.
     */
    private static final Log LOG = LogFactory.getLog(DaemonStartRunner.class);

    private static final long DEFAULT_DAEMON_TIMEOUT = TimeUnit.SECONDS.toMillis(1);

    private Thread shutdownHook = null;

    private boolean continueAwait = true;

    private final StoppableJavaRunner m_delegate;
    private final CountDownLatch latch;

    private long daemonTimeout;

    public DaemonStartRunner(String timeoutStr) {
        if (timeoutStr != null) {
            try {
                daemonTimeout = Integer.parseInt(timeoutStr);
            } catch (NumberFormatException e) {
                daemonTimeout = DEFAULT_DAEMON_TIMEOUT;
            }
        } else {
            daemonTimeout = DEFAULT_DAEMON_TIMEOUT;
        }

        latch = new CountDownLatch(1);
        m_delegate = new DefaultJavaRunner(true) {
            @Override
            public void waitForExit() {
                latch.countDown();
                super.waitForExit();
            }
        };
    }

    public void exec(final String[] vmOptions, final String[] classpath, final String mainClass,
                     final String[] programOptions, final String javaHome, final File workingDir,
                     final String[] environmentVariables) throws PlatformException {
        new Thread("DaemonStartRunner") {
            @Override
            public void run() {
                try {
                    m_delegate.exec(vmOptions, classpath, mainClass, programOptions, javaHome, workingDir, environmentVariables);
                } catch (PlatformException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
        try {
            latch.await();

            Thread.sleep(daemonTimeout);
        } catch (InterruptedException e) {
            LOG.warn(e.getMessage(), e);
        }
        startShutdownFileMonitor(workingDir);
    }

    public void exec(String[] vmOptions, String[] classpath, String mainClass, String[] programOptions, String javaHome, File workingDir) throws PlatformException {
        exec(vmOptions, classpath, mainClass, programOptions, javaHome, workingDir, new String[0]);
    }

    public void shutdown() {
        m_delegate.shutdown();
    }

    private void startShutdownFileMonitor(final File workingDir) {
        new Thread("ShutdownFileMonitor") {
            public void run() {
                createLockFile(workingDir);
                shutdownHook = createShutdownHook();
                Runtime.getRuntime().addShutdownHook(shutdownHook);

                // Loop waiting for a shutdown file
                while (continueAwait) {
                    // Wait for the next connection
                    // handle each new connection in a separate thread
                    File file = new File(workingDir, DaemonCommons.SHUTDOWN_FILE);
                    if (file.exists()) {
                        file.delete();
                        DaemonStartRunner.this.stop(workingDir);
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        DaemonStartRunner.this.stop(workingDir);
                    }
                }
                LOG.trace("Finished awaiting...");
            }
        }.start();
    }

    /**
     * Creates a simple shutdown hook.
     *
     * @return Unstarted thread that can be registered as a shutdown hook
     */
    private Thread createShutdownHook() {
        return new Thread(
                new Runnable() {
                    public void run() {
                        LOG.trace("Executing shutdown hook...");
                        shutdown();
                    }
                }, "Pax Runner Daemon Shutdown Hook"
        );
    }

    /**
     * Creates a "lock" file. An empty file that is created as the Daemon is
     * started (attached/detached) and removed when the it stops. The file
     * will be used to determine if the Daemon is already running.
     *
     * @param workingDir
     */
    private void createLockFile(File workingDir) {
        File lock = new File(DaemonCommons.getRunnerHomeDir(workingDir, true), DaemonCommons.LOCK_FILE);
        if (lock.exists()) {
            throw new RuntimeException(DaemonCommons.LOCK_FILE + " exists. Please make sure" +
                    " that the Pax Runner daemon is not already running.");
        }
        try {
            lock.createNewFile();
            lock.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeLockFile(File workingDir) {
        File lock = new File(DaemonCommons.getRunnerHomeDir(workingDir, true), DaemonCommons.LOCK_FILE);
        if (lock.exists()) {
            lock.delete();
        }
    }

    private void stopAwait() {
        continueAwait = false;
    }

    /**
     * Stops the running instance of the Daemon and Pax Runner if any.
     *
     * @param workingDir
     */
    public void stop(File workingDir) {
        stopAwait();
        removeLockFile(workingDir);
        if (shutdownHook != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            LOG.trace("Removed Shutdown Hook.");
            shutdownHook.run();
            shutdownHook = null;
        } else {
            shutdown();
        }
    }
}
