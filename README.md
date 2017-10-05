
The Vault
=========

An attempt at a tamper proof Auditing sollution.

Prerequisites for Development
-------------------------------------
You must have the following installed on your machine:

  - JDK 1.8 or higher

  - Maven 3.2.3 or higher


Building and Deploying
----------------------
You can build and deploy this example in two ways:

- A. Using Jetty: Quick and Easy
  This option is useful if you want to get up and running quickly.
   
- B. Using Your Favorite WebContainer (like Apache Tomcat)
  This option is useful if you want to see the application
  as a web application inside your favorite web container.


A. Using Jetty: Quick and Easy
------------------------------
To build the example and deploy to Jetty, complete the
following steps:

1. In a command prompt/shell, change to the directory
   that contains this README.md file.

2. Enter the following Maven command:

```
mvn package install -DskiptTests
```

This Maven command builds the application as an embedded web application.

3. Enter the following Maven command to run the app:

```
mvn jetty:run
```

This Maven command starts Jetty and deploys the web application to Jetty. Once complete,
you should see the following printed to the console:

[INFO] Started Jetty Server

Testing the application
-----------------------

Once the application is running, you can access the application by executing commands:

1. Add an Audit record

In a web browser, enter the following URL:

```
http://localhost:8080/addData?data=HelloWorld
```

This will create an Audit record in the database and update the hash tree with the hash of that audit data. The system responds with a dump of the Audit records and the Hash tree buckets. To just add the data, and not see the entire dump, use:

```
http://localhost:8080/addSimpleData?data=HelloWorld
```

2. Verify if the audit has been tampered with:

```
http://localhost:8080/check
```

This will return an array of errors (if any).

3. Dump the Audit records:

```
http://localhost:8080/records
```

4. Dump the Hash tree records:

```
http://localhost:8080/buckets
```

5. Dump the Audit and Hash records:

```
http://localhost:8080/dump
```

6. Remove an Audit record by oid:

```
http://localhost:8080/removeRecord?oid=<oid>
```

7. Clear the entire database removing all Audit and all Hash buckets:

```
http://localhost:8080/removeAll?confirm=Yes
```
