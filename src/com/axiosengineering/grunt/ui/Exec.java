package com.axiosengineering.grunt.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Timer;
import java.util.TimerTask;



/**
 * The Class Exec. Encapsulates an execution of an OS command. It executes the command,
 * returns the execution status, consumes the std.out and std.err streams, and returns
 * a string representation of them after the streams have been closed.
 */
public class Exec {

	/** A string representation of the output sent to srtd.out by the executing process. */
	private String outResult;

	/**  A string representation of the output sent to srtd.err by the executing process. */
	private String errResult;

	/** The process that executes the specified command. */
	private Process process;

	/** The process execution status. */
	private boolean failed = false;

	/** The stream reader for std.out. */
	StreamReader outReader;

	/** The stream reader for std.err. */
	StreamReader errReader;

	private InputStream stdOut;

	private InputStream stdErr;

	/**
	 * Execute the specified command.
	 *
	 * @param cmd the command.
	 * @param envp the environment in which to execute the command.
	 * @return the execution status.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public int doExec(String[] cmd, String[] envp) throws IOException{
		return doExec(cmd, envp, null, true);
	}

	/**
	 * Execute the specified command.
	 *
	 * @param cmd the command.
	 * @param envp the environment in which to execute the command.
	 * @param timeout the number of milliseconds after which to abort if the process execution has not completed.
	 * @param consumeResult if true, std.err and std.out will be consumed and the results can be retrieved by the client when
	 * process execution is completed. Otherwise the client is expected to consume std.err and std.out to keep the process from hanging.
	 * @return the execution status.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public int doExec(String[] cmd, String[] envp, Integer timeout, boolean consumeOutput) throws IOException{
		Timer t = null;
		try {
			process = Runtime.getRuntime().exec(cmd, envp);
			if (consumeOutput) {
				outReader = new StreamReader(process.getInputStream());
				outReader.setPriority(10);
				errReader = new StreamReader(process.getErrorStream());
				outReader.start();
				errReader.start();
			} else {
				this.stdOut = process.getInputStream();
				this.stdErr = process.getErrorStream();
			}
			t = new Timer();
			if (timeout != null) {
				t.schedule(task, timeout);
			}
			if (consumeOutput) {
				int status = process.waitFor();
				outReader.join();
				errReader.join();
				StringWriter outWriter = outReader.getResult();
				outResult = outWriter.toString();
				outWriter.close();
				StringWriter errWriter = errReader.getResult();
				errResult = errWriter.toString();
				errWriter.close();
				return (failed ? -1: status);
			} else {
				return 0;
			}
			
		} catch (InterruptedException e) {
			return -1;
		} finally {
			if (t != null) {
				t.cancel();
			}
		}
	}

	/**
	 * Execute the specified command.
	 *
	 * @param cmd the command.
	 * @return the process execution status.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public int doExec(String[] cmd) throws IOException{
		return doExec(cmd, null);
	}

	/**
	 * Gets the std.out result.
	 *
	 * @return the text written to std.out by the process executing the command.
	 */
	public String getOutResult(){
		return outResult;
	}

	/**
	 * Gets the stf.err result.
	 *
	 * @return the text written to std.err by the process executing the command.
	 */
	public String getErrResult(){
		return errResult;
	}

	/**
	 * The Class StreamReader.
	 */
	public static class StreamReader extends Thread {

		/** The is. */
		private InputStream is;

		/** The sw. */
		private StringWriter sw;

		/**
		 * Instantiates a new stream reader.
		 *
		 * @param is the input stream
		 */
		StreamReader(InputStream is) {
			this.is = is;
			sw = new StringWriter(30000);
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			try {
				int c;
				while ((c = is.read()) != -1){
					sw.write(c);
				}
			}
			catch (IOException e) { ; }
		}

		/**
		 * Gets the result.
		 *
		 * @return the StringWriter that holds the contents copied from the input stream.
		 */
		StringWriter getResult() {
			try {
				is.close();
			} catch (IOException e) {
				System.err.println("Unable to close input stream in StreamReader");
			}
			/** 
			 * 
			 * the StringWriter is closed in {@link #doExec(String[])} after storing its string result.
			 */
			return sw;
		}
	}

	/** The timeout task. KIlls the process after the specified timeout period. */
	private TimerTask task = new TimerTask() {

		@Override
		public void run() {
			failed = true;
			process.destroy();
		}

	};
	
	public Process getProcess() {
		return this.process;
	}
	
	public InputStream getStdOut() {
		return this.stdOut;
	}
	
	public InputStream getStdErr() {
		return this.stdErr;
	}
}
