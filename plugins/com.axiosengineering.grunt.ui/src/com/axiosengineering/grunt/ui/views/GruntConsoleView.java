package com.axiosengineering.grunt.ui.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.axiosengineering.grunt.ui.Activator;

public class GruntConsoleView extends ViewPart {
	
	public static final String VIEW_ID = "com.axiosengineering.grunt.ui.consoleView";
	private Text textArea;

	public GruntConsoleView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayoutFactory.fillDefaults().applyTo(parent);
		textArea = new Text(parent, SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(textArea);
	}

	@Override
	public void setFocus() {
		this.textArea.setFocus();
	}
	
	public void setInputStream(final InputStream is) {
		new Thread() {
			public void run() {
				BufferedReader bir = new BufferedReader(new InputStreamReader(is));
				String s;
				try {
					while ((s = bir.readLine()) != null) {
						final String final_s = s;
						textArea.getDisplay().asyncExec(new Runnable() {

							@Override
							public void run() {
								textArea.append(final_s + "\n");
							}
							
						});
					}
				} catch (IOException e) {
					Activator.getDefault().getLog().log(new Status(
							Status.ERROR, Activator.PLUGIN_ID, "A problem occured while resding the Grunt consle input stream"));
				}
			};
		}.start();
	}
	
	public void setErrorStream(final InputStream is) {
		new Thread() {
			public void run() {
				BufferedReader bir = new BufferedReader(new InputStreamReader(is));
				String s;
				try {
					while ((s = bir.readLine()) != null) {
						final String final_s = s;
						textArea.getDisplay().asyncExec(new Runnable() {

							@Override
							public void run() {
								textArea.append(final_s + "\n");
							}
							
						});
					}
				} catch (IOException e) {
					Activator.getDefault().getLog().log(new Status(
							Status.ERROR, Activator.PLUGIN_ID, "A problem occured while resding the Grunt consle input stream"));
				}
			};
		}.start();
	}

	public void appendText(final String string) {
		this.textArea.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				textArea.append(string);
			}
			
		});
	}

	public void setViewTitle(String name) {
		this.setPartName(name);
	}

}
