package com.adahas.tools.jmxeval;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import com.adahas.tools.jmxeval.exception.EvalException;
import com.adahas.tools.jmxeval.response.Response;
import com.adahas.tools.jmxeval.response.Status;

/**
 * Execution context
 */
public class Context {

  /**
   * Filename parameter name
   */
  public static final String CONFIG_FILENAME = "filename";

  /**
   * Validation parameter name
   */
  public static final String CONFIG_VALIDATE = "validate";

  /**
   * Schema parameter name
   */
  public static final String CONFIG_SCHEMA = "schema";

  /**
   * JMX server connection
   */
  private MBeanServerConnection connection;

  /**
   * Dynamic variables collection used while execution
   */
  private final Map<String, Object> variables = new HashMap<>();

  /**
   * Configuration values
   */
  private final Map<String, String> config;

  /**
   * Response
   */
  private final Response response = new Response();

  /**
   * Constructs the context with given configuration
   *
   * @param config Configuration elements map
   */
  public Context(final Map<String, String> config) {
    this.config = config;
  }

  /**
   * Get a configuration parameter value
   *
   * @param key Configuration parameter key
   * @return Configuration parameter value
   */
  public String getConfigValue(final String key) {
    return config.get(key);
  }

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
   * @throws EvalException When the variable is already defined or is having a reserved name as its variable name
   */
  public void setVar(final String name, final Object value) throws EvalException {
    if (variables.containsKey(name)) {
      throw new EvalException(Status.UNKNOWN, "Variable already set: " + name);
    } else {
      variables.put(name, value);
    }
  }

  /**
   * Get a variable from the global variables collection
   *
   * @param name Name of the variable with optional : separated default value.
   * @return Value of the variable
   * @throws EvalException When the variable is not set and no default is provided
   */
  public Object getVar(final String name) throws EvalException {
    String key = name;
    String defaultValue = null;
    final int seperator = name.indexOf(':');
    if (seperator >= 0) {
      key = name.substring(0, seperator);
      defaultValue = name.substring(seperator + 1);
    }
    if (variables.containsKey(key)) {
      return variables.get(key);
    } else if (defaultValue != null) {
      return defaultValue;
    } else {
      throw new EvalException(Status.UNKNOWN, "Variable not set: " + name);
    }
  }

  /**
   * Set 'validate' parameter value.
   *
   * @param value Value of the 'validate' parameter
   */
  @Option(name = "--" + CONFIG_VALIDATE, metaVar = "<boolean>", usage = "set validation true|false, default is false")
  protected void setConfigValidate(final String value) {
    config.put(CONFIG_VALIDATE, value);
  }

  /**
   * Set 'validate' parameter value.
   *
   * @param value Value of the 'validate' parameter
   */
  @Option(name = "--" + CONFIG_SCHEMA, metaVar = "<version>", usage = "set schema version")
  protected void setConfigSchema(final String value) {
    config.put(CONFIG_SCHEMA, value);
  }

  /**
   * Set 'define' parameter value.
   *
   * @param value Value of the 'define' parameter
   */
  @Option(name = "--set", aliases = { "--define" }, metaVar = "<name=value>", usage = "set variable name to value")
  protected void setDefine(final String nameValue) throws EvalException {
    final String[] tokens = nameValue.split("=", 2);
    if (tokens.length != 2) {
      throw new EvalException(Status.UNKNOWN, "arg to --set (" + nameValue + ") must be in \"name=value\" format!");
    }
    setVar(tokens[0], tokens[1]);
  }

  /**
   * Set filename parameter.
   *
   * @param value File name
   */
  @Argument (metaVar = "<filename>", required = true)
  protected void setConfigFilename(final String value) {
    config.put(CONFIG_FILENAME, value);
  }
}
