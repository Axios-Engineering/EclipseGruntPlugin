package com.axiosengineering.grunt.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.axiosengineering.grunt.ui.actions.StopGruntTaskAction;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.axiosengineering.grunt.ui"; //$NON-NLS-1$

	public static final String KEY_FILE = "file";
	
	public static final String KEY_TASK = "task";

	public static final String KEY_PAGE = "page";

	// The shared instance
	private static Activator plugin;
	
	private List<String> runningTasks = new ArrayList<String>();
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {

			@Override
			public boolean preShutdown(IWorkbench workbench, boolean forced) {
				if (StopGruntTaskAction.p != null) 
					StopGruntTaskAction.p.destroy();
				return true;
			}

			@Override
			public void postShutdown(IWorkbench workbench) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public void addRunningTask(String task) {
		this.runningTasks.add(task);
	}
	
	public void removeRunningTask(String task) {
		this.runningTasks.remove(task);
	}
	
	public boolean isTaskRunning(String task) {
		return this.runningTasks.contains(task);
	}

}
