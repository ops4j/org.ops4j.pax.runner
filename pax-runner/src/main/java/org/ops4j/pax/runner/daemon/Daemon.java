package org.ops4j.pax.runner.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.AccessControlException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.net.Base64Encoder;
import org.ops4j.pax.runner.CommandLine;
import org.ops4j.pax.runner.CommandLineImpl;
import org.ops4j.pax.runner.Run;
import org.ops4j.pax.runner.platform.DefaultJavaRunner;
import org.ops4j.pax.runner.platform.StoppableJavaRunner;

/**
 * The class that runs as Daemon for the Pax Runner. The {@link DaemonLauncher}
 * launches the instance of this class either as an attached or a detached process.
 * In either case, the instance of this class launches the Pax-Runner and waits
 * for appropriate shutdown command to be issued on its shutdown port.
 * <br/>
 * The Daemon process terminates either when the Pax-Runner completes execution,
 * or when a shutdown command is issued via shutdown port.
 * 
 * @author <a href="mailto:open4thomas@gmail.com">Thomas Joseph</a>
 *
 */
public class Daemon {

    // Constants -----------------------------------------------------
    /**
     * Configuration option to specify password file, if not specified, the name
     * of the default password file.
     */
    public static final String PASSWORD_FILE = "org.ops4j.pax.runner.daemon.password.file";
    private static final String LOCK_FILE = "org.ops4j.pax.runner.daemon.lock"; 
    private static final String NEWLINE= "\r\n";
    private static final String OPT_NETWORK_TIMEOUT="org.ops4j.pax.runner.daemon.network.timeout";
    private static final String OPT_SHUTDOWN_TIMEOUT = "org.ops4j.pax.runner.daemon.shutdown.timeout";
    private static final String OPT_SHUTDOWN_CMD = "org.ops4j.pax.runner.daemon.shutdown.cmd";
    private static final String OPT_SHUTDOWN_PORT = "org.ops4j.pax.runner.daemon.shutdown.port";

    // Attributes ----------------------------------------------------
    private CommandLine commandLine = null;
    private String[] cmdArgs = null;
    private StoppableJavaRunner runner = null;
    private Thread shutdownHook = null;
    private int networkTimeout = 1000*60;
    private long shutdownTimeout = 0;
    private ServerSocket serverSocket = null;
    private boolean continueAwait = true;
    private File lockFile = null;

    private static Daemon instance = null;
    private static String shutdown = "shutdown";
    private static int shutdownPort = 8008;

    // Static --------------------------------------------------------
    /** logger. */
    private static final Log LOG = LogFactory.getLog(Daemon.class);

    /**
     * Launches a new Daemon instance, that will bring up the Pax Runner.
     */
    public static void main(String[] args) {
        getInstance();
        instance.load(args);
        instance.start();
    }

    /**
     * @return The singleton instance of the Daemon.
     */
    public static Daemon getInstance() {
        if (instance == null)
            instance = new Daemon();
        return instance;
    }

    /**
     * Determines if any instance of the Daemon is already started.
     * 
     * @return True if any instance of this class is already started, false otherwise.
     */
    public static boolean isDaemonStarted() {
        File lock = new File(LOCK_FILE);
        if (lock.exists() && lock.isFile()) {
            return true;
        }
        return false;
    }

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------
    /**
     * Prepares the instance of this class with configurations, before launching
     * the Pax-Runner.
     * 
     * @param args The arguments passed to start this instance.
     */
    public void load(String... args) {
        cmdArgs = args;
        commandLine = new CommandLineImpl(args);
        setNetworkTimeout(commandLine.getOption(OPT_NETWORK_TIMEOUT));
        setShutdown(commandLine.getOption(OPT_SHUTDOWN_CMD));
        setShutdownPort(commandLine.getOption(OPT_SHUTDOWN_PORT));
        setShutdownTimeout(commandLine.getOption(OPT_SHUTDOWN_TIMEOUT));
    }

    /**
     * Starts the Daemon - Launches Pax Runner, Opens up shutdown port on which
     * it will listen to shutdown command.
     * 
     */
    public void start() {
        new RunnerLauncher().start();
        await();
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
        }
        if (runner != null) {
            LOG.debug("Bringing down Runner...");
            runner.shutdown();
            runner = null;
        }
        LOG.info("Pax Runner daemon stopped");
    }

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

    /**
     * Creates a file on the file system with the given content for the file.
     * 
     * @param file The file that should be written to the filesystem.
     * @param content The content for the file.
     */
    static void writeToFile(File file, String content) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            fw.write(content);
        } catch (IOException e) {
            throw new RuntimeException ("Error creating file.",e);
        } finally {
            try { 
                if(fw !=  null)
                    fw.close();
                fw=null;
            }
            catch (IOException e) {/* ignore */}
        }
    }

    /**
     * 
     * @return The shutdown port that was configured (if any) to launch the
     * Daemon (if it was launched), the default port otherwise
     * 
     * @see #OPT_SHUTDOWN_PORT
     */
    static int getShutdownPort() {
        int port = shutdownPort;
        String sPort = readDaemonProperty(OPT_SHUTDOWN_PORT);
        if ((port = parseSafeInt( sPort)) != -1)
            return port;
        return shutdownPort;
    }

    /**
     * Returns the command that must be issued to the shutdown port to stop the
     * launched Daemon instance.
     * 
     * @return The shutdown command that was configured (if any) when launching the
     * Daemon instance (if it was launched), the default command otherwise
     * 
     * @see #OPT_SHUTDOWN_CMD 
     */
    static String getShutdown() {
        String read = readDaemonProperty(OPT_SHUTDOWN_CMD);
        if (read!=null && read.length() > 0)
            return read;
        return shutdown;
    }

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    private void await() {
        lockFile = createLockFile();
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

    private void stopAwait() {
        continueAwait = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
                LOG.info("Stopped shutdown port.");
            }
        } catch (IOException e) {
            ;
        }
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
                    if (lockFile != null && lockFile.exists() ) {
                        // file.deleteOnExit() do not seem to consistently work on Windows
                        lockFile.delete();
                        lockFile = null;
                    }
                }
            }, "Pax Runner Daemon Shutdown Hook"
        );
    }

    private File createLockFile() {
        File currentDir =  new File(System.getProperty("user.dir"));
        File lock = new File(currentDir, LOCK_FILE);
        if (lock.exists()) {
            throw new RuntimeException(LOCK_FILE + " exists. Please make sure" +
            		" that the Pax Runner daemon is not already running.");
        }
        try {
            lock.createNewFile();
            String content = OPT_SHUTDOWN_CMD + "=" + shutdown +
                NEWLINE + OPT_SHUTDOWN_PORT + "=" + shutdownPort;
            Daemon.writeToFile(lock, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lock;
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

    private void setShutdownPort(String shutdownPort) {
        if (parseSafeInt(shutdownPort) != -1) {
            Daemon.shutdownPort = parseSafeInt(shutdownPort);
        }
    }

    private void setNetworkTimeout(String networkTimeout) {
        if (parseSafeInt(networkTimeout) != -1) {
            this.networkTimeout = parseSafeInt(networkTimeout);
        }
    }

    private void setShutdownTimeout(String shutdownTimeout) {
        if (parseSafeInt(shutdownTimeout) != -1) {
            this.shutdownTimeout = parseSafeInt(shutdownTimeout);
        }
    }

    private static int parseSafeInt(String arg) {
        if (arg != null && arg.length() > 0) {
            try {
                return Integer.parseInt(arg);
            } catch(Exception e) {}
        }
        return -1;
    }

    private static String readDaemonProperty(String key) {
        File currentDir =  new File(System.getProperty("user.dir"));
        File lock = new File(currentDir, LOCK_FILE);
        if (lock.exists()) {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(lock));
                return props.getProperty(key);
            } catch (FileNotFoundException e) {;}
              catch (IOException e) {;}
        }
        return null;
    }

    // Inner classes -------------------------------------------------
    /**
     * Launches the Pax Runner in a separate thread within the same process.
     */
    class RunnerLauncher extends Thread {
        RunnerLauncher () {
            super("RunnerLauncher");
            this.setDaemon(true);
        }

        public void run() {
            if (runner == null) {
                runner = createJavaRunner();
                LOG.trace("Created Runner.");
            }
            Run.main(runner, cmdArgs);
            instance.stopAwait();
            LOG.info("Pax Runner daemon stopped");
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

                        // ASK FOR PASSWORD for remote system
                        if (!isLocalConnect) {
                            // Write a welcome note
                            String welcome = "Welcome to Pax-Runner Remote Console";
                            String underline = "===============================================";
                            out.write(underline.substring(0, welcome.length()) + NEWLINE);
                            out.write(welcome + NEWLINE);
                            out.write(underline.substring(0, welcome.length()) + NEWLINE);
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
                                    out.write("Invalid password attempts exceeded limits."+NEWLINE);
                                    out.write("Try connecting again."+NEWLINE);
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
                        out.write("Enter the command to shutdown the Pax Runner:"+NEWLINE);
                        out.write("> ");
                        out.flush();

                        // Read a set of characters from the socket
                        String command = readStream(stream);
                        LOG.trace("Recieved Command [ " + command + "] from ["
                                +socket.getRemoteSocketAddress() + "].");

                        try {
                            String[] commands = command.split("\\\\s+");
                            if (commands.length > 1) {
                                command = commands[0];
                                setShutdownTimeout(command);
                            }
                        } catch (Exception e) {
                            ;// ignore
                        }

                        // Match against our command string
                        boolean match = command.toString().equals(shutdown);
                        if (match) {
                            LOG.trace("Shutdown command recieved via Telnet.");
                            LOG.trace("Stop after " + shutdownTimeout + "ms.");
                            try {
                                Thread.sleep(shutdownTimeout);
                            } catch (InterruptedException e) {
                                LOG.warn("Problems in shutdown timeout.");
                            }
                            stop();
                            return;
                        } else {
                            LOG.warn("Pax Runner: Invalid command.");
                            out.write("Invalid Command!"+NEWLINE);
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

    // Getters and Setters -----------------------------------------------------
    private void setShutdown(String shutdown) {
        if (shutdown != null && shutdown.length() > 0)
            Daemon.shutdown = shutdown;
    }
}

