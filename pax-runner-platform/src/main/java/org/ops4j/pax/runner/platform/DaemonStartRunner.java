package org.ops4j.pax.runner.platform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.AccessControlException;
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

    private int networkTimeout = 1000 * 60;
    private ServerSocket serverSocket = null;
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

    public synchronized void exec(final String[] vmOptions, final String[] classpath, final String mainClass,
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
        startServerSocket(workingDir);
    }

    public void exec(String[] vmOptions, String[] classpath, String mainClass, String[] programOptions, String javaHome, File workingDir) throws PlatformException {
        exec(vmOptions, classpath, mainClass, programOptions, javaHome, workingDir, new String[0]);
    }

    public synchronized void shutdown() {
        m_delegate.shutdown();
    }

    private void startServerSocket(final File workingDir) {
        new Thread("ServerSocketThread") {
            public void run() {
                createInfoFile(workingDir);
                createLockFile(workingDir);
                shutdownHook = createShutdownHook();
                Runtime.getRuntime().addShutdownHook(shutdownHook);

                // Set up a server socket to wait on
                try {
                    LOG.debug("Setting up shutdown port on " + DaemonCommons.getShutdownPort(workingDir));
                    serverSocket = new ServerSocket(DaemonCommons.getShutdownPort(workingDir));
                } catch (IOException e) {
                    throw new RuntimeException("Unable to set up shutdown port ["
                            + DaemonCommons.getShutdownPort(workingDir)
                            + "].", e);
                }

                // Loop waiting for a connection and a valid command
                while (continueAwait) {
                    // Wait for the next connection
                    try {
                        // handle each new connection in a separate thread
                        new ClientHandler().handle(serverSocket.accept());
                    } catch (IOException e) {
                        LOG.debug("Stopped accepting connections." + e.getMessage());
                    }
                }

                // Close the server socket and return
                try {
                    if (serverSocket != null) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    ;
                }
                LOG.trace("Finished awaiting...");
            }
        }.start();
    }

    /**
     * Creates a file that stores information about the running instance of the
     * Daemon.
     *
     * @param workingDir
     */
    private void createInfoFile(File workingDir) {
        File info = new File(DaemonCommons.getRunnerHomeDir(workingDir, true), DaemonCommons.INFO_FILE);
        int count = 10;
        while (info.exists() && count > 0) {
            info.delete();
            count--;
        }
        try {
            info.createNewFile();
            String content = DaemonCommons.OPT_SHUTDOWN_CMD + "=" + DaemonCommons.shutdown +
                    DaemonCommons.NEWLINE + DaemonCommons.OPT_SHUTDOWN_PORT + "=" + DaemonCommons.shutdownPort;
            DaemonCommons.writeToFile(info, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    /**
     * Stops the running instance of the Daemon and Pax Runner if any.
     */
    public void stop() {
        if (shutdownHook != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            LOG.trace("Removed Shutdown Hook.");
            shutdownHook.run();
            shutdownHook = null;
        } else {
            shutdown();
        }
    }

    /**
     * Handles a individual client on the given socket as a separate thread. If
     * the client is connecting from a loopback adapter (localhost), no password is required.
     * If the client connects via telnet, password is required to issue commands.
     * The shutdown sequence begins if the client enters the correct shutdown command.
     *
     * @author Thomas Joseph.
     */
    class ClientHandler {
        public void handle(final Socket socket) {
            new Thread(
                    new Runnable() {
                        public void run() {
                            boolean isLocalConnect = false;
                            InputStream stream = null;
                            PrintWriter out = null;
                            try {
                                isLocalConnect = socket.getInetAddress().isLoopbackAddress();
                                socket.setSoTimeout(networkTimeout);
                                LOG.trace("Connected.");
                                stream = socket.getInputStream();
                                out = new PrintWriter(socket.getOutputStream(), true);
                            } catch (AccessControlException ace) {
                                LOG.warn("StandardServer.accept security exception: "
                                        + ace.getMessage(), ace);
                                return;
                            } catch (IOException e) {
                                //LOG.error("StandardServer.await: accept: ", e);
                                return;
                            }

                            // Read a set of characters from the socket
                            String command = readStream(stream);
                            LOG.trace("Recieved Command [ " + command + "] from ["
                                    + socket.getRemoteSocketAddress() + "].");

                            try {
                                String[] commands = command.split("\\\\s+");
                                if (commands.length > 1) {
                                    command = commands[0];
                                }
                            } catch (Exception e) {
                                // ignore
                            }

                            // Match against our command string
                            boolean match = command.toString().equals(DaemonCommons.shutdown);
                            if (match) {
                                LOG.trace("Shutdown command recieved via Telnet.");
                                stop();
                                return;
                            } else {
                                LOG.warn("Pax Runner: Invalid command.");
                                out.write("Invalid Command!" + DaemonCommons.NEWLINE);
                                out.flush();
                            }

                            // Close the socket now that we are done with it
                            try {
                                LOG.trace("Closing the socket now that we are done with it...");
                                socket.close();
                            } catch (IOException e) {
                                LOG.warn("Exception in closing socket..", e);
                            }
                        }
                    }, "ClientHandler-" + socket.getRemoteSocketAddress()
            ).start();
        }

        /**
         * Reads the given stream for string commands.
         *
         * @param stream
         * @return the command that was issued in the input stream.
         */
        private String readStream(final InputStream stream) {
            StringBuffer command = new StringBuffer();
            int expected = 1024; // Cut off to avoid DoS attack
            while (stream != null && expected > 0) {
                int ch = -1;
                try {
                    ch = stream.read();
                } catch (IOException e) {
                    ch = -1;
                }
                if (ch < 32) { // Control character or EOF terminates loop
                    break;
                }
                command.append((char) ch);
                expected--;
            }
            return command.toString();
        }
    }
}
