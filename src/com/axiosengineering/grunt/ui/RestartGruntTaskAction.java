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

public class RestartGruntTaskAction extends Action {

	private String task;

	private IFile gruntFile;
	
	public static Process p;
	
	public RestartGruntTaskAction() {
		super(null, IAction.AS_PUSH_BUTTON);
		final ImageDescriptor runImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/restart.png");
		setImageDescriptor(runImageDescriptor);
	}

	@Override
	public void run() {
		System.err.println("RESTART GRUNT TASK: task: " + task + " file: " + gruntFile.getProject().getName());
		IPath basePath = gruntFile.getProject().getLocation();
		IPath gruntFilePath = new Path(gruntFile.getName());
		IPath path = basePath.append(gruntFilePath);
		System.err.println("Gruntfile exists: " + path.toFile().exists());
		System.err.println("Gruntfile path: " + path.toString());
		if (p != null) {
			p.destroy();
		}
		final String[] cmd = new String[] {"grunt", "--no-color", "--gruntfile", path.toString(), task};
		final Exec exec = new Exec();
		new Thread() {
			public void run() {
				try {
					exec.doExec(cmd, null, null, false);
					Process process = exec.getProcess();
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

}
