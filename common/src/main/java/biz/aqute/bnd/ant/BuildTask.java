package biz.aqute.bnd.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Iterator;

import biz.aqute.lib.osgi.eclipse.EclipseClasspath;
import biz.aqute.lib.osgi.Jar;
import biz.aqute.lib.osgi.Builder;
import biz.aqute.qtokens.QuotedTokenizer;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

public class BuildTask extends Task
{
	List	files		= new ArrayList();
	List	classpath	= new ArrayList();
	boolean	failok;
	boolean	exceptions;
	boolean	print;
	boolean	eclipse;

	public void execute() throws BuildException {
		try {
			if (files == null)
				throw new BuildException("No files set");

			List classpath = new ArrayList();
			if (eclipse) {
				File project = getProject().getBaseDir();
				EclipseClasspath cp = new EclipseClasspath(project
						.getParentFile(), project);
				classpath.addAll(cp.getClasspath());
			}

			Jar cp[] = new Jar[classpath.size()];
			int x = 0;
			for (Iterator e = classpath.iterator(); e.hasNext();) {
				File f = (File) e.next();
				if (f.exists())
					cp[x++] = new Jar(f.getName(), f);
				else
					System.out
							.println(f.getAbsolutePath() + "??? On Classpath");
			}

			System.err.println("Done " + files );
			for ( Iterator f = files.iterator(); f.hasNext();) {
				File file = (File) f.next();
				Builder builder = new Builder();

				// Do nice property calculations
				// merging includes etc.
				builder.setProperties(file);
				// get them and merge them with the project
				// properties
				Properties p = builder.getProperties();
				p.putAll(getProject().getProperties());
				builder.setProperties(p);

				builder.setClasspath(cp);

				builder.build();

				Jar jar = builder.getJar();
				String path = builder.getProperty("-output");
				if (path == null) {
					path = file.getAbsolutePath();
					int n = path.lastIndexOf('.');
					if (n > 0)
						path = path.substring(0, n) + ".jar";
					else
						path = path + ".jar";
				}
				if (builder.getWarnings().size() > 0) {
					System.out.println("Warnings");
					System.out.println(builder.getWarnings());
				}
				if (failok || builder.getErrors().isEmpty()) {
					jar.write(path);
				}
				else {
					System.out.println(builder.getErrors());
					throw new BuildException("Errors duing build: "  + builder.getErrors());
				}
			}
		}
		catch (Exception e) {
			// if (exceptions)
			e.printStackTrace();
			if (!failok)
				throw new BuildException("Failed to build jar file: ", e);
		}
	}

	public void setFiles(String files) {
		System.err.println("Donex: " + files );
		addAll(this.files, files);
		System.err.println("Donex: " + this.files );
	}

	void addAll(List list, String files) {
		QuotedTokenizer qt = new QuotedTokenizer(files, ",");
		String entries[] = qt.getTokens();
		File project = getProject().getBaseDir();
		for (int i = 0; i < entries.length; i++) {
			File f = new File(project, entries[i]);
			if (f.exists())
				list.add(f);
			else
				System.out.println(f.getAbsolutePath() + "? not on classpath");
		}
	}

	public void setClasspath(String files) {
		addAll(classpath, files);
	}

	public void setEclipse(boolean eclipse) {
		this.eclipse = eclipse;;
	}

	boolean isFailok() {
		return failok;
	}

	public void setFailok(boolean failok) {
		this.failok = failok;
	}

	boolean isExceptions() {
		return exceptions;
	}

	public void setExceptions(boolean exceptions) {
		this.exceptions = exceptions;
	}

	boolean isPrint() {
		return print;
	}

	void setPrint(boolean print) {
		this.print = print;
	}

}
