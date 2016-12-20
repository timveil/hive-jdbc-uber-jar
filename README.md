# Overview
Sometimes you want to connect your favorite database query or visualization tool to Hive. I've found that this can be quite cumbersome, typically requiring you to copy jars from the Hadoop cluster to some place locally that can be read by your tool. Recent versions of Hive include a "standalone" jar, but even this does not include all required dependencies.  The goal of this simple maven project is to easily pull the required jars into a single place locally and create an "uber" or "standalone" jar that can be referenced by any JDBC compliant tool.

Please note this jar works with both kerberized and non-kerberized clusters.  Configuring tools to work against kerberized clusters is typically more involved and requires an understanding of Kerberos.  A full explanation of configuring Kerberos is outside the scope of this project.

You can download the latest binaries from the releases page:  https://github.com/timveil/hive-jdbc-uber-jar/releases

* Added support kerberized clusters - 12/19/16 - __LATEST__
* Updated for HDP 2.5.3.0 - 12/01/16
* Updated for HDP 2.5.0.0 - 09/12/16

## Note about Kerberos and the workaround
When connecting to a kerberized cluster, ultimately the class `org.apache.hadoop.util.VersionInfo` is loaded.  This class attempts to load a file called `*-version-info.properties` in an effort to determine the current Hadoop version.  To do this, the following snippet of code is called:

```java
  protected VersionInfo(String component) {
    info = new Properties();
    String versionInfoFile = component + "-version-info.properties";
    InputStream is = null;
    try {
      is = Thread.currentThread().getContextClassLoader().getResourceAsStream(versionInfoFile);
      if (is == null) {
        throw new IOException("Resource not found");
      }
      info.load(is);
    } catch (IOException ex) {
      LogFactory.getLog(getClass()).warn("Could not read '" +
          versionInfoFile + "', " + ex.toString(), ex);
    } finally {
      IOUtils.closeStream(is);
    }
  }
```

When using DataGrip, this code executes successfully, but with DbVisualizer and other tools like SQuirreLSQL, the properties file is not found and errors are generated downstream.  For example, the following error is often encountered if the properties file fails to load.

```
java.lang.RuntimeException: Illegal Hadoop Version: Unknown (expected A.B.* format)
   at org.apache.hadoop.hive.shims.ShimLoader.getMajorVersion(ShimLoader.java:168)
   at org.apache.hadoop.hive.shims.ShimLoader.loadShims(ShimLoader.java:143)
   at org.apache.hadoop.hive.shims.ShimLoader.getHadoopThriftAuthBridge(ShimLoader.java:129)
   at org.apache.hive.service.auth.KerberosSaslHelper.getKerberosTransport(KerberosSaslHelper.java:54)
   at org.apache.hive.jdbc.HiveConnection.createBinaryTransport(HiveConnection.java:414)
   at org.apache.hive.jdbc.HiveConnection.openTransport(HiveConnection.java:191)
   at org.apache.hive.jdbc.HiveConnection.<init>(HiveConnection.java:155)
   at org.apache.hive.jdbc.HiveDriver.connect(HiveDriver.java:105)
```

The trouble seems to be caused by the way `org.apache.hadoop.util.VersionInfo` attempts to load the properties file using the current thread's classloader.  I suspect the difference in behavior between tools boils down to how each chooses to load the driver jars.  In any event, I have overwritten `org.apache.hadoop.util.VersionInfo` in this project to use a less problematic approach for loading the properties file.  This one line code change works in all tested tools.

```java
// Original code uses Thread.currentThread().getContextClassLoader() which does not contain the properties file in DbVisualizer or SQuirreLSQL
is = Thread.currentThread().getContextClassLoader().getResourceAsStream(versionInfoFile);

// My updated code uses the classloader of the calling class and has more predictable results.  The properties file is found in all tools.
is = this.getClass().getClassLoader().getResourceAsStream(versionInfoFile);
```

This updated code is inserted into the final jar and replaces the copy of `org.apache.hadoop.util.VersionInfo` found in `hadoop-common.jar`

# Non-kerberized Setup

## DbVisualizer (as of version 9.5.5)
Below is an example configuration using [DbVisualizer](http://www.dbvis.com/):

1. Open the Diver Manager dialog ("Tools" > "Driver Manager...") and hit the "Create a new driver" icon.

2. Fill in the information as seen below.  For the "Driver File Paths" you are pointing to `hive-jdbc-uber-x.jar`.

    ```
    jdbc:hive2://<server>:<port10000>/<database>
    ```

    ![](images/driver.png)

3. Create a new connection ("Database" > "Create Database Connection") and fill out the details based on your cluster as seen below.

    ![](images/connection.png)

4. Hit the "Connect" button to test the connection.  You should see something like the following in the "Connection Message" text area if the connection is successful.

    ```fundamental
    Apache Hive
    1.2.1000.2.5.3.0-37
    null
    null
    ```

## IntelliJ Data Grip (as of version 2016.3)
Below is an example configuration using IntelliJ [Data Grip](https://www.jetbrains.com/datagrip/):

1. Under "File" > "Data Sources...", create a new Driver.

    ```
    jdbc:hive2://{host}:{port}/{database}[;<;,{:identifier}={:param}>]
    ```

    ![](images/intellij-driver.png)

2. Then create a new Project Data Source using the new Driver.

    ![](images/intellij-connection.png)

3. After creating the Project Data Source, test the connection.  You should see the following:

    ![](images/intellij-connection-test.png)

# Kerberized Setup
Connecting a JDBC tool to a kerberized cluster is a bit more complicated than connecting to a non-kerberized cluster.  Before getting started, ensure the following:

* The `krb5.conf` file on your workstation matches the one on your cluster
* You have a valid kerberos principal that can access the appropriate services your custer
* You can succesfully `kinit` from your workstation against the realm specified in your `krb5.conf` file

    ```bash
    # for example, from my Mac, i execute the follwing command to authenticate to the kdc
    kinit -t /[path to my keytab]/tveil.keytab
    ```

## DbVisualizer (as of version 9.5.5)
Below is an example configuration using [DbVisualizer](http://www.dbvis.com/) against a kerberized cluster.

2. `kinit` with an appropriate principal and launch DbVisualizer

4. Open DbVisualizer preferences ("DbVisualizer" > "Preferences") and add the following properties.  DbVisualizer will need to be restarted after applying these changes.

    ```dosini
    -Dsun.security.krb5.debug=true
    -Djavax.security.auth.useSubjectCredsOnly=false
    ```

    ![](images/tool-properties.png)

3. Open the Diver Manager dialog ("Tools" > "Driver Manager...") and hit the "Create a new driver" icon.

4. Fill in the information as seen below.  For the "Driver File Paths" you are pointing to `hive-jdbc-uber-x.jar`.

    ```
    jdbc:hive2://<server>:<port10000>/<database>
    ```

    ![](images/driver.png)

5. Create a new connection ("Database" > "Create Database Connection") and fill out the details based on your cluster as seen below.  Please note that you must append the "principal" to the "database" parameter for kerberized connections.

    ![](images/secure-connection.png)

6. Hit the "Connect" button to test the connection.  You should see something like the following in the "Connection Message" text area if the connection is successful.

    ```fundamental
    Apache Hive
    1.2.1000.2.5.3.0-37
    null
    null
    ```

## IntelliJ Data Grip (as of version 2016.3)
Below is an example configuration using IntelliJ [Data Grip](https://www.jetbrains.com/datagrip/):

1. `kinit` with an appropriate principal and launch DataGrip

2. Under "File" > "Data Sources...", create a new Driver.

    ```
    jdbc:hive2://{host}:{port}/{database}[;<;,{:identifier}={:param}>]
    ```

    ![](images/intellij-driver.png)

3. Then create a new Project Data Source using the new Driver.

    ![General Tab](images/intellij-secure-connection-general.png)

    ```dosini
    -Dsun.security.krb5.debug=true
    -Djavax.security.auth.useSubjectCredsOnly=false
    ```

    ![Advanced Tab](images/intellij-secure-connection-advanced.png)

4. After creating the Project Data Source, test the connection.  You should see the following:

    ![](images/intellij-connection-test.png)

# How to Build
To build locally, you must have Maven installed and properly configured.  After that it's as simple as running `mvn:package`.  A file called `hive-jdbc-uber-x.jar` will be created in your `target` directory.  The newly created jar will have the Hive JDBC driver as well as all required dependencies.
