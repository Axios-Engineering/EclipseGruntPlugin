package com.axiosengineering.grunt.ui.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.axiosengineering.grunt.ui.Activator;
import com.axiosengineering.grunt.ui.GruntProjectContentProvider;
import com.axiosengineering.grunt.ui.GruntProjectContentProvider.AliasTask;
import com.axiosengineering.grunt.ui.GruntProjectContentProvider.TaskContainer;
import com.axiosengineering.grunt.ui.actions.RestartGruntTaskAction;
import com.axiosengineering.grunt.ui.actions.StartGruntTaskAction;
import com.axiosengineering.grunt.ui.actions.StopGruntTaskAction;

public class GruntControlView extends ViewPart {



	private static final String COLOR_PROJECT = "color_project";

	private TreeViewer viewer;

	private StyledCellLabelProvider gruntProjectLabelProvider = new StyledCellLabelProvider() {

		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			StyledString styledString = new StyledString("");
			if (element instanceof IProject) {
				styledString = new StyledString(((IProject) element).getName());
				styledString.setStyle(0, styledString.length(), StyledString.createColorRegistryStyler(COLOR_PROJECT, null));
				cell.setStyleRanges(styledString.getStyleRanges());
			} else if (element instanceof String) {
				styledString = new StyledString((String) element);
			} else if (element instanceof AliasTask) {
				AliasTask aliasTask = (AliasTask) element;
				styledString = new StyledString(aliasTask.task + ": " + aliasTask.definition);
				StyleRange style = new StyleRange();
				style.start = 0;
				style.length = aliasTask.task.length();
				//style.font = null; //if font is set, setting fontStyle will have no effect
				//style.fontStyle = SWT.BOLD;
				//the above should work, but it doesn't
				//so we set the font with the desired style
				FontData[] fd = cell.getFont().getFontData();
				fd[0].setStyle(SWT.BOLD);
				style.font = new Font(Display.getCurrent(), fd);
				cell.setStyleRanges(new StyleRange[]{style});
			} else if (element instanceof TaskContainer) {
				TaskContainer container = (TaskContainer) element;
				if (container.alias) {
					styledString = new StyledString("Alias Tasks");
				} else {
					styledString = new StyledString("Tasks");
				}
			}
			cell.setText(styledString.toString());
		}
	};
	
	private ISelectionChangedListener viewerSelectionListener = new ISelectionChangedListener() {

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			StructuredSelection ss = (StructuredSelection) event.getSelection();
			Object sel = ss.getFirstElement();
			Map<String, Object> config = new HashMap<String, Object>();
			if (sel instanceof String || sel instanceof AliasTask) {
				String task = "";
				if (sel instanceof String) {
					task = (String) sel;
				} else {
					task = ((AliasTask) sel).task;
				}
				IFile gruntFile = contentProvider.getFileForTask((String) task);
				config.put(Activator.KEY_TASK, task);
				config.put(Activator.KEY_FILE, gruntFile);
				config.put(Activator.KEY_PAGE, getSite().getWorkbenchWindow().getActivePage());
				startGruntTaskAction.setEnabled(true);
				startGruntTaskAction.configureAction(config);
				stopGruntTaskAction.setEnabled(true);
				stopGruntTaskAction.configureAction(config);
				restartGruntTaskAction.setEnabled(true);
				restartGruntTaskAction.configureAction(config);
			} 
			else {
				startGruntTaskAction.setEnabled(false);
				startGruntTaskAction.configureAction(null);
				stopGruntTaskAction.setEnabled(false);
				stopGruntTaskAction.configureAction(null);
				restartGruntTaskAction.setEnabled(false);
				restartGruntTaskAction.configureAction(null);
			}
		}
		
	};

	private StartGruntTaskAction startGruntTaskAction;

	private RestartGruntTaskAction restartGruntTaskAction;

	private StopGruntTaskAction stopGruntTaskAction;

	private GruntProjectContentProvider contentProvider;

	public GruntControlView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		initializeColorRegistry();
		createActions();
		createToolBars();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {

			@Override
			public void resourceChanged(IResourceChangeEvent e) {
				if (e.getDelta() != null && e.getDelta().getResource() != null) {
					getSite().getShell().getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
							viewer.expandAll();
						}
						
					});
				}
			}
			
		});
	}

	private void createToolBars() {
		final IActionBars bars = getViewSite().getActionBars();

		final IToolBarManager toolBarManager = bars.getToolBarManager();
		if (this.startGruntTaskAction != null) {
			toolBarManager.add(startGruntTaskAction);
		}
		if (this.stopGruntTaskAction != null) {
			toolBarManager.add(this.stopGruntTaskAction);
		}
		if (this.restartGruntTaskAction != null) {
			toolBarManager.add(this.restartGruntTaskAction);
		}
		toolBarManager.add(new Separator());
		toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void createActions() {
		this.startGruntTaskAction = new StartGruntTaskAction();
		this.startGruntTaskAction.setEnabled(false);
		this.stopGruntTaskAction = new StopGruntTaskAction();
		this.stopGruntTaskAction.setEnabled(false);
		this.restartGruntTaskAction = new RestartGruntTaskAction();
		this.restartGruntTaskAction.setEnabled(false);
	}

	private void initializeColorRegistry() {
		JFaceResources.getColorRegistry().put(COLOR_PROJECT, new RGB(9, 22, 214));
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayoutFactory.swtDefaults().applyTo(parent);
		Label label = new Label(parent, SWT.NONE);
		label.setText("Projects");
		viewer = new TreeViewer(parent, SWT.NONE);
		viewer.setLabelProvider(gruntProjectLabelProvider);
		this.contentProvider = new GruntProjectContentProvider();
		viewer.setContentProvider(contentProvider);
		viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		viewer.expandAll();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());
		viewer.addSelectionChangedListener(viewerSelectionListener);
	}

	@Override
	public void setFocus() {
		this.viewer.getControl().setFocus();
	}

}
