/*
 * Copyright (c) 2021. Enrico Daga and Luca Panziera
 *
 * MLicensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.basilapi.basil.it;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We develop our own executor inspired by the Stanbol commons.test JarExecutor,
 * which was a singleton.
 * 
 * Most of this code is copied from
 * org.apache.stanbol.commons.testing.jarexec.JarExecutor
 * 
 * @author ed4565
 *
 */
public class JarExecutor {

	private File jarToExecute;
	private String javaExecutable;
	private File workingDirectory;
	private int serverPort;

	private final Logger log = LoggerFactory.getLogger(getClass());

	public final int DEFAULT_PORT = 8765;
	public final String DEFAULT_JAR_FOLDER = "target/dependency";

	public String PROP_PREFIX ;
	public final String PROP_JAR_FOLDER = "jar.folder";
	public final String PROP_JAR_NAME_REGEXP = "jar.name.regexp";
	public final String PROP_JAR_ARGS = "jar.args";
	public final String PROP_VM_OPTIONS = "vm.options";
	public final String PROP_WORKING_DIRECTORY = "workingdirectory";

	private Properties config;

	@SuppressWarnings("serial")
	public static class ExecutorException extends Exception {

		ExecutorException(String reason) {
			super(reason);
		}

		ExecutorException(String reason, Throwable cause) {
			super(reason, cause);
		}
	}

	public JarExecutor(String propPrefix, Properties config) throws ExecutorException {
		PROP_PREFIX = propPrefix;
		this.config = config;
		init();
	}

	public JarExecutor(Properties config) throws ExecutorException {
		PROP_PREFIX = "jar.executor.";
		this.config = config;
		init();
	}

	public void init() throws ExecutorException {

		final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
		javaExecutable = isWindows ? "java.exe" : "java";
		String jarFolderPath = config.getProperty(PROP_PREFIX + PROP_JAR_FOLDER);
		jarFolderPath = isBlank(jarFolderPath) ? DEFAULT_JAR_FOLDER : jarFolderPath;
		final File jarFolder = new File(jarFolderPath);

		String jarNameRegexp = config.getProperty(PROP_PREFIX + PROP_JAR_NAME_REGEXP);
		if (isBlank(jarNameRegexp))
			throw new ExecutorException("Mandatory blank field: " + PROP_PREFIX + PROP_JAR_NAME_REGEXP);
		final Pattern jarPattern = Pattern.compile(jarNameRegexp);

		// Find executable jar
		final String[] candidates = jarFolder.list();
		if (candidates == null) {
			throw new ExecutorException("No files found in jar folder specified by " + PROP_PREFIX + PROP_JAR_FOLDER
					+ " property: " + jarFolder.getAbsolutePath());
		}
		File f = null;
		if (candidates != null) {
			for (String filename : candidates) {
				if (jarPattern.matcher(filename).matches()) {
					f = new File(jarFolder, filename);
					break;
				}
			}
		}

		if (f == null) {
			throw new ExecutorException("Executable jar matching '" + jarPattern + "' not found in "
					+ jarFolder.getAbsolutePath() + ", candidates are " + Arrays.asList(candidates));
		}
		jarToExecute = f;

		String workingDirectoryName = config.getProperty(PROP_PREFIX + PROP_WORKING_DIRECTORY);
		if (workingDirectoryName != null) {
			this.workingDirectory = new File(workingDirectoryName);
			if (!this.workingDirectory.exists()) {
				this.workingDirectory.mkdirs();
			} else {
				if (!this.workingDirectory.isDirectory()) {
					throw new ExecutorException(
							"Specified working directory " + workingDirectoryName + " is not a directory.");
				}

				if (!this.workingDirectory.canRead()) {
					throw new ExecutorException("Can't access specified working directory " + workingDirectoryName);
				}
			}
			log.info("Using " + this.workingDirectory.getAbsolutePath() + " as working directory");
		} else {
			this.workingDirectory = null;
		}
	}

	/**
	 * Start the jar if not done yet, and setup runtime hook to stop it.
	 */
	public void start() throws Exception {
		final ExecuteResultHandler h = new ExecuteResultHandler() {
			@Override
			public void onProcessFailed(ExecuteException ex) {
				log.error("Process execution failed:" + ex, ex);
			}

			@Override
			public void onProcessComplete(int result) {
				log.info("Process execution complete, exit code=" + result);
			}
		};

		final String vmOptions = config.getProperty(PROP_PREFIX + PROP_VM_OPTIONS);
		final Executor e = new DefaultExecutor();
		if (this.workingDirectory != null) {
			e.setWorkingDirectory(this.workingDirectory);
		}
		final CommandLine cl = new CommandLine(javaExecutable);
		if (vmOptions != null && vmOptions.length() > 0) {
			// TODO: this will fail if one of the vm options as a quoted value with a space
			// in it, but this is
			// not the case for common usage patterns
			for (String option : StringUtils.split(vmOptions, " ")) {
				cl.addArgument(option);
			}
		}
		cl.addArgument("-jar");
		cl.addArgument(jarToExecute.getAbsolutePath());
		cl.addArguments(config.getProperty(PROP_PREFIX + PROP_JAR_ARGS));
		log.info("Executing " + cl);
		e.setStreamHandler(new PumpStreamHandler());
		e.setProcessDestroyer(new ShutdownHookProcessDestroyer());
		e.execute(cl, h);
	}
}
