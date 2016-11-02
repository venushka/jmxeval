package com.adahas.tools.jmxeval.model.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.mockito.internal.util.io.IOUtil;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.JMXEvalException;
import com.adahas.tools.jmxeval.model.Element;
import com.adahas.tools.jmxeval.model.ElementBuilder;

/**
 * Connection element which encloses all evals performed using a single
 * JMX connection
 */
public class Connection extends Element {

  private static final Logger log = Logger.getLogger(Connection.class.getName());

  /**
   * Connection URL
   */
  private final Field url;

  /**
   * Username to connect using (optional)
   */
  private final Field username;

  /**
   * Password to use (required only if username is set)
   */
  private final Field password;

  /**
   * Whether SSL is enabled
   */
  private final Field ssl;

  /**
   * Constructs the element
   *
   * @param context Execution context
   * @param node XML node
   */
  public Connection(final Context context, final Node node) {
    super(context);

    url = getNodeAttr(node, "url");
    username = getNodeAttr(node, "username");
    password = getNodeAttr(node, "password");
    ssl = getNodeAttr(node, "ssl", "false");
  }

  /**
   * @see Element#process()
   */
  @Override
  public void process() throws JMXEvalException {
    JMXConnector jmxConnector = null;
    try {
      final JMXServiceURL jmxServiceURL = new JMXServiceURL(url.get());
      final Map<String, Object> jmxEnv = new HashMap<>();

      if (username.get() != null) {
        jmxEnv.put(JMXConnector.CREDENTIALS, new String[] { username.get(), password.get() });
      }

      if (Boolean.parseBoolean(ssl.get())) {
        final SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
        jmxEnv.put("com.sun.jndi.rmi.factory.socket", csf);
      }

      jmxConnector = connect(jmxServiceURL, jmxEnv);

      // establish jmx connection
      context.setConnection(jmxConnector.getMBeanServerConnection());

    } catch (IOException e) {
      log.log(Level.WARNING, "Exception occured while opening connection", e);
      // do not set the connection if exception occurs, but let the process continue
      // so that the plugin output is consistent, and shows all the eval checks on it
    } finally {
      try {
        // process elements even if the connection origination fails
        super.process();
      } finally {
        IOUtil.closeQuietly(jmxConnector);
      }
    }
  }

  /**
   * Get a {@link JMXConnector} to connect to a given URL. Using a getter method to allow testing, due to lack of a DI framework.
   *
   * @return an {@link ElementBuilder} instance
   * @throws IOException if connecting to the JMX URL fails
   */
  JMXConnector connect(final JMXServiceURL jmxServiceURL, Map<String, Object> jmxEnv) throws IOException {
    return JMXConnectorFactory.connect(jmxServiceURL, jmxEnv);
  }
}
