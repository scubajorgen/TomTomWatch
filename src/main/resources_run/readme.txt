TOMTOMWATCH

GETTING STARTED RIGHT AWAY
1. Go to http://blog.studioblueplanet.net/?page_id=566, download the latest compiled version
2. makeworkingdirs.bat
3. java -jar TomTomWatch.jar


INTRODUCTION
TomTomWatch is a utility for downloading GPS logged tracks with the TomTom Adventurer 
sports watch (the RUnner, SPark and Multisports should also be supported, though I did not test).
See http://blog.studioblueplanet.net/?page_id=566 for more information


COMPILING AND RUNNING
1. Download the source from Github
2. Use Maven to compile.
3. Go to the target directory
4. Run makeworkingdirs.bat or adapt tomtomwatch.properties
5. Run the jar java -jar TomTomWatch-<x.y>.jar. <x.y> is the version, like 1.0


TomTomWatch needs a number of working directories. These are defined in tomtomwatch.properties
/gpx            To store the gpx files
/ttbin          To store the raw ttbin files. You can use the same dir as TomTom MySports, c:\users\[user]\TomTOm Sports
/routes         To fetch route gpx file from
/files          To store downloaded files - debugging only
/simultation    To store a simulation set and run a simulation - debugging only

The tomtomwatch.properties that ends up in the build software comes form /TomTomWatch/src/main/resources_run.
However, when building and running the software from Netbeans, the software expects a version in the root /TomTomWatch.
So here a version is placed that can be used during development.
