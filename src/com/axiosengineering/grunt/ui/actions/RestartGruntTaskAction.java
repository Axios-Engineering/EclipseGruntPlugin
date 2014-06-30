package com.axiosengineering.grunt.ui.actions;

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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.axiosengineering.grunt.ui.Activator;
import com.axiosengineering.grunt.ui.Exec;
import com.axiosengineering.grunt.ui.views.GruntConsoleView;

public class RestartGruntTaskAction extends Action {

	private String task;

	private IFile gruntFile;

	private IWorkbenchPage page;
	
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
		if (page != null) {
			page.getWorkbenchWindow().getShell().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					try {
						GruntConsoleView view = (GruntConsoleView) page.showView(
								GruntConsoleView.VIEW_ID, task, IWorkbenchPage.VIEW_ACTIVATE);
						view.setInputStream(exec.getProcess().getInputStream());
						view.setErrorStream(exec.getProcess().getErrorStream());
						view.setViewTitle("Grunt: " + task);
					} catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			});
		}
	}

	public void configureAction(Map<String, Object> config) {
		if (config == null) {
			this.task = null;
			this.gruntFile = null;
			this.page = null;
		} else {
			this.task = (String) config.get(Activator.KEY_TASK);
			this.gruntFile = (IFile) config.get(Activator.KEY_FILE);
			this.page = (IWorkbenchPage) config.get(Activator.KEY_PAGE);
		}
	}

}
