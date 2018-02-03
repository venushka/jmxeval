jmxeval
=======

[![Build Status](https://travis-ci.org/venushka/jmxeval.svg?branch=master)](https://travis-ci.org/venushka/jmxeval)
[![Quality Gate](https://sonarcloud.io/api/badges/gate?key=com.adahas:jmxeval:master)](https://sonarcloud.io/dashboard/index/com.adahas:jmxeval:master)

*jmxeval* is a highly flexible Nagios/NRPE plugin for monitoring Java applications via JMX. Instead of just checking just an attribute of an MBean, JMXEval allows you to query multiple MBean attributes as well as results of MBean method invocations, and also perform mathematical computations to derive much more meaningful figures for monitoring. JMXEval can also provide all the information captured from MBeans as well as any computed figures as performance data allowing you to capture and visualise trends in Nagios.

# Setting up
The only requirement for running *jmxeval* is a Java 7 runtime. Please follow the steps below to setup *jmxeval*.

## On Linux (or any \*nix based system)
To setup jmxeval with Nagios/NRPE,

1. Download the latest version's .tar.gz file.
2. Unpack it by running *tar -xvzf &lt;filename&gt;*
3. Copy the *check_jmxeval* file and the *jmxeval* directory to the Nagios/NRPE plugins directory from the zip file in the Nagios plugins directory, usually */usr/lib/nagios/plugins* on Nagios server and */usr/local/nagios/libexec* when using NRPE.
4. Ensure the *JAVA_HOME* environment variable is set to the location of a Java 7 or higher JRE/JDK installation, or uncomment the change the *JAVA_HOME* variable in *check_jmxeval* to point to one.
5. Run the plugin in the console by executing the *check_jmxeval* script and see if it prints the [command syntax](#command-syntax).

## On Windows
To setup jmxeval with NRPE,

1. Download the latest version's .zip file.
2. Unzip the file.
3. Copy the *check_jmxeval.bat* file and the *jmxeval* directory to the NRPE plugins directory.
4. Ensure the *JAVA_HOME* environment variable is set to the location of a Java 7 or higher JRE/JDK installation, or uncomment the change the *JAVA_HOME* variable in *check_jmxeval* to point to one.
5. Run the plugin in the console by executing the *check_jmxeval.bat* script and see if it prints the [command syntax](#command-syntax).

# Making your Java application ready for monitoring
jmxeval uses [JMX](http://docs.oracle.com/javase/tutorial/jmx/overview/index.html) to gather information from a running Java application to perform any checks that its configured to perform. Before setting up any checks in jmxeval, you will need to [enable JMX](http://docs.oracle.com/javase/7/docs/technotes/guides/management/agent.html) on the application you are going to monitor, and also configure authentication and SSL for JMX connections.

If you are using jmxeval to monitor an application using a framework such as Spring, or an application container such as Tomcat, please refer their documentation on how to enable JMX as the preferred method of configuring JMX could be different.

# Command syntax
Following is the syntax for running the plugin. It can be run standalone in a console without Nagios/NRPE to check if it works as expected before using it with Nagios/NRPE.
```
check_jmxeval <filename> [--set (--define) <name=value>] [--validate] [--verbose]
 --set (--define) <name=value> : set variable name to value
 --validate                    : turn validation on (default: false)
 --verbose                     : run with verbose output (note: only use for
                                 debugging issues by running the plugin
                                 manually) (default: false)
```

## &lt;filename&gt;
The *filename* is the XML configuration file that defines what is being checked by the jmxeval. The configuration file can define what information needs to be queries, any transformation of data that need to happen, any calculations, when to alert and also what performance data need to be captured. The *filename* is mandatory for running the plugin, when it's not specified, it will result in an error and print the above syntax. You can select one the of configuration files that comes in the *jmxeval/conf* directory or you can write your own, it's pretty simple. The syntax for writing XML configuration files is explained in the [configuration files](#configuration-files) section.

## --set &lt;name=value&gt; / --define &lt;name=value&gt;
This allows you to set a variable that can be used when the configuration file is being evaluated to perform the configured check(s), allowing the configuration files to be reused. For example, if you want to check the heap size of a Java process that is allowed a maximum of 1GB of heap space, and you would like it to warn at 750MB and alert as critical at 900MB, you will need to specify these alert levels in the configuration file. But if you have lot of processes which you need to do the same check for, having to create separate files for each process is cumbersome. Instead, you can make the *warning* and *critical* levels specified in the configuration file be driven by variables set on the command line. For example,
```
check_jmxeval conf/check-memory-heap-usage-size-mb.xml --set warning=750 --set critical=900
```
You can also use these to set values list the hostname, port and authentication details for originating the JMX connections. Within the configuration files, these can be referred in most XML element attributes making the configuration adaptable for monitoring multiple processes. See the [configuration files](#configuration-files) section to find out how to use these variables in the configuration files.

## --validate
Adding this option will enable XML configuration file validation when running jmxeval. This ensures that the configuration file contains the correct XML elements and attributes in the correct structure as expected by jmxeval, which would be helpful if you are writing your own configuration files. The XML configuration file is validated against the [jmxeval-1.2](http://www.adahas.com/schema/jmxeval-1.2) schema definition (XSD).

## --verbose
This make jmxeval log more information to the console while its running and in an event of an error. Since Nagios uses the console output as the means of capturing data, while running in non-verbose mode, jmxeval will adhere to Nagios plugin output standard, which restricts the amount of information that can be shown in the console. However, at times, especially when trying to troubleshoot a failure, it would be useful to get all the information you can get, which is exactly what this option does. Remember that when this option is enabled, Nagios might not be able to interpret the output of jmxeval, so make sure you **don't** include this option when using jmxeval with Nagios/NRPE.

# Configuration files
When running jmxeval, you will need to specify a configuration file. This is an XML file that specifies what checks it should perform. A configuration file for a really simple check would specify the following.

1. How to connect to the Java process.
2. What is being checked.
3. What information needs to be fetched from the Java process.
4. When to raise alerts.

Let's look an example for checking the number of threads in a Java process, and alert if it reaches a given threshold.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmxeval:jmxeval xmlns:jmxeval="http://www.adahas.com/schema/jmxeval-1.2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <connection url="service:jmx:rmi:///jndi/rmi://tomcat7.adahas.com:8999/jmxrmi" ssl="false" username="monitoruser" password="supersecret">
    <eval name="Threads">
      <!-- get the thread count and assign it to a variable named 'threadCount' -->
      <query var="threadCount" objectName="java.lang:type=Threading" attribute="ThreadCount" />
      <!-- use the 'threadCount' variable to perform the check -->
      <check useVar="threadCount" warning="75" critical="90" message="ThreadCount is ${threadCount}" />
    </eval>
  </connection>
</jmxeval:jmxeval>
```

In the above example,

1. *&lt;connection&gt;* specifies *how to connect*.
2. *&lt;eval&gt;* specifies *what is being checked*.
3. *&lt;query&gt;* specifies *what to get*.
4. *&lt;check&gt;* specifies *when to alert*.

However, in certain events, just the simple check is not enough. For example, you might want to have a check on heap usage as a percentage. This would require the maximum allowed heap size and the actual heap usage and then perform a calculation, and a check on the percentage, let's say warn at 75% and critical at 90%. You may also want to capture the actual heap size as well as performance data for Nagios to capture it and look at trends. Let's have a look at how we can do this with jmxeval.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmxeval:jmxeval xmlns:jmxeval="http://www.adahas.com/schema/jmxeval-1.2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <connection url="service:jmx:rmi:///jndi/rmi://tomcat7.adahas.com:8999/jmxrmi" ssl="false" username="monitoruser" password="supersecret">
    <eval name="Heap Usage">
      <!-- get the allowed max heap size and assign it to a variable named 'heapMax' -->
      <query var="heapMax" objectName="java.lang:type=Memory" attribute="max" compositeAttribute="HeapMemoryUsage" />
      <!-- get the current heap size and assign it to a variable named 'heapSize' -->
      <query var="heapSize" objectName="java.lang:type=Memory" attribute="used" compositeAttribute="HeapMemoryUsage">
        <!-- capture performance data from the container element, 'heapSize' in this case -->
        <perf />
      </query>
      <!-- calculate the heap usage percentage using the previouly defined variables and assign it to a new variable named 'heapPercentage' -->
      <expr var="heapPercentage" expression="${heapSize} / ${heapMax} * 100" scale="0" />
      <!-- use the 'heappercentage' variable to perform the check -->
      <check useVar="heapPercentage" warning="75" critical="90" message="Heap usage is ${heapPercentage}%">
        <!-- capture 'heapPercentage' as performance data -->
        <perf />
      </check>
    </eval>
  </connection>
</jmxeval:jmxeval>
```

Depending on how many Java processes you need to monitor, sometimes it might be be quite a lot of work to setup checks for each and every  individual aspect of each Java process. For example, if you have a lot of microservices running, you might just want to know if each process is working OK or not. *jmxeval* makes this easier by allowing multiple checks to be defined in the same configuration file. Following is an example where two checks are included in the same configuration file.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmxeval:jmxeval xmlns:jmxeval="http://www.adahas.com/schema/jmxeval-1.2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <connection url="service:jmx:rmi:///jndi/rmi://tomcat7.adahas.com:8999/jmxrmi" ssl="false" username="monitoruser" password="supersecret">
    <!-- check the heap usage -->
    <eval name="Heap Usage">
      <query var="heapMax" objectName="java.lang:type=Memory" attribute="max" compositeAttribute="HeapMemoryUsage" />
      <query var="heapSize" objectName="java.lang:type=Memory" attribute="used" compositeAttribute="HeapMemoryUsage">
        <perf />
      </query>
      <expr var="heapPercentage" expression="${heapSize} / ${heapMax} * 100" scale="0" />
      <check useVar="heapPercentage" warning="75" critical="90" message="Heap usage is ${heapPercentage}%">
        <perf />
      </check>
    </eval>
    <!-- check the garbage collection run time -->
    <eval name="GC Duration">
      <query var="gcMillis" objectName="java.lang:type=GarbageCollector,name=PS Scavenge" compositeAttribute="LastGcInfo" attribute="duration" />
      <expr var="gcSecs" expression="${gcMillis} / 1000" scale="0" />
      <check useVar="secs" warning="${gcWarning}" critical="${gcCritical}" message="Last GC took ${gcSecs}s">
        <perf />
      </check>
    </eval>
  </connection>
</jmxeval:jmxeval>
```

## Configuration elements
Let's look at the different elements that you can use in the configuration files.

### &lt;eval&gt;
Represents a single check. This is a container for all the other elements that performs different tasks to achieve the end result of the check. Following attributes can be set on this element.

| Attribute | Description | Mandatory | Default |
| --- | --- | --- | --- |
| name | Display name of the check. This will be the name shown in the plugin output. | Yes | |
| host | A regular expression pattern to match against the hostname of the machine the plugin is being executed on. This is useful if there are multiple *&lt;eval&gt;* elements are configured in the same configuration file and only some of them needs to be executed in some of the monitored machines. By default, it matches all hostnames. | No | .* |

Following is an example where an *&lt;eval&gt;* is defined that should only run on machines that has hostnames starting with *ec2-*.
```xml
<eval name="EC2 Check" host="^ec2-.*">
```

### &lt;connection&gt;
Defines a JMX connection to a Java process. Any queries or method invocations on MBeans made using this connection should be nested within this element. Following attributes can be set on this element.

| Attribute | Description | Mandatory | Default |
| --- | --- | --- | --- |
| url | JMX connection URL to the Java process being monitored. | Yes | |
| username | Username to use for JMX authentication. If a username is not specified, it is assumed that no authentication is required. | No | |
| password | Password used for authentication, ignored if a username is not set | No | |
| ssl | Use SSL enabled JMX connection. (true / false) | No | false |

The *url* to can be quite different based on how JMX is configured on the Java process you are attempted to connect to. Here are some example URLs,

Connection to a Java process running on *localhost* on port *8999*, without authentication.
```xml
<connection url="service:jmx:rmi:///jndi/rmi://localhost:8999/jmxrmi">
```

Connection to a Java process running on *tomcat7.adahas.com* on port *8999*, with authentication and SSL.
```xml
<connection url="service:jmx:rmi:///jndi/rmi://tomcat7.adahas.com:8999/jmxrmi" username="monitoruser" password="supersecret" ssl="true">
```

Connection to a Java process running on host named `tomcat7.adahas.com` having JMX and JNDI services running on two different ports; JMX on port *8999* and JNDI *1099*. Note that this type of URL will only be needed if JNDI service has been configured specifically to use a different port.
```xml
<connection url="service:jmx:rmi://tomcat7.adahas.com:8999/jndi/rmi://tomcat7.adahas.com:1099/jmxrmi">
```

### &lt;query&gt;
Reads an attribute of a MBean and assigns it to a variable. This element must be contained in a *&lt;connection&gt;* element, which makes the JMX connection made to the Java process available to this element to execute the query. The variable that is created by this with the value of the MBean attribute can be referenced for performing calculations and alerting in the elements that follow it. Following attributes can be set on this element.

| Attribute | Description | Mandatory | Default |
| --- | --- | --- | --- |
| var | The name of the variable to create with the value read from the MBean attribute. This must be a variable name that is not set as reassigning values to variables is not allowed. | Yes | |
| objectName | Name of the MBean to query. | Yes | |
| compositeAttribute | If the attribute being queried is an attribute of a  [CompositeData](https://docs.oracle.com/javase/7/docs/api/javax/management/openmbean/CompositeData.html) attribute, the name of CompositeData attribute. | No | |
| attribute | The attribute to read the value of. When a *compositeAttribute* is set, the *attribute* will refer to the attribute name in the [CompositeData](https://docs.oracle.com/javase/7/docs/api/javax/management/openmbean/CompositeData.html) field. | Yes | - |
| valueOnFailure | The value to return if the query fails. These failures can include missing *objectName* or *attribute*. This is an optional attribute. If not set, JMX failures cause the plugin to fail. | No | - |

Following is an example of making a query for a simple attribute value.
```xml
<query var="threadCount" objectName="java.lang:type=Threading" attribute="ThreadCount" />
```

When querying attribute of a [CompositeData](https://docs.oracle.com/javase/7/docs/api/javax/management/openmbean/CompositeData.html) attribute, it would look like this.
```xml
<query var="heapUsage" objectName="java.lang:type=Memory" attribute="used" compositeAttribute="HeapMemoryUsage" />
```

The following is an example of querying an error queue depth in an ActiveMQ cluster where the AMQ-Master is the only host that has the given *objectName*, so we want the plugin to return an *OK* if the *objectName* and/or *attribute* are missing (non-master nodes).
```xml
<query var="errQueueDepth" objectName="org.apache.activemq:type=Broker,brokerName=amq-broker-${nodeName},destinationType=Queue,destinationName=myErrors" attribute="QueueSize" valueOnFailure="0" />
```

### &lt;exec&gt;
Executes an MBean operation, captures the return value and assigns it to a variable. Similar to the *&lt;query&gt;* element, this also must be contained in a *&lt;connection&gt;* element, from which it acquire the connection to perform the MBean operation call. Following attributes can be set on this element.

| Attribute | Description | Mandatory | Default |
| --- | --- | --- | --- |
| var | The name of the variable to create with the value read returned from the MBean operation call. This must be a variable name that is not set as reassigning values to variables is not allowed. | Yes | |
| objectName | Name of the MBean to call the operation on. | Yes | |
| operation | The name of the operation to call, including the types of the arguments in Java method signature format. The argument types that are supported are *java.lang.String*, *boolean*, *byte*, *short*, *int* and *long*. | Yes | - |
| arg1 ... arg10 | Values of the arguments to pass to the operation call. The order of the arguments *must* match the order of the arguments specified in the *operation* attribute. If any of the attributes should be set to *null*, skip specifying the attribute altogether and continue with the next argument number. If an argument should be set to empty (""), specify the attribute with the value set to empty.| No | |

An example of invoking a method without any arguments would look like this.
```xml
<exec var="clearCacheSecs" objectName="com.adahas:type=CacheManager" operation="clearCaches()" />
```

If the operation requires a `String` argument, it would look like this.
```xml
<exec var="usersGB" objectName="com.adahas:type=ActiveSessionManager" operation="getUserSessionsByCountry(java.lang.String)" arg1="GB" />
```

If the second argument need to be `null` and the third needs to have a value, you just skip specifying *arg2* altogether.
```xml
<exec var="usersLastTenLoggedIn" objectName="com.adahas:type=ActiveSessionManager" operation="getUserSessionsByCountryCityAndLimit(java.lang.String, java.lang.String, long)" arg1="GB" arg3="10" />
```

### &lt;expr&gt;
Evaluate a mathematical expression. It supports addition (+), subtraction (-), multiplication (\*), division (/) and remainder (%) operations. You can use parentheses ( ) to specify the order of operations. Following attributes can be set on this element.

| Attribute | Description | Mandatory | Default |
| --- | --- | --- | --- |
| var | The name of the variable to create with the result value from the evaluation of the *expression* specified. | Yes | |
| expression | The expression to evaluate. | Yes | |
| scale | The number of decimal places to have in the result of the expression evaluation. | No | 2 |

Here a simple example of an expression evaluation. This uses two variables, *heapUsage* and *heapMax*, probably created by two *&lt;query&gt;* elements before this, to calculate the heap usage as a percentage.
```xml
<expr var="heapPercentage" expression="${heapUsage} / ${heapMax} * 100" scale="0" />
```

To calculate the free heap percentage, it can be changed to the following. By adding parenthesis, we can get the expression within the parenthesis to execute first.
```xml
<expr var="heapPercentage" expression="(${heapMax} - ${heapUsage}) / ${heapMax} * 100" scale="0" />
```

### &lt;check&gt;
Reads a given variable and alerts if it matches the warning or critical alert criteria specified. The check can operate in two different modes, *default* and *regex* allowing a range of different ways to define the criteria for alerting.

In *default* mode, if the variable that is being checked is a numeric value, the check will be performed assuming that the *warning* and *critical* criteria are specified based on the [thresholds and ranges](https://nagios-plugins.org/doc/guidelines.html#THRESHOLDFORMAT) on the Nagios plugin development guidelines, and for non-numeric values the variable is compared with the *warning* and *critical* attributes to check if they are an exact match to determine an alert need to be raised.

In *regex* mode, any type of variable is considered as text and the *warning* and *critical* must be [regular expressions](https://docs.oracle.com/javase/tutorial/essential/regex/intro.html), where the value is checked for matching the patterns defined to determine if an alert need to be raised.

Following attributes can be set on this element.

| Attribute | Description | Mandatory | Default |
| --- | --- | --- | --- |
| useVar | The name of the variable to perform the check against. | Yes | |
| mode | The mode to perform the check, *default* or *regex*. | Yes | default |
| warning | The criteria for raising a warning level alert. | No | |
| critical | The criteria for raising a critical level alert. | No | |
| message | The message to accompany the check result in the output. | No | |

Let's have a look at few checks. Simplest check would be to see if a numeric variable is greater than 75 to raise a warning, and greater than 90 to raise a critical alert.
```xml
<check useVar="heapPercentage" warning="75" critical="90" message="Heap usage is ${heapPercentage}%">
```

If the variable value is text, a check can be done to see if the value exactly matches a given value. In this example, if the value of variable is "disconnected" a critical alert will be raised, and no warning alert set.
```xml
<check useVar="queueStatus" critical="disconnected" />
```

Irrelevant of the variable's content, be it numeric or non-numeric, regular expressions can be used to check the result. This example raises a warning alert if the variable has a value that contains the word *awaiting lock* and a critical alert if it contains *dead lock* or *failed*.
```xml
<check useVar="lockStatus" warning=".*awaiting\slock" critical=".*(dead\slock|failed).*" />
```

### &lt;perf&gt;
Captures information from the element that contains a *&lt;perf&gt;* element and reports it as [performance data](https://nagios-plugins.org/doc/guidelines.html#AEN200) for Nagios. Performance data allows Nagios to produce charts and graphs allowing you to identify trends and troubleshoot issues.

A *&lt;perf&gt;* element can be nested in *&lt;query&gt;*, *&lt;exec&gt;*, *&lt;expr&gt;* and *&lt;check&gt;* elements. By just adding a *&lt;perf&gt;*  element will capture any information that its parent element makes available to it. In addition, following attributes can also be set on this element. If a value is set directly to this element, and its parent also provides information for the same attribute, the value set directly to this element gets the higher precedence.

| Attribute | Description | Mandatory | Default |
| --- | --- | --- | --- |
| label | Display name to use in the performance data for the variable provided by the parent element. | No | *var* from the parent element |
| warning | Warning value to include in performance data. | No | *warning* from the parent element |
| critical | Warning value to include in performance data. | No | *critical* from the parent element |
| min | Minimum value of the data range to include in performance data. | No | |
| max | Maximum value of the data range to include in performance data. | No | |
| unit | The unit of measure (UOM) for the data reported, could be unspecified, or one of *us*, *ms*, *s*, *%*, *B*, *KB*, *MB*, *GB*, *TB*, *c* (counter)| No |

## Defining variables
*jmxeval* uses variables to hold information captured from MBeans, results from evaluating mathematical expressions, perform checks for raising alerts and report performance data. Many of the XMl elements in the configuration files creates variables as part of their role when the plugin is executing, the elements that follow can refer to them by the ${varName} notation.

In addition to this, you can also define variables via [the command line](#command-syntax), which can then be referred in the configuration files. For example, you run *jmxeval* with `--set host=tomcat7.adahas.com --set port=8999` as a command line argument, and then refer in the configuration file as follows.
```xml
<connection url="service:jmx:rmi:///jndi/rmi://${host}:${port}/jmxrmi">
```

This makes the configuration files much more reusable, as you can make all the process specific information and even the alert levels parameterised, so you can use the same configuration file to do similar (but not same) checks in multiple processes using the same configuration file. For example, the following configuration file allow you to set the process specific information such as *host* and *port*, along with the *warning* and *critical* alert thresholds via the command line.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmxeval:jmxeval xmlns:jmxeval="http://www.adahas.com/schema/jmxeval-1.2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <connection url="service:jmx:rmi:///jndi/rmi://${host}:${port}/jmxrmi">
    <eval name="Heap Usage">
      <query var="heapUsage" objectName="java.lang:type=Memory" attribute="used" compositeAttribute="HeapMemoryUsage">
        <perf />
      </query>
      <query var="heapMax" objectName="java.lang:type=Memory" attribute="max" compositeAttribute="HeapMemoryUsage" />
      <expr var="heapPercentage" expression="${heapUsage} / ${heapMax} * 100" scale="0" />
      <check useVar="heapPercentage" warning="${warning}" critical="${critical}" message="Heap usage is ${heapPercentage}%">
        <perf />
      </check>
    </eval>
  </connection>
</jmxeval:jmxeval>
```

You can also refer to any of the Java system properties in the configuration files using the same syntax as well. If you are running the *jmxeval* jar file directly (not using the check_jmxeval scripts), you can also use JVM arguments to set arguments using VM arguments set as *-DvarName=varValue*

## Variable defaults
Setting variables via the command line makes the configuration files reusable, however it does make the command to run the plugin longer, which again will be hard to maintain if there is a lot of process that need to be monitored.

In most cases, the check(s) you would want to do is the same, for example, if the heap memory usage is at 75% raise a warning, and if its at 90% raise a critical alert. However, there might a few processes where you want to use different thresholds, for example, warn at 60% and critical at 80%. We could use two configuration files in this case or specify the thresholds in each command line, either way, too much configuration to manage. The answer here is variable defaults.

When you refer to a variable in a configuration file using the *${varName}* notation, you can also specify a default value as *${varName:defaultValue}*. When *jmxeval* evaluates a configuration file that refers to a variable with a default value, it first checks if the variable is defined in the configuration fil or the command line, if not it checks if a system property exists with the same name, failing both, the default value is returned instead of *null*.

So, for the above example, the following configuration file will use the defaults for warning and critical levels unless they are specified via the command line.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmxeval:jmxeval xmlns:jmxeval="http://www.adahas.com/schema/jmxeval-1.2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <connection url="service:jmx:rmi:///jndi/rmi://${host}:${port}/jmxrmi">
    <eval name="Heap Usage">
      <query var="heapUsage" objectName="java.lang:type=Memory" attribute="used" compositeAttribute="HeapMemoryUsage">
        <perf />
      </query>
      <query var="heapMax" objectName="java.lang:type=Memory" attribute="max" compositeAttribute="HeapMemoryUsage" />
      <expr var="heapPercentage" expression="${heapUsage} / ${heapMax} * 100" scale="0" />
      <check useVar="heapPercentage" warning="${warning:75}" critical="${critical:90}" message="Heap usage is ${heapPercentage}%">
        <perf />
      </check>
    </eval>
  </connection>
</jmxeval:jmxeval>
```

## Sample configuration files
*jmxeval* now comes with few pre-defined sample configuration files. These can be used as is, or be used as reference for creating your own configuration files. The bundled configuration files can be found in the *jmxeval/conf* directory inside the zip/tar.gz distribution.

# Feature requests & issues

If you have any feature requests or encounter a bug please do [raise it here](https://github.com/venushka/jmxeval/issues) and/or send me a pull request.

# License

    Copyright 2012-2018 Venushka Perera

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
