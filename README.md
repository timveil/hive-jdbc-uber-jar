# Overview
Sometimes you want to connect your favorite database query or visualization tool to Hive. I've found that this can be quite cumbersome, typically requiring you to copy jars from the Hadoop cluster to some place locally that can be read by your tool. The goal of this simple maven project is to easily pull the required jars into a single place locally and create an "uber" or "standalone" jar that can be referenced by any JDBC compliant tool.

Please note this project only supports connecting to a remote hiveserver in "non-kerberos" mode.  Additional pom changes would be required to support "kerberos secure" mode and "embedded" mode.  See [Hiveserver2 Client Documentation](https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Clients#HiveServer2Clients-JDBC) for recommended jar additions.

:elephant: __Updated for HDP 2.5.0.0__ :elephant: 

You can download the latest binaries from the releases page:  https://github.com/timveil/hive-jdbc-uber-jar/releases

## DbVisualizer (as of version 9.2.15)
Below is an example configuration using [DbVisualizer](http://www.dbvis.com/):

1. Under "Tools" > "Driver Manager..." hit the "Create a new driver" button.

2. Fill in the information as seen below.  For the "Driver File Paths" you are pointing to the `hive-jdbc-uber-x.jar` created above.

![](https://github.com/timveil/hive-jdbc-uber-jar/blob/master/images/driver.png)

3. Next create a new connection.  In this case I'm pointing to Hive on my Hortonworks Sandbox.

![](https://github.com/timveil/hive-jdbc-uber-jar/blob/master/images/connection.png)

## IntelliJ Data Grip (as of version 2016.1)
Below is an example configuration using IntelliJ [Data Grip](https://www.jetbrains.com/datagrip/):

1. Under "File" > "Data Sources...", first create a new Driver.

![](https://github.com/timveil/hive-jdbc-uber-jar/blob/master/images/intellij-driver.png)

2. Then create a new Project Data Source using the new Driver.

![](https://github.com/timveil/hive-jdbc-uber-jar/blob/master/images/intellij-connection.png)

3. After creating the Project Data Source, test the connection.  You should see the following:

![](https://github.com/timveil/hive-jdbc-uber-jar/blob/master/images/intellij-connection-test.png)

## How to Build
To build locally, you must have Maven installed and properly configured.  After that it's as simple as running `mvn:package`.  A file called `hive-jdbc-uber-x.jar` will be created in your `target` directory.  The newly created jar will have the Hive JDBC driver as well as all required dependencies.
