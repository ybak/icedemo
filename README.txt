mvn install:install-file -DgroupId=com.zeroc -DartifactId=ice -Dversion=3.4.1 -Dpackaging=jar -Dfile=Ice.jar 

mvn install:install-file -DgroupId=com.zeroc -DartifactId=ice -Dversion=3.4.1 -Dpackaging=jar -DgeneratePom=true -Dclassifier=sources

glacier2router --Ice.Config=src/main/resources/config.glacier2