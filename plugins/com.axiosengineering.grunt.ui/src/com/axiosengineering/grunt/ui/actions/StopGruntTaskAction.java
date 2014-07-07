package com.axiosengineering.grunt.ui.actions;

import java.util.Map;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.axiosengineering.grunt.ui.Activator;
import com.axiosengineering.grunt.ui.TaskActionListener;
import com.axiosengineering.grunt.ui.views.GruntConsoleView;

public class StopGruntTaskAction extends Action {

	private String task;

	private IFile gruntFile;

	private IWorkbenchPage page;

	private ListenerList listeners;

	public static Process p;

	public StopGruntTaskAction() {
		super(null, IAction.AS_PUSH_BUTTON);
		final ImageDescriptor runImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/stop.png");
		setImageDescriptor(runImageDescriptor);
		setToolTipText("Stop Grunt Task");
		this.listeners = new ListenerList();
	}

	@Override
	public void run() {
		System.err.println("STOP GRUNT TASK: task: " + task + " file: " + gruntFile.getProject().getName());
		if (p != null) {
			p.destroy();
			final int result;
			try {
				result = p.waitFor();
				Activator.getDefault().removeRunningTask(task);
			} catch (InterruptedException e) {
				Activator.getDefault().getLog().log(new Status(
						Status.ERROR, Activator.PLUGIN_ID, "Unable to get process status", e));
				return;
			}
			for (Object listener : listeners.getListeners()) {
				((TaskActionListener) listener).taskActionSelected();
			}
			if (page != null) {
				page.getWorkbenchWindow().getShell().getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						try {
							GruntConsoleView view = (GruntConsoleView) page.showView(
									GruntConsoleView.VIEW_ID, task, IWorkbenchPage.VIEW_ACTIVATE);
							view.appendText("Process exited with value " + result + "\n");
							view.setViewTitle("Grunt: " + task);
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				});
			}
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
	
	public void addListener(TaskActionListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeListener(TaskActionListener listener) {
		this.listeners.remove(listener);
	}
}
