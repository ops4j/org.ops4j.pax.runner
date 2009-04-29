package org.ops4j.pax.runner.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.AccessControlException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.net.Base64Encoder;
import org.ops4j.pax.runner.CommandLine;
import org.ops4j.pax.runner.CommandLineImpl;
import org.ops4j.pax.runner.Run;
import org.ops4j.pax.runner.platform.DefaultJavaRunner;
import org.ops4j.pax.runner.platform.StoppableJavaRunner;

/**
 * @author <a href="mailto:open4thomas@gmail.com">Thomas Joseph</a>
 *
 */
public class Daemon {

    // Constants -----------------------------------------------------
    public static final String PASSWORD_FILE = "password.file";
    private static final String PID_FILE = "runner.pid"; 

    // Attributes ----------------------------------------------------
    private CommandLine commandLine = null;
    private String[] cmdArgs = null;
    private StoppableJavaRunner runner = null;
    private Thread shutdownHook = null;
    private int networkTimeout = 0;
    private long shutdownTimeout = 0;
    private ServerSocket serverSocket = null;
    private boolean continueAwait = true;

    private static Daemon instance = null;
    private static String shutdown = "shutdown";
    private static int shutdownPort = 8008;

    // Static --------------------------------------------------------
    /** logger. */
    private static final Log LOG = LogFactory.getLog(Daemon.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        getInstance();
        instance.load(args);
        instance.start();
    }

    public static Daemon getInstance() {
        if (instance == null)
            instance = new Daemon();
        return instance;
    }

    public static boolean isDaemonStarted() {
        File pid = new File(PID_FILE);
        if (pid.exists() && pid.isFile()) {
            return true;
        }
        return false;
    }

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------
    public void load(String... args) {
        cmdArgs = args;
        commandLine = new CommandLineImpl(args);
    }

    public void start() {
        new RunnerLauncher().start();
        await();
    }

    public void stop() {
        if (shutdownHook != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            LOG.debug("Removed Shutdown Hook.");
            shutdownHook.run();
            shutdownHook = null;
        }
        LOG.info("Bringing down Runner..."+runner);
        runner.shutdown();
        LOG.info("Pax Runner stopped");
        runner = null;
    }

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------
    /**
     * Retrieves the file that stores password information for the current
     * configuration.
     * 
     * @return The password information file.
     */
    static File getPasswordFile(String passwordFilePath) {
        if (passwordFilePath == null || passwordFilePath.length() == 0) {
            passwordFilePath = PASSWORD_FILE;
        }
        return new File(passwordFilePath);
    }

    /**
     * Encrypts and returns the encrypted string for the given message.
     * 
     * @param message the pass phrase that is to be encrypted.
     * @return the encrypted string for the given message.
     */
    static String encrypt(String message) {
        java.security.MessageDigest d =null;
        try {
            d = java.security.MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        d.reset();
        d.update(message.getBytes());
        return new String(Base64Encoder.encode(d.digest()));
    }

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    private void await() {
        createPIDFile();
        shutdownHook = createShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        // Set up a server socket to wait on
        try {
            LOG.debug("Setting up shutdown port on " + getShutdownPort());
            serverSocket =
                new ServerSocket(getShutdownPort());
        } catch (IOException e) {
            throw new RuntimeException("Unable to set up shutdown port ["
                    + getShutdownPort()
                    + "].", e);
        }

        // Loop waiting for a connection and a valid command
        while (continueAwait) {
            // Wait for the next connection
            try {
                // handle each new connection in a separate thread
                new ClientHandler().handle(serverSocket.accept());
            } catch (IOException e) {
                LOG.warn("Stopped accepting connections." + e.getMessage());
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
        LOG.debug("Finished awaiting...");
    }

    private void stopAwait() {
        continueAwait = false;
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            ;
        }
        serverSocket = null;
        LOG.info("Stopped shutdown port.");
    }

    private StoppableJavaRunner createJavaRunner()
    {
        return new DefaultJavaRunner();
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
                    stopAwait();
                }
            }, "Pax Runner Daemon Shutdown Hook"
        );
    }

    private void createPIDFile() {
        File currentDir =  new File(System.getProperty("user.dir"));
        File pid = new File(currentDir, PID_FILE);
        if (pid.exists()) {
            throw new RuntimeException(PID_FILE + " exists. Please make sure" +
            		" that the Pax Runner is not already running.");
        }
        try {
            pid.createNewFile();
            pid.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner classes -------------------------------------------------
    class RunnerLauncher extends Thread {
        RunnerLauncher () {
            super("RunnerLauncher");
            this.setDaemon(true);
        }

        public void run() {
            if (runner == null) {
                runner = createJavaRunner();
                System.out.println("Created runner " + runner);
            }
            Run.main(runner, cmdArgs);
            instance.stop();
        }
    }

    /**
     * Handles a individual client on the given socket as a separate thread. If
     * the client is connecting from a loopback adapter (localhost), no password is required.
     * If the client connects via telnet, password is required to issue commands.
     * The shutdown sequence begins if the client enters the correct shutdown command.
     * 
     * @author Thomas Joseph.
     *
     */
    class ClientHandler {
        public void handle(final Socket socket) {
            new Thread(
                new Runnable () {
                    public void run() {
                        boolean isLocalConnect = false;
                        InputStream stream = null;
                        PrintWriter out = null;
                        try {
                            isLocalConnect = socket.getInetAddress().isLoopbackAddress();
                            socket.setSoTimeout(getNetworkTimeout());
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

                        // ASK FOR PASSWORD for remote system
                        if (!isLocalConnect) {
                            // Write a welcome note
                            String welcome = "Welcome to Pax-Runner Remote Console";
                            String underline = "========================================";
                            out.write(underline.substring(0, welcome.length()) + "\n");
                            out.write(welcome + "\n");
                            out.write(underline.substring(0, welcome.length()) + "\n");
                            out.flush();

                            String passwordHash = new String(readEncryptedPassword());
                            if (passwordHash != null && passwordHash.trim().length() > 0) {
                                int count = 3;
                                boolean matched = false;
                                while (count > 0) {
                                    --count;
                                    out.write("Please enter password: ");
                                    out.flush();
                                    String clientPassword = readStream(stream);
                                    readStream(stream); // skipping control character
                                    clientPassword = new String(encrypt(clientPassword));
                                    if (passwordHash.trim().equals(clientPassword.trim())) {
                                        matched = true;
                                        break;
                                    }
                                }
                                if (!matched) {
                                    out.write("Invalid password attempts exceeded limits.\n");
                                    out.write("Try connecting again.\n");
                                    out.flush();
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        ;
                                    }
                                    return;
                                }
                            }
                        }

                        LOG.trace("Going to ask the client to supply command.");
                        out.write("Enter the command to shutdown the Pax Runner:\n");
                        out.write("> ");
                        out.flush();

                        // Read a set of characters from the socket
                        String command = readStream(stream);
                        LOG.debug("Recieved Command [ " + command + "] from ["
                                +socket.getRemoteSocketAddress() + "].");

                        // Close the socket now that we are done with it
                        try {
                            LOG.trace("Closing the socket now that we are done with it...");
                            socket.close();
                        } catch (IOException e) {
                            LOG.warn("Exception in closing socket..", e);
                        }

                        long timeout = getShutdownTimeout();
                        try {
                            String[] commands = command.split("\\\\s+");
                            if (commands.length > 1) {
                                command = commands[0];
                                timeout = Integer.parseInt(commands[1]);
                                setShutdownTimeout(timeout);
                            }
                        } catch (Exception e) {
                            ;// ignore
                        }

                        // Match against our command string
                        boolean match = command.toString().equals("shutdown");
                        if (match) {
                            LOG.debug("Shutdown command recieved via Telnet.");
                            stop();
                            return;
                        } else {
                            LOG.warn("Pax Runner: Invalid command.");
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
            while (stream!=null && expected > 0) {
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

    private String readEncryptedPassword() {
        return readFile(getPasswordFile(commandLine.getOption(Daemon.PASSWORD_FILE)));
    }

    private String readFile(File file) {
        try {
            LOG.debug("Read file:" + file.getAbsolutePath());
            BufferedReader br = new BufferedReader(new FileReader(file));
            return br.readLine();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Getters and Setters -----------------------------------------------------
    private CommandLine getCommandLine() {
        return commandLine;
    }

    private void setCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    private String[] getCmdArgs() {
        return cmdArgs;
    }

    private void setCmdArgs(String[] cmdArgs) {
        this.cmdArgs = cmdArgs;
    }

    private StoppableJavaRunner getRunner() {
        return runner;
    }

    private void setRunner(StoppableJavaRunner runner) {
        this.runner = runner;
    }

    static int getShutdownPort() {
        return shutdownPort;
    }

    private void setShutdownPort(int shutdownPort) {
        Daemon.shutdownPort = shutdownPort;
    }

    private long getShutdownTimeout() {
        return shutdownTimeout;
    }

    private void setShutdownTimeout(long shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    private int getNetworkTimeout() {
        return networkTimeout;
    }

    private void setNetworkTimeout(int networkTimeout) {
        this.networkTimeout = networkTimeout;
    }

    static String getShutdown() {
        return shutdown;
    }

    private static void setShutdown(String shutdown) {
        Daemon.shutdown = shutdown;
    }
}

