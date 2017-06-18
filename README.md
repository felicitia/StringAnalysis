The project is set up to run with Eclipse. The following steps must be taken to set up the workspace:

1. Preferably initialize a clean workspace.
2. Import the NewStringAnalysis project from the StringAnalysis root folder.
3. Import the graphs project from the StringAnalysis/dependencies folder.
4. Make sure you have installed the m2eclipse plugin via:

http://download.eclipse.org/technology/m2e/releases

If that does not work, use:

http://download.eclipse.org/technology/m2e/releases/1.0

5. Right-click either the graphs or NewStringAnalysis project, and go to Maven -> Download sources, and then Maven -> Update Dependencies

After this step, it is recommended to restart Eclipse so the Maven dependencies will be loaded. At this point, the project is correctly set up on Eclipse and you may move on to setting up the RunningScripts.
