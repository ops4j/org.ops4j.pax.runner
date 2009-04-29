package org.ops4j.pax.runner.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;

import org.ops4j.io.Pipe;
import org.ops4j.net.Base64Encoder;
import org.ops4j.pax.runner.CommandLine;
import org.ops4j.pax.runner.CommandLineImpl;
import org.ops4j.pax.runner.Run;
import org.ops4j.pax.runner.User;

/**
 * An entry point to start and stop the DaemonImpl.
 * 
 * @author <a href="mailto:open4thomas@gmail.com">Thomas Joseph</a>
 *
 */
public class DaemonLauncher {

    // Constants -----------------------------------------------------
    // first 3 could be moved to CommanLine?
    public static final String OPTION_START = "--start";
    public static final String OPTION_STARTD = "--startd";
    public static final String OPTION_STOP = "--stop";
    public static final String OPTION_PASSWORD_ENC = "--password.encrypted";

    public static final String SPACE = " ";

    // Attributes ----------------------------------------------------
    private CommandLine commandLine = null;
    private String[] cmdArgs = null;
    //private String passwordEncrypted = "";
    private static DaemonLauncher instance = null; 

    // Static --------------------------------------------------------
    /** logger. */
    private static final Log LOG = Run.getLogger();

    public static final DaemonLauncher getInstance() {
        if (instance == null)
            instance = new DaemonLauncher();
        return instance;
    }

    // Constructors --------------------------------------------------
    private DaemonLauncher() {
        super();
    }

    // Public --------------------------------------------------------
    /**
     * @param args
     */
    public static void main(String[] args) {
        final String operation = args==null || args.length == 0 ? null:args[0];
        DaemonLauncher launcher = getInstance();
        if (OPTION_START.equals(operation)) {
            launcher.load(args);
            launcher.start();
        } else if (OPTION_STARTD.equals(operation)) {
            launcher.load(args);
            launcher.startd();
        } else if (OPTION_STOP.equals(operation)) {
            launcher.load(args);
            launcher.stop();
        } else {
            throw new RuntimeException("No valid option specified for Pax Runner" +
                    " DaemonImpl. The specified option should be first argument.\n" +
                    "Valid options: "+ OPTION_START +" | " + OPTION_STARTD
                    +" | " + OPTION_STOP);
        }
    }

    private void load(String[] args) {
        cmdArgs = args;
        commandLine = new CommandLineImpl(args);
    }

    private void start() {
        Daemon.main(cmdArgs);
    }

    private void startd() {
        checkPasswordFile();
        Thread newProc = new Thread( new Runnable() {
            public void run() {
                try {
                    String cp = System.getProperty("java.class.path") == null? ""
                            : "-cp "+System.getProperty("java.class.path");
                    System.out.println(">>>> " + Daemon.class.toString());
                    String strCmd = "java" + SPACE
                        + cp + SPACE
                        + "org.ops4j.pax.runner.daemon.Daemon";
                    for(String cmd: cmdArgs) {
                        strCmd = strCmd + SPACE + cmd;
                    }
                    LOG.debug("Starting command line: " + strCmd);

                    Process proc = Runtime.getRuntime().exec(strCmd);
                    Runtime.getRuntime().addShutdownHook(createShutdownHook(proc));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            private Thread createShutdownHook(final Process process) {
                final Pipe errPipe = new Pipe( process.getErrorStream(), System.err ).start( "Error pipe" );
                final Pipe outPipe = new Pipe( process.getInputStream(), System.out ).start( "Out pipe" );
                final Pipe inPipe = new Pipe( process.getOutputStream(), System.in ).start( "In pipe" );
                return new Thread(
                    new Runnable() {
                        public void run() {
                            errPipe.stop();
                            outPipe.stop();
                            inPipe.stop();
                        }
                    }, "Pax-Runner DaemonImpl Shutdown Hook"
                );
            }
        }, "Pax-Runner DaemonLauncher");
        newProc.setDaemon(true);
        newProc.start();
        try {
            Thread.sleep(4000); // wait to see if there are any immediate problems...
            newProc.join();
        } catch (InterruptedException e) {
            LOG.warn("Problems in waiting for the launched thread.");
        }
        LOG.info("Pax-Runner Daemon launched !");
    }

    private void stop() {
        
    }

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------
    /**
     * Creates a file on the file system with the given content for the file.
     * 
     * @param file The file that should be written to the filesystem.
     * @param content The content for the file.
     */
    static void createFile(File file, String content) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            fw.write(content);
        } catch (IOException e) {
            throw new RuntimeException ("Error crearing password file.",e);
        } finally {
            try { if(fw !=  null) fw.close(); }
            catch (IOException e) {/* ignore */}
        }
    }
    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    /**
     * Checks if a password has been set for the current configuration. If
     * password not set, a prompt is provided to enter new password.
     */
    private void checkPasswordFile() {
        File passwordFile = Daemon.getPasswordFile(commandLine.getOption(Daemon.PASSWORD_FILE));

        if(! passwordFile.exists()) {
            try {
                LOG.warn("Password for this configuration is not set.");
                boolean done = false;
                do {
                    LOG.warn("Please enter a new password: ");
                    // Console c = System.console(); Works for Java 6 only
                    String response = User.ask();
                    if (response.trim().length() == 0) {
                        createFile(passwordFile, "");
                        done = true;
                    } else {
                        LOG.warn("Please re-enter the new password: ");
                        String confirm = User.ask();
                        if (confirm.equals(response)) {
                            response = Daemon.encrypt(response);
                            // passwordEncrypted = response;
                            createFile(passwordFile, response);
                            done = true;
                        } else {
                            LOG.warn("Passwords did not match.");
                        }
                    }
                } while (!done);

            } catch(Exception e) {
                throw new RuntimeException("Error setting password ", e);
            }
        } else {
            LOG.info("Password exists for this configuration.");
            try {
                BufferedReader br = new BufferedReader(new FileReader(passwordFile));
                // passwordEncrypted = br.readLine();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    // Getters and Setters -------------------------------------------

    // Inner classes -------------------------------------------------
}

