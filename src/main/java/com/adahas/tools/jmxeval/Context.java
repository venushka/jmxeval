package com.adahas.tools.jmxeval;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;

import com.adahas.tools.jmxeval.exception.JMXEvalException;
import com.adahas.tools.jmxeval.response.Response;

/**
 * Execution context
 */
public class Context {

  /**
   * Filename.
   */
  @Argument(metaVar = "<filename>", required = true)
  private String filename;

  /**
   * Run with verbose logging.
   */
  @Option(name = "--verbose", handler = BooleanOptionHandler.class, usage = "run with verbose output (note: only use for debugging issues by running the plugin manually)")
  private boolean verbose;

  /**
   * Validate input file with schema.
   */
  @Option(name = "--validate", handler = BooleanOptionHandler.class, usage = "turn validation on")
  private boolean validate;

  /**
   * Schema version.
   */
  @Option(name = "--schema", metaVar = "<version>", usage = "set schema version")
  private String schemaVersion;

  /**
   * JMX server connection
   */
  private MBeanServerConnection connection;

  /**
   * Dynamic variables collection used while execution
   */
  private final Map<String, Object> variables = new HashMap<>();

  /**
   * Response
   */
  private final Response response = new Response();

  /**
   * Get the response instance
   *
   * @return the response
   */
  public Response getResponse() {
    return response;
  }

  /**
   * Get the MBeanServerConnection instance
   *
   * @return the connection
   */
  public MBeanServerConnection getConnection() {
    return connection;
  }

  /**
   * Set the MBeanServerConnection instance
   *
   * @param connection the connection to set
   */
  public void setConnection(final MBeanServerConnection connection) {
    this.connection = connection;
  }

  /**
   * Sets a variable in the global variables collection
   *
   * @param name Name of the variable
   * @param value Value of the variable
   * @throws JMXEvalException When the variable is already defined or is having a reserved name as its variable name
   */
  public void setVar(final String name, final Object value) throws JMXEvalException {
    if (variables.containsKey(name)) {
      throw new JMXEvalException("Variable already set: " + name);
    } else {
      variables.put(name, value);
    }
  }

  /**
   * Get a variable from the global variables collection
   *
   * @param name Name of the variable with optional : separated default value.
   * @return Value of the variable
   */
  public Object getVar(final String name) {
    String key = name;
    String defaultValue = null;
    final int seperator = name.indexOf(':');
    if (seperator >= 0) {
      key = name.substring(0, seperator);
      defaultValue = name.substring(seperator + 1);
    }
    if (variables.containsKey(key)) {
      return variables.get(key);
    } else if (System.getProperties().containsKey(key)) {
      return System.getProperty(key);
    } else if (defaultValue != null) {
      return defaultValue;
    } else {
      return null;
    }
  }

  /**
   * Set parameter value passed as key=value pair.
   *
   * @param value Value of the 'define' parameter
   */
  @Option(name = "--set", aliases = { "--define" }, metaVar = "<name=value>", usage = "set variable name to value")
  public void setVar(final String nameValue) throws JMXEvalException {
    final int separatorIndex = nameValue.indexOf('=');
    if (separatorIndex > 0) {
      setVar(nameValue.substring(0, separatorIndex), nameValue.substring(separatorIndex + 1));
    } else {
      throw new JMXEvalException("arg to --set (" + nameValue + ") must be in \"name=value\" format!");
    }
  }

  /**
   * Get the filename.
   *
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Use verbose logging.
   *
   * @return the verbose
   */
  public boolean isVerbose() {
    return verbose;
  }

  /**
   * Validate input file against schema.
   *
   * @return the validate
   */
  public boolean isValidate() {
    return validate;
  }

  /**
   * Get the schema version.
   *
   * @return the schemaVersion
   */
  public String getSchemaVersion() {
    return schemaVersion;
  }
}
