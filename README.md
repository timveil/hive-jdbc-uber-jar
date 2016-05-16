# Overview
Sometimes you want to connect your favorite database visualization tool to Hive.  I've found that this can be somewhat cumbersome, typically requiring you to copy jars from the distro to some place locally that can be read by your tool.  The goal of this simple maven project was to easily pull the required jars into a single place locally and create an "uber" or "standalone" jar that can be referenced by any JDBC hungry tool.

Please note this project only supports connecting to a remote hiveserver in "non-kerberos" mode.  Additional pom changes would be required to support "kerberos secure" mode and "embedded" mode.  See [Hiveserver2 Client Documentation](https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Clients#HiveServer2Clients-JDBC) for recommended jar additions.

You can download the latest version of the jar [here] (https://github.com/timveil/hive-jdbc-uber-jar/releases)

## DbVisualizer
Below is an example configuration using [DbVisualizer](http://www.dbvis.com/):

1. Under "Tools" > "Driver Manager..." hit the "Create a new driver" button.

2. Fill in the information as seen below.  For the "Driver File Paths" you are pointing to the `hive-jdbc-uber-1.0-x.jar` created above.

![](https://github.com/timveil/hive-jdbc-uber-jar/wiki/images/driver.png)

3. Next create a new connection.  In this case I'm pointing to Hive on my Hortonworks Sandbox.

![](https://github.com/timveil/hive-jdbc-uber-jar/wiki/images/connection.png)

## IntelliJ IDEA
Below is an example configuration using IntelliJ [IDEA's](http://www.jetbrains.com/idea/) database capabilities:

1. Under "Data Sources and Drivers", first create a new Driver.

![](https://github.com/timveil/hive-jdbc-uber-jar/wiki/images/intellij-driver.png)

2. Then create a new Data Source using the new Driver

![](https://github.com/timveil/hive-jdbc-uber-jar/wiki/images/intellij-connection.png)

## How to Build
To build locally, you must have Maven installed and properly configured.  After that it's as simple as running `mvn:package`.  A file called `hive-jdbc-uber-1.0-x.jar` will be created in your `target` directory.  After that, point your favorite tool to this jar.  It will have the necessary Hive JDBC drivers as well as required dependencies.
