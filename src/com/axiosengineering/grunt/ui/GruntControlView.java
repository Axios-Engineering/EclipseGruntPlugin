package com.axiosengineering.grunt.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class GruntControlView extends ViewPart {

	protected static final String GRUNT_FILE = "Gruntfile.js";

	private static final String COLOR_PROJECT = "color_project";

	private TreeViewer viewer;
	
	private StyledCellLabelProvider gruntProjectLabelProvider = new StyledCellLabelProvider() {
		
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			StyledString styledString = new StyledString("");
			if (element instanceof IResource) {
				styledString = new StyledString(((IResource) element).getName());
			}
			cell.setText(styledString.toString());
			
			if (element instanceof IProject) {
				styledString.setStyle(0, styledString.length(), StyledString.createColorRegistryStyler(COLOR_PROJECT, null));
				cell.setStyleRanges(styledString.getStyleRanges());
			}
		}
	};
	private ITreeContentProvider gruntProjectContentProvider = new ITreeContentProvider() {

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
			
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
					return new Object[] {gruntFile};
				}
			}
			return Collections.emptyList().toArray(new Object[0]);
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof IFile) {
				IFile file = (IFile) element;
				return file.getProject();
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
		
	};

	public GruntControlView() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		initializeColorRegistry();
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
		viewer.setContentProvider(gruntProjectContentProvider);
		viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		viewer.expandAll();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());
	}

	@Override
	public void setFocus() {
		this.viewer.getControl().setFocus();
	}

}
