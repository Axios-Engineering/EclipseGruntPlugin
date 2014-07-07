EclipseGruntPlugin
==================

This project provides an Eclipse feature that implements a UI for controlling grunt tasks. Grunt is a task runner for
Javascript projects. See http://gruntjs.com/ for more information.

INSTALLATION (Eclipse Experts):

An eclipse update site archive can be found at releng/com.axiosengineering.grunt.ui.update.site.zip. Install the single
feature provided in this update site. (NB: Ensure that "Group items by cagtegory" is not checked)

INSTALLATION (Detailed Instructions):

Download the file releng/com.axiosengineering.grunt.ui.update.site.zip. From the Eclipse Help menu, select
"Install New Software...", click "Add", click "Archive", then navigate to the update
site file. Make sure that "Group items by category" is not checked. Select "Grunt UI Feature", and click through the
remainder of the wizard. To remove the feature, select "About Eclipse" (from the appropriate menu for your platform),
click "Installation Details", select the "Grunt UI" feature, and click "Uninstall".

USAGE:

Switch to the Javascript perspective. The Grunt Control view will appear below the Project Explorer view. (If you don't
see the view, reset the perspective from the Window menu). Any project in your workspace that contains a file in its
top level directory named Gruntfile.js will automatically be added to the Grunt Control View. Select the desired task or
task alias, and click the Start action in the toolbar. Grunt output will be dispalyed in the Grunt Console view, which
will be located by default to the right of the Declaration view in the lower right view stack. Select a different grunt
task and start it if desired, or select a task already started and click the Stop or Restart action.

Compatibility:

This feature has been tested with Eclipse Kepler and Luna for JEE developers distribution.

Contact:

Send bug reports, feature suggestions, questions, etc. to mark.leone@axiosengineering.com

License:

You may use this software with no restrictions. Pull requests are welcome.
