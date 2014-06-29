package com.axiosengineering.grunt.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class GruntProjectContentProvider implements ITreeContentProvider {

	protected static final String GRUNT_FILE = "Gruntfile.js";

	private Map<IFile, List<String>> fileToTasksMap = new HashMap<IFile, List<String>>();

	private Map<String, IFile> taskToFileMap = new HashMap<String, IFile>();
	
	private Map<String, String> aliasTaskDefinitions = new HashMap<String, String>();

	private List<String> aliasTasks = new ArrayList<String>();

	public class TaskContainer {

		public IFile file;
		public List<String> tasks;
		public boolean alias;

		public TaskContainer(IFile gruntFile, List<String> tasks, boolean alias) {
			this.file = gruntFile;
			this.tasks = tasks;
			this.alias = alias;
		}
	}
	
	public class AliasTask {
		public String task;
		public String definition;
		
		public AliasTask(String task, String definition) {
			this.task = task;
			this.definition = definition;
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		List<IProject> gruntProjects = new ArrayList<IProject>();
		if (inputElement instanceof IWorkspaceRoot) {
			IWorkspaceRoot root = (IWorkspaceRoot) inputElement;
			for (IProject project : root.getProjects()) {
				if (project.getFile(GRUNT_FILE).exists()) {
					gruntProjects.add(project);
				}
			}
		}
		return gruntProjects.toArray(new IProject[0]);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IProject) {
			IProject project = (IProject) parentElement;
			IFile gruntFile = project.getFile(GRUNT_FILE);
			if (gruntFile.exists()) {
				return new Object[] {
						new TaskContainer(gruntFile, getTasks(gruntFile), false),
						new TaskContainer(gruntFile, aliasTasks, true)
				};
			}
		} else if (parentElement instanceof TaskContainer) {
			TaskContainer container = (TaskContainer) parentElement;
			List<String> tasks = container.tasks;
			mapFileToTasks(tasks, container.file);
			mapTasksToFile(tasks, container.file);
			if (container.alias) {
				List<AliasTask> retVal = new ArrayList<AliasTask>();
				for (String task : aliasTasks) {
					String definition = aliasTaskDefinitions.get(task);
					retVal.add(new AliasTask(task, definition));
				}
				return retVal.toArray(new AliasTask[0]);
			} else {
				return tasks.toArray(new String[0]);
			}
		}
		return Collections.emptyList().toArray(new Object[0]);
	}

	private void mapTasksToFile(List<String>  tasks, IFile file) {
		//first remove existing mappings for this file
//		List<String> keysToRemove = new ArrayList<String>();
//		for (Entry<String, IFile> entry : taskToFileMap.entrySet()) {
//			if (entry.getValue().equals(file)) {
//				keysToRemove.add(entry.getKey());
//			}
//		}
//		for (String key : keysToRemove) {
//			this.taskToFileMap.remove(key);
//		}
		for (String task : tasks) {
			this.taskToFileMap.put(task, file);
		}
	}

	private void mapFileToTasks(List<String>  tasks, IFile file) {
		this.fileToTasksMap.put(file, tasks);
	}

	private List<String> getTasks(IFile gruntFile) {
		List<String> tasks = new ArrayList<String>();
		aliasTasks = new ArrayList<String>();
		if (!gruntFile.exists()) {
			Activator.getDefault().getLog().log(new Status(
					Status.ERROR, Activator.PLUGIN_ID,
					"Grunt file " + gruntFile.getName() + " in project " + gruntFile.getProject().getName() + " does not exist"));
			return tasks;
		}
		BufferedReader br = null;
		try {
			InputStream is = gruntFile.getContents();
			br = new BufferedReader(new InputStreamReader(is));
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(new Status(
					Status.ERROR, Activator.PLUGIN_ID,
					"Cannot open Grunt file " + gruntFile.getName() + " in project " + gruntFile.getProject().getName(), e));
			return tasks;
		}

		try {
			String s;
			Pattern p1 = Pattern.compile("grunt\\.loadNpmTasks\\('(.*)'\\)");
			Pattern p2 = Pattern.compile("grunt\\.registerTask\\('([^']*)',\\s*\\[(.*)\\]");
			while ((s = br.readLine()) != null) {
				Matcher m1 = p1.matcher(s);
				Matcher m2 = p2.matcher(s);
				if (m1.find() && m1.groupCount() > 0) {
					String task = m1.group(1).replaceAll("grunt-(contrib-)*", "");
					tasks.add(task);
				} else if (m2.find() && m2.groupCount() > 0) {
					StringBuilder alias = new StringBuilder(m2.group(1));
					this.aliasTasks .add(alias.toString());
					if (m2.groupCount() > 1) {
						aliasTaskDefinitions.put(alias.toString(), m2.group(2).replaceAll("'", "").replaceAll(",", ", "));
					}
				}
			}
			return tasks;
		} catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(
					Status.ERROR, Activator.PLUGIN_ID,
					"Error while reading Grunt file " + gruntFile.getName() + " in project " + gruntFile.getProject().getName(), e));
			return tasks;
		}
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IFile) {
			IFile file = (IFile) element;
			return file.getProject();
		} else if (element instanceof String) {
			String task = (String) element;
			return this.taskToFileMap.get(task);
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public IFile getFileForTask(String task) {
		return this.taskToFileMap.get(task);
	}

}
