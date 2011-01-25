4 steps :
	0/ MANUAL
		- create a folder $PROJECT_HOME/build
		- get the latest sources from GIT (https://github.com/astik/infoglue/zipball/master ; the tarball doesn't seem to contain the test folder ...)
		- unzip archive into build folder and rename the extracted archive (astik-infoglue-****) : infoglue
	=> you have now infoglue sources available here /build/infoglue
	1/ ANT (details below)
		- It will deploy the maven2 architecture (project infoglue-root and submodule)
		- Then it will copy the source from the cvs into the right folder of the maven2 architecture
	=> You have now a working maven2 project : infoglue-root 	
	2/ MAVEN (details below)
		- go to infoglue-root folder with a command line
		- install these 2 jar into your repo:
			mvn install:install-file -DgroupId=netscape.javascript -DartifactId=netscape-javascript -Dversion=unknown -Dpackaging=jar -Dfile=lib/netscape-javascript.jar
			mvn install:install-file -DgroupId=com.mullassery -DartifactId=imaging -Dversion=unknown -Dpackaging=jar -Dfile=lib/pmiw.jar
		- compile Infoglue, it may takes some time, as it have to grab all the needed lib
			mvn install
	3/ ECLIPSE (details below)
		- import into eclipse infoglue-root as a maven project
		- it will be shown as multiple project, a project for a module 
		- fix error if needed (see details)


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

I didn't prepare the test phase, so the ant file doesn't move the file that are not needed to compile. It will come later, if needed.

2 things are not good with CVS :
- some java classes required to compiled are in test folder, they must take place in main src folder
- source folder org/infoglue/cms/plugins/codeeditor contains class files, which is not great   
- some compiled class are in the source folder with their respective java file

------------------------------------------------
Details on the maven part of this install :

2 jars can't be found on the Internet :
- netscape-javascript.jar	used in infoglue-opentree and infoglue-codeeditor
- pmiw.jar used in infoglue-cms-imageediting
They have to be manually install into your maven repository :
mvn install:install-file -DgroupId=netscape.javascript -DartifactId=netscape-javascript -Dversion=unknown -Dpackaging=jar -Dfile=lib/netscape-javascript.jar
mvn install:install-file -DgroupId=com.mullassery -DartifactId=imaging -Dversion=unknown -Dpackaging=jar -Dfile=lib/pmiw.jar

One other particular jar, propertyset-1.4 is not available as a build, it is only available as a tag in openymphony svn.
A maven project has been created for this : propertyset

All the others are maven dependencies fetch from central or atlasian repos.

Velocity is bundle into Infoglue as velocity-1.6.3IGPatched ; in the maven version its the regular 1.6.3 as I don't have the source
Milton is bundle into Infoglue as milton-api-1.5.4IGPatched ; in the maven version its the regular 1.5.4 as I don't have the source

------------------------------------------------
Details on the eclipse part of this install :

Many error are, in fact, dependencies which consist of unused import, do an "organise import" in eclipse, it should work
Other kind of error on dependencies are logger which are declared with a wrong class.

You can find more info about the DateTickUnit note :
http://sourceforge.net/tracker/index.php?func=detail&aid=1062698&group_id=15494&atid=365494
http://sourceforge.net/tracker/index.php?func=detail&aid=1062703&group_id=15494&atid=365494

If it's not the default configuration, set eclipse execution environment so "J2SE-1.5", so that, it would run on any 1.5 environment.
	-> it would make some error during compilation :
		dependencies on sun.misc.BASE64Decoder & BASE64Encoder => replace it with Base64 from commons.codec
		DateTickUnit constructor come from an unoffical distrib of jfreechart, remove the third param to make it work
		@override is use for interface which is not support with java 1.5 