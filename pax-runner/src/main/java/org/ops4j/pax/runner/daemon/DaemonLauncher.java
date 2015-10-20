package org.ops4j.pax.runner.daemon;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;

import org.ops4j.io.Pipe;
import org.ops4j.pax.runner.CommandLine;
import org.ops4j.pax.runner.CommandLineImpl;
import org.ops4j.pax.runner.Run;
import org.ops4j.pax.runner.User;

/**
 * An entry point to start and stop the Daemon from the CLI. Based on the
 * parameters supplied, the Pax Runner can:
 * <ul>
 *      <li>Start attached to the console.</li>
 *      <li>Start detached from the console.</li>
 *      <li>Stop the Pax Runner Daemon launched, if any.</li>
 * </ul>
 * 
 * @author <a href="mailto:open4thomas@gmail.com">Thomas Joseph</a>
 * @since 0.20.0 (29 April 2009)
 *
 */
public class DaemonLauncher {

    // Constants -----------------------------------------------------
    // first 3 could be moved to CommanLine?
    public static final String OPTION_START = "--start";
    public static final String OPTION_STARTD = "--startd";
    public static final String OPTION_STOP = "--stop";

    public static final String SPACE = " ";

    // Attributes ----------------------------------------------------
    private CommandLine commandLine = null;
    private String[] cmdArgs = null;
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
                    " Daemon. The specified option should be first argument.\n" +
                    "Valid options: "+ OPTION_START +" | " + OPTION_STARTD
                    +" | " + OPTION_STOP);
        }
    }

    private void load(String[] args) {
        cmdArgs = args;
        commandLine = new CommandLineImpl(args);
    }

    private void start() {
        checkPasswordFile();
        Daemon.main(cmdArgs);
    }

    private void startd() {
        checkPasswordFile();
        Thread newProc = new Thread( new Runnable() {
            public void run() {
                try {
                    String cp = System.getProperty("java.class.path") == null? ""
                            : "-cp "+System.getProperty("java.class.path");
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
                    }, "Pax-Runner Daemon Shutdown Hook"
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
        if (Daemon.isDaemonStarted()) {
            Socket socket = null;
            PrintWriter out = null;
            try {
                socket = new Socket("localhost", Daemon.getShutdownPort());
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (UnknownHostException e) {
                LOG.error("Unknown address: localhost.");
                return;
            } catch (IOException e) {
                LOG.error("Couldn't connect to: localhost.");
                return;
            }

            final String shutdownCmd = Daemon.getShutdown();
            out.write(shutdownCmd +"\n");
            out.flush();
            LOG.debug("Pax Runner Daemon: Shutdown command issued:"+ shutdownCmd);
            do {
                LOG.info("Pax Runner Daemon: Shutdown in progress...");
                try {
                    Thread.sleep(1000 * 2);
                } catch (InterruptedException e) {
                    ;
                }
            } while (Daemon.isDaemonStarted());
            LOG.info("Pax Runner Daemon Stopped.");
        } else {
            LOG.warn("No Daemons yet launched!");
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    /**
     * Checks if a password has been set for the current configuration. If
     * password not set, a prompt is provided to enter new password.
     */
    private void checkPasswordFile() {
        File passwordFile = Daemon.getPasswordFile(commandLine.getOption(Daemon.PASSWORD_FILE));

        if(!passwordFile.exists()) {
            try {
                LOG.warn("Password for this configuration is not set.");
                boolean done = false;
                do {
                    LOG.warn("Please enter a new password: ");
                    // Console c = System.console(); Works for Java 6 only
                    String response = User.ask();
                    if (response.trim().length() == 0) {
                        passwordFile.createNewFile();
                        done = true;
                    } else {
                        LOG.warn("Please re-enter the new password: ");
                        String confirm = User.ask();
                        if (confirm.equals(response)) {
                            response = Daemon.encrypt(response);
                            passwordFile.createNewFile();
                            Daemon.writeToFile(passwordFile, response);
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
        }
    }

    // Getters and Setters -------------------------------------------

    // Inner classes -------------------------------------------------
}

