package com.axiosengineering.grunt.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

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
					StopGruntTaskAction.p = process;
					RestartGruntTaskAction.p = process;
					consumeStreams(exec);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();
	}

	protected void consumeStreams(final Exec exec) {
		final BufferedReader bir = new BufferedReader(new InputStreamReader(exec.getProcess().getInputStream()));
		new Thread() {

			public void run() {
				try {
					String s;
					while ((s = bir.readLine()) != null) {
						System.out.println(s);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						bir.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					int result = 0;
					try {
						result = exec.getProcess().waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Process exited with value " + result);
				}
			};
		}.start();
		
		final BufferedReader ber = new BufferedReader(new InputStreamReader(exec.getProcess().getErrorStream()));
		new Thread() {
			public void run() {
				try {
					String s;
					while ((s = ber.readLine()) != null) {
						System.err.println(s);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						ber.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
		}.start();
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
