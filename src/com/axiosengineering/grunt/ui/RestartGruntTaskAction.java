package com.axiosengineering.grunt.ui;

import java.io.IOException;
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
		String[] cmd = new String[] {"grunt", "--no-color", "--gruntfile", path.toString(), task};
		Exec exec = new Exec();
		try {
			exec.doExec(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("STD ERROR: " + exec.getErrResult());
		System.err.println("STD OUT: " + exec.getOutResult());
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
