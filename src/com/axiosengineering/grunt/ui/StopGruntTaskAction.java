package com.axiosengineering.grunt.ui;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class StopGruntTaskAction extends Action {

	private String task;
	
	private IFile gruntFile;

	public static Process p;

	public StopGruntTaskAction() {
		super(null, IAction.AS_PUSH_BUTTON);
		final ImageDescriptor runImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/stop.png");
		setImageDescriptor(runImageDescriptor);
	}
	
	@Override
	public void run() {
		System.err.println("STOP GRUNT TASK: task: " + task + " file: " + gruntFile.getProject().getName());
		if (p != null) {
			p.destroy();
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
}
