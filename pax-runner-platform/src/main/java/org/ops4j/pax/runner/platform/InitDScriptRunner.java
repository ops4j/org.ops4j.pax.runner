/**
 * 
 */
package org.ops4j.pax.runner.platform;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.platform.internal.CommandLineBuilder;

/**
 * @author Matthias Kuespert
 * @since 
 */
public class InitDScriptRunner implements JavaRunner {
	
    private static final Log    LOG                                   = LogFactory.getLog(DefaultJavaRunner.class);

    private static final String INITD_SCRIPT_TEMPLATE                 = "/templates/init.d-script.txt";

    private static final String INITD_SCRIPT_TEMPLATE_APPNAME         = "PAX_RUNNER_APPNAME";
    private static final String INITD_SCRIPT_TEMPLATE_APPNAME_DEFAULT = "osgiapp";

    private static final String INITD_SCRIPT_TEMPLATE_APPROOT         = "PAX_RUNNER_APPROOT";

    private static final String INITD_SCRIPT_TEMPLATE_STARTCODE       = "PAX_RUNNER_STARTCODE";

    private String              m_applicationName                     = INITD_SCRIPT_TEMPLATE_APPNAME_DEFAULT;
	
	/**
	 * Constructor
	 * 
	 * @param applicationName
	 *            The name of the application started by the script. Also
	 *            defines the name of the generated script.
	 */
	public InitDScriptRunner(String applicationName) {
        if (null != applicationName && applicationName.length() > 0) {
			m_applicationName = applicationName;
		}
	}

    public void exec( String[] vmOptions, String[] classpath, String mainClass, String[] programOptions, String javaHome, File workingDir )
        throws PlatformException
    {
        exec( vmOptions, classpath, mainClass, programOptions,javaHome,workingDir,new String[0] );
    }

    /**
	 * retrieve template, replace some values and write to m_applicationName
	 */
	public void exec(String[] vmOptions,
			         String[] classpath,
			         String   mainClass,
			         String[] programOptions,
			         String   javaHome,
			         File     workingDir,
                     String[] environmentVariables )
			throws PlatformException {
	    File scriptFile = new File(workingDir, m_applicationName);
		try {
		    LOG.debug("creating init.d script " + scriptFile.getAbsolutePath() + " from template " + INITD_SCRIPT_TEMPLATE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(INITD_SCRIPT_TEMPLATE)));
			BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));
			// replace PAX_RUNNER_ placeholders
			while (true) {
				String line = reader.readLine();
				if (null == line) {
				    writer.close();
				    reader.close();
				    break;
				}
				// here we go ...
                if (line.contains(INITD_SCRIPT_TEMPLATE_APPNAME)) {
                    line = line.replace(INITD_SCRIPT_TEMPLATE_APPNAME, m_applicationName);
                }
                if (line.endsWith(INITD_SCRIPT_TEMPLATE_APPROOT)) {
                    line = line.replace(INITD_SCRIPT_TEMPLATE_APPROOT, workingDir.getAbsolutePath());
                }
				if (line.startsWith(INITD_SCRIPT_TEMPLATE_STARTCODE)) {
				    // build classpath
			        final StringBuilder cp = new StringBuilder();
                    for (String path : classpath) {
                        if (cp.length() == 0) {
                            cp.append("-cp ");
                        } else {
                            cp.append(File.pathSeparator);
                        }
                        cp.append(path);
                    }
		            // build commandline
		            final CommandLineBuilder commandOptions = new CommandLineBuilder()
                                                                  .append(vmOptions)
                                                                  .append(cp.toString())
                                                                  .append(mainClass)
                                                                  .append(programOptions);
			        StringBuilder startCommand = new StringBuilder();
		            for (String part : commandOptions.toArray()) {
		                if (startCommand.length() == 0) {
                            startCommand.append("java ");
		                } else {
                            startCommand.append(" \\\n         ");
		                }
		                startCommand.append(part);
		            }
				    line = "    " + startCommand + " >${log_file} 2>&1 &";
				}
                // TODO add env vars properly.
                
				writer.write(line + "\n");
			}
		} catch (Exception e) {
		    throw new PlatformException("Error creating init.d script: " + e.getMessage(), e);
		}
	}
}
