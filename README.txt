4 steps :
	0/ MANUAL
		- create a folder $PROJECT_HOME/build
		- get the latest sources from GIT (https://github.com/bogeblad/infoglue/zipball/master ; the tarball doesn't seem to contain the test folder ...)
		- unzip archive into build folder and rename the extracted archive (bogeblad-infoglue-****) : infoglue
	=> you have now infoglue sources available here /build/infoglue
	1/ ANT (details below)
		- It will deploy the maven2 architecture (project infoglue-root and submodule)
		- Then it will copy the source from the infoglue project into the right folder of the maven2 architecture
	=> You have now a working maven2 project : infoglue-root 	
	2/ MAVEN (details below)
		You'll need maven3 (which is faster and compatible with maven2).
		If you just start with maven, it may takes some time at first as it have to grab all the needed lib
		- go to infoglue-root/propertyset folder with a command line and install it
			mvn install
		- go to infoglue-root/milton-api folder with a command line and install it
			mvn install
		- go to infoglue-root/velocity folder with a command line and install it
			mvn install
		- go to infoglue-root folder with a command line and install it
			mvn install
	3/ That's it =)
		- you can go to infoglue-root/infoglue-webapp-cms and configure the properties : /src/main/filters/filter.properties
		- launch tomcat when ready (right now, it is configured to use mysql ; jdbc driver is attached to tomcat) 
			mvn tomcat:run
		- go with you browser to http://localhost:8080/infoglue-webapps-cms
	4 optionnal / ECLIPSE (details below)
		- you can import into eclipse infoglue-root as a maven project
		- it will be shown as multiple project, a project for a module 


------------------------------------------------
Why make a maven port of the project :
	- to have a standart way of describing, managing, deploying the project
	- have access to the dependencies manager, it will be easier to upgrade components
	- deploy easily with maven plugin (cargo, ...)
	- make test unit easier to run
	- use a continuous integration environment (continuum, nexus)
	- generate reports (checkstyle, covertura, pmd, sonar), javadocs, site


------------------------------------------------
Details on the ant part of this install :

1 thing is not good with Infoglue source structure :
- some java classes required to compiled are in test folder, they must take place in main src folder


------------------------------------------------
Details on the maven part of this install :

One particular jar, propertyset-1.4 is not available as a build, it is only available as a tag in openymphony svn.
A maven project has been created for this : propertyset

2 jars are patched for Infoglue, details of these patches are available in their respective README : 
- velocity-1.7IGPatched
- milton-api-1.5.4IGPatched

All the others are maven dependencies fetch from repositories listed into the parent pom.xml.


------------------------------------------------
To see new version of artefact, use :
mvn versions:display-plugin-updates
mvn versions:display-dependency-updates