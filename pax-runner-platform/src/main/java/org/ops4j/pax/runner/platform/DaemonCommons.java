package org.ops4j.pax.runner.platform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.Properties;

/**
 * @author dpishchukhin
 */
class DaemonCommons {
    private static final Log LOG = LogFactory.getLog(DaemonCommons.class);

    static final String OPT_SHUTDOWN_CMD = "org.ops4j.pax.runner.platform.daemon.shutdown.cmd";
    static final String OPT_SHUTDOWN_PORT = "org.ops4j.pax.runner.platform.daemon.shutdown.port";
    static final String NEWLINE = "\r\n";
    static final String INFO_FILE = "org.ops4j.pax.runner.platform.daemon.info";
    static final String LOCK_FILE = "org.ops4j.pax.runner.platform.daemon.lock";
    static String shutdown = "shutdown";
    static int shutdownPort = 8008;

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
     * Creates a file on the file system with the given content for the file.
     *
     * @param file    The file that should be written to the filesystem.
     * @param content The content for the file.
     */
    static void writeToFile(File file, String content) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            fw.write(content);
            fw.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error creating file.", e);
        } finally {
            try {
                if (fw != null)
                    fw.close();
                fw = null;
            } catch (IOException e) {/* ignore */}
        }
    }

    static String readDaemonProperty(File workingDir, String key) {
        File info = new File(getRunnerHomeDir(workingDir, false), INFO_FILE);
        if (info.exists()) {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(info));
                return props.getProperty(key);
            } catch (FileNotFoundException e) {
                // do nothing
            } catch (IOException e) {
                // do nothing
            }
        }
        return null;
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

    static int parseSafeInt(String arg) {
        if (arg != null && arg.length() > 0) {
            try {
                return Integer.parseInt(arg);
            } catch (Exception e) {
            }
        }
        return -1;
    }

    /**
     * @param workingDir
     * @return The shutdown port that was configured (if any) to launch the
     *         Daemon (if it was launched), the default port otherwise
     * @see DaemonCommons#OPT_SHUTDOWN_PORT
     */
    static int getShutdownPort(File workingDir) {
        int port;
        String sPort = readDaemonProperty(workingDir, OPT_SHUTDOWN_PORT);
        if ((port = parseSafeInt(sPort)) != -1)
            return port;
        return shutdownPort;
    }

    /**
     * Returns the command that must be issued to the shutdown port to stop the
     * launched Daemon instance.
     *
     * @param workingDir
     * @return The shutdown command that was configured (if any) when launching the
     *         Daemon instance (if it was launched), the default command otherwise
     * @see DaemonCommons#OPT_SHUTDOWN_CMD
     */
    static String getShutdown(File workingDir) {
        String read = readDaemonProperty(workingDir, OPT_SHUTDOWN_CMD);
        if (read != null && read.length() > 0)
            return read;
        return shutdown;
    }
}
