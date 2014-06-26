package com.axiosengineering.grunt.ui;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.axiosengineering.grunt.ui.Exec.StreamReader;

public class StartGruntTaskAction extends Action {

	private String task;

	private IFile gruntFile;

	private Process process;

	public StartGruntTaskAction() {
		super(null, IAction.AS_PUSH_BUTTON);
		final ImageDescriptor runImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/start.png");
		setImageDescriptor(runImageDescriptor);
	}

	@Override
	public void run() {
		System.err.println("START GRUNT TASK: task: " + task + " project: " + gruntFile.getProject().getName());
		IPath basePath = gruntFile.getProject().getLocation();
		IPath gruntFilePath = new Path(gruntFile.getName());
		IPath path = basePath.append(gruntFilePath);
		System.err.println("Gruntfile exists: " + path.toFile().exists());
		System.err.println("Gruntfile path: " + path.toString());
		final String[] cmd = new String[] {"grunt", "--no-color", "--gruntfile", path.toString(), task};
		final Exec exec = new Exec();
		new Thread() {
			public void run() {
				try {
					exec.doExec(cmd, null, null, false);
					process = exec.getProcess();
					consumeStreams(exec);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();
	}

	protected void consumeStreams(Exec exec) {
		StreamReader outReader = new StreamReader(process.getInputStream());
		outReader.setPriority(10);
		StreamReader errReader = new StreamReader(process.getErrorStream());
		outReader.start();
		errReader.start();
		try {
			exec.getProcess().waitFor();
			outReader.join();
			errReader.join();
			StringWriter outWriter = outReader.getResult();
			String outResult = outWriter.toString();
			System.err.println("Process standard output: " + outResult);
			outWriter.close();
			StringWriter errWriter = errReader.getResult();
			String errResult = errWriter.toString();
			System.err.println("Process error output: " + errResult);
			errWriter.close();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void configureAction(Map<String, Object> config) {
		if (config == null) {
			this.task = null;
			this.gruntFile = null;
		} else {
			this.task = (String) config.get(Activator.KEY_TASK);
			this.gruntFile = (IFile) config.get(Activator.KEY_FILE);
		}
	}

	public Process getProcess() {
		return this.process;
	}
}
